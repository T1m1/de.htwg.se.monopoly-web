package controllers;

import java.util.*;
import java.util.concurrent.TimeUnit;

import models.MonopolyObserver;
import models.PendingGame;

import org.pac4j.play.java.JavaController;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import play.Logger;
import play.Logger.ALogger;
import play.mvc.Result;
import play.mvc.WebSocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import de.htwg.monopoly.controller.IController;
import de.htwg.monopoly.entities.IFieldObject;
import de.htwg.monopoly.entities.impl.Player;
import de.htwg.monopoly.util.IMonopolyUtil;
import de.htwg.monopoly.util.PlayerIcon;
import de.htwg.monopoly.util.UserAction;

public class Application extends JavaController {

	private static Cache<String, IController> controllers = CacheBuilder.newBuilder().expireAfterAccess(2, TimeUnit.DAYS).build();
	private static Cache<String, MonopolyObserver> observer = CacheBuilder.newBuilder().expireAfterAccess(2, TimeUnit.DAYS).build();
	private static Cache<String, String> lastMessage = CacheBuilder.newBuilder().expireAfterAccess(2, TimeUnit.DAYS).build();
	private static Cache<String, Boolean> prisonRollFlags = CacheBuilder.newBuilder().expireAfterAccess(2, TimeUnit.DAYS).build();

	private static Cache<String, PendingGame> pendingGames = CacheBuilder.newBuilder().expireAfterAccess(2, TimeUnit.DAYS).build();

	private static final ALogger logger = Logger.of(Application.class);

	public static Result welcome() {
		logger.debug("Welcome page loading");
		return ok(views.html.welcome.render(""));
	}

	public static Result index() {
		logger.debug("index site loading");
		return ok(views.html.index.render("Index", controllers.asMap().get(getSession())));
	}

	/**
	 * get specific game instance.
	 * 
	 * @param game
	 *            id for instance.
	 * @return view for specific instance.
	 */
	public static Result showInstance(String game) {
		logger.debug("new site loading");
		if (!controllers.asMap().containsKey(game)) {
			return notFound();
		}
		return ok(views.html.index.render("Index", controllers.asMap().get(game)));
	}

	public static Result start() {
		ArrayNode json = (ArrayNode) request().body().asJson();

		if (json == null) {
			logger.error("No Json data in start()");
			return badRequest("Expecting Json data");
		}

		Map<String, PlayerIcon> players = new HashMap<String, PlayerIcon>();
		Iterator<JsonNode> elements = json.elements();

		while (elements.hasNext()) {
			ObjectNode player = (ObjectNode) elements.next();

			String playerName = player.get("name").asText();
			String playerIcon = player.get("figure").asText();

			players.put(playerName, PlayerIcon.valueOf(playerIcon.toUpperCase()));
		}

		if (players.isEmpty()) {
			logger.error("could not start game... players: " + players);
			return badRequest("Some error during initialization!");
		}

		startNewGame(players);

		return ok();
	}

	private static boolean startNewGame(Map<String, PlayerIcon> player) {
		IController game = new de.htwg.monopoly.controller.impl.Controller(IMonopolyUtil.FIELD_SIZE);
		game.startNewGame(player);
		controllers.put("" + game.hashCode(), game);
		prisonRollFlags.put("" + game.hashCode(), false);
		session("game", "" + game.hashCode());
		

		logger.info("New Game started");
		// start the game and begin with first player
		controllers.asMap().get((session("game"))).startNewGame(player);

		return true;
	}
	
	private static boolean startNewGameNetwork(Map<String, PlayerIcon> player, String gameName) {
		IController game = new de.htwg.monopoly.controller.impl.Controller(IMonopolyUtil.FIELD_SIZE);
		
		// starts the game
		game.startNewGame(player);
		
		controllers.put("" + game.hashCode(), game);
		prisonRollFlags.put("" + game.hashCode(), false);
		session("game", "" + game.hashCode());
		
		pendingGames.asMap().get(gameName).addID(game.hashCode());
		

		logger.info("New Game started");
		// start the game and begin with first player
		controllers.asMap().get(Integer.toString(game.hashCode())).startNewGame(player);

		return true;
	}


	public static Result getCurrentGameId() {
		return ok(toJson("id",session("game")));
	}
	
	public static Result rollDice() {
		String currentSession = getSession();

		logger.debug("User started turn: ");

		if (prisonRollFlags.asMap().get(currentSession)) {
			logger.debug("tries to roll dice to redeem");
			return handlePrisonRoll();
		}

		if (controllers.asMap().get(currentSession).getCurrentPlayer().isInPrison()) {
			logger.debug("is in prison and needs to select a option");
			return ok(getMessage("Sie sitzen im Gefängnis.. bitte wählen Sie eine entsprechende Gefängnis Option aus..."));
		}

		if (!controllers.asMap().get(currentSession).isCorrectOption(UserAction.START_TURN)) {
			logger.debug("user choose wrong action");
			return ok(getMessage("Aktion nicht verfügbar"));
		}

		logger.debug("user starts his turn by throwing the dice and moving");
		controllers.asMap().get(currentSession).startTurn();
		return ok(getMessage());
	}

	public static Result getDiceResult() {
		JSONObject dice = new JSONObject();
		dice.put("dice1", "" + controllers.asMap().get(getSession()).getDice().getDice1());
		dice.put("dice2", "" + controllers.asMap().get(getSession()).getDice().getDice2());
		return ok(dice.toJSONString());
	}

	private static Result handlePrisonRoll() {
		String currentSession = getSession();

		if (!controllers.asMap().get(currentSession).isCorrectOption(UserAction.ROLL_DICE)) {
			prisonRollFlags.put(currentSession, false);
			return ok(getMessage("Aktion nicht verfügbar"));
		}

		controllers.asMap().get(getSession()).rollDiceToRedeem();

		if (!controllers.asMap().get(getSession()).getCurrentPlayer().isInPrison()) {
			prisonRollFlags.put(currentSession, false);
		}

		return ok(getMessage());
	}

	private static String getMessage() {
		JSONObject message = new JSONObject();
		message.put("msg", controllers.asMap().get(getSession()).getMessage());
		return msg(message.toJSONString());
	}


	public static Result getCurrentPlayerAsJSON() {
		JSONObject message = new JSONObject();
		message.put("name", controllers.asMap().get(getSession()).getCurrentPlayer().getName());
		return ok(message.toJSONString());
	}

	public static Result endTurn() {
		if (!controllers.asMap().get(getSession()).isCorrectOption(UserAction.END_TURN)) {
			// wrong input, option not available
			return ok(getMessage("Aktion nicht verfügbar"));
		}
		controllers.asMap().get(getSession()).endTurn();
		return ok(getMessage());
	}

	public static Result buy() {
		if (!controllers.asMap().get(getSession()).isCorrectOption(UserAction.BUY_STREET)) {
			// wrong input, option not available
			return ok(getMessage("Aktion nicht verfügbar"));
		}

		if (controllers.asMap().get(getSession()).buyStreet()) {
			return ok(getMessage());
		}
		return ok(getMessage("Kein Geld um die Straße zu kaufen!!!"));

	}

	public static Result endGame() {
		controllers.asMap().get(getSession()).endTurn();
		controllers.asMap().get(getSession()).exitGame();
		return redirect("END GAME");
	}

	public static Result prisonBuy() {
		logger.debug("tries to redeem with money");
		if (!controllers.asMap().get(getSession()).isCorrectOption(UserAction.REDEEM_WITH_MONEY)) {
			// wrong input, option not available
			return ok(getMessage("Aktion nicht verfügbar"));
		}

		logger.debug("redeem with money action available ");

		if (controllers.asMap().get(getSession()).redeemWithMoney()) {
			logger.debug("enough money --> sets free");
			return ok(getMessage("Freigekauft"));
		} else {
			logger.debug("not enough money --> still in prison");
			return ok(getMessage("Nicht genug Geld!"));
		}
	}

	public static Result prisonCard() {
		if (!controllers.asMap().get(getSession()).isCorrectOption(UserAction.REDEEM_WITH_CARD)) {
			// wrong input, option not available
			return ok(getMessage("Aktion nicht verfügbar"));
		}
		if (controllers.asMap().get(getSession()).redeemWithCard()) {
			return ok(getMessage("Freikarte eingesetzt"));
		} else {
			return ok(getMessage("Keine Freikarte vorhanden.."));
		}
	}

	public static Result prisonRoll() {
		String currentSession = getSession();

		if (!controllers.asMap().get(currentSession).isCorrectOption(UserAction.REDEEM_WITH_DICE)) {
			// wrong input, option not available
			return ok(getMessage("Aktion nicht verfügbar"));
		}
		prisonRollFlags.put(currentSession, true);

		if (controllers.asMap().get(currentSession).redeemWithDice()) {
			return ok(getMessage());
		} else {
			return ok(getMessage("Irgendwas ist gehörig schief gelaufen..."));
		}

	}

	public static Result drawCard() {
		if (!controllers.asMap().get(getSession()).isCorrectOption(UserAction.DRAW_CARD)) {
			// wrong input, option not available
			return ok(getMessage("Aktion nicht verfügbar"));
		}
		controllers.asMap().get(getSession()).drawCard();

		return ok(getMessage());

	}

	public static Result checkAnswer(Boolean answer) {
		if (!controllers.asMap().get(getSession()).isCorrectOption(UserAction.REDEEM_WITH_QUESTION)) {
			// wrong input, option not available
			return ok(getMessage("Aktion nicht verfügbar"));
		}
		if (controllers.asMap().get(getSession()).checkPlayerAnswer(answer)) {
			return ok(getMessage("Korrekte Antwort. Sie sind frei gekommen"));
		} else {
			return ok(getMessage("Leider falsche Antwort, der nächste Spieler ist dran."));
		}

	}

	public static Result getQuestion() {
		JSONObject message = new JSONObject();
		message.put("question", controllers.asMap().get(getSession()).getPrisonQuestion());

		return ok(message.toJSONString());
	}

	public static Result update() {
		return ok(getPlayersAsJSON());
	}

	public static Result getPossibleOptions() {
		JSONObject options = new JSONObject();
		int i = 0;
		for (UserAction action : controllers.asMap().get(getSession()).getOptions()) {
			options.put("" + i, "" + action);
			i++;
		}
		return ok(options.toJSONString());
	}

	public static String msg(String msg) {
		lastMessage.put(getSession(), msg);
		return msg;
	}

	public static Result getLastMessage() {
		return ok(lastMessage.asMap().get(getSession()));
	}

	public static String getPlayersAsJSON() {
		int numberOfPlayer = controllers.asMap().get(getSession()).getNumberOfPlayers();
		JSONObject all[] = new JSONObject[numberOfPlayer];

		for (int i = 0; i < numberOfPlayer; i++) {
			Player currentPlayer = controllers.asMap().get(getSession()).getPlayer(i);
			all[i] = new JSONObject();
			all[i].put("name", currentPlayer.getName());
			all[i].put("pos", currentPlayer.getPosition());
			all[i].put("budget", currentPlayer.getBudget());
			all[i].put("pic", currentPlayer.getFigure().toLowerCase());
			JSONArray ownershipt = new JSONArray();
			for (IFieldObject field : currentPlayer.getOwnership()) {
				ownershipt.add(field.toString());
			}
			all[i].put("ownership", " " + ownershipt);

			if (currentPlayer.hasPrisonFreeCard()) {
				all[i].put("prisoncard", "ja");
			} else {
				all[i].put("prisoncard", "nein");
			}
		}

		JSONObject allPlayer = new JSONObject();
		for (int i = 0; i < all.length; i++) {
			allPlayer.put(i, all[i]);
		}
		return allPlayer.toString();

	}

	public static Result getGameInstances() {
		Integer numberOfGames = (int) pendingGames.size();
		JSONObject games[] = new JSONObject[numberOfGames];
		int i = 0;
		for (String current : pendingGames.asMap().keySet()) {

			String nameOfGame = pendingGames.asMap().get(current).getName();
			int numberOfPlayer = pendingGames.asMap().get(current).getPlayerCount();

			JSONArray tempPlayers = new JSONArray();
			// add joined player to json array
			for (String currentPlayer : pendingGames.asMap().get(current).getPlayers().keySet()) {

				// temp player object
				JSONObject tmpPlayer = new JSONObject();
				tmpPlayer.put("name", currentPlayer);
				// TODO: maybe bug: the Icon is now saved only in lowercase
				// letters...
				tmpPlayer.put("figure", pendingGames.asMap().get(current).getPlayers().get(currentPlayer).toString().toLowerCase());

				// add player object to array
				tempPlayers.add(tmpPlayer);
			}

			// fill array with placeholders
			int joinedPlayer = pendingGames.asMap().get(current).getPlayers().size();
			for (int k = joinedPlayer; k < IMonopolyUtil.MAX_NUMBER_OF_PLAYER; k++) {
				JSONObject tmpPlayer = new JSONObject();
				if (k < numberOfPlayer) {
					tmpPlayer.put("name", "offen");
					tmpPlayer.put("figure", "");
				} else {
					tmpPlayer.put("name", "X");
					tmpPlayer.put("figure", "");
				}
				// add player object to arry
				tempPlayers.add(tmpPlayer);
			}

			// add array to game json object
			games[i] = new JSONObject();
			games[i].put("players", tempPlayers);
			games[i].put("numberOfPlayer", numberOfPlayer);
			games[i].put("name", nameOfGame);
			i++;
		}

		// add all game json objects to an array
		JSONArray allGames = new JSONArray();
		for (int j = 0; j < games.length; j++) {
			allGames.add(games[j]);
		}

		return ok(allGames.toJSONString());
	}

	public static Result createGameInstance() {

		JsonNode json = request().body().asJson();
		logger.error("create new pending game");

		if (json == null) {
			logger.error("No Json data in createGameInstances");
			return badRequest("Expecting Json data");
		}

		String gameName = json.get("name").asText();

		// error handling
		if (pendingGames.asMap().get(gameName) != null) {
			logger.error("Game not created: Already exists");
			return badRequest("game already exist");
		}

		Integer numberOfPlayer = json.get("numberOfPlayer").asInt();

		JsonNode playerNode = json.get("players");
		Iterator<JsonNode> elements = playerNode.elements();

		Map<String, PlayerIcon> players = new HashMap<String, PlayerIcon>();

		while (elements.hasNext()) {
			JsonNode playerElement = elements.next();

			String name = playerElement.get("name").asText();
			String icon = playerElement.get("figure").asText();
			players.put(name, PlayerIcon.valueOf(icon.toUpperCase()));

			// only add the first player
			break;
		}

		// add game instance to pending games
		PendingGame aPendingGame = new PendingGame(gameName, numberOfPlayer, players);

		pendingGames.put(aPendingGame.getName(), aPendingGame);

		session("gameCreated", "true");

		return ok();
	}

	public static Result addPlayertoGameInstance() {

		JsonNode json = request().body().asJson();

		if (json == null) {
			logger.error("No Json data in createGameInstances");
			return badRequest("Expecting Json data");
		}

		String gameName = json.get("gameName").asText();

		PendingGame pendingGame = pendingGames.asMap().get(gameName);
		// error handling
		if (pendingGame == null) {
			logger.error("Can not join game: Game does not exist");
			return badRequest("game does not exist");
		}

		JsonNode playerNode = json.get("player");

		String playerName = playerNode.get("name").asText();
		String playerIcon = playerNode.get("figure").asText();

		HashMap<String, PlayerIcon> player = new HashMap<String, PlayerIcon>();
		player.put(playerName, PlayerIcon.valueOf(playerIcon.toUpperCase()));

		// add player to pending games
		if (pendingGame.hasSpace()) {
			pendingGame.addPlayer(player);
		} else {
			logger.error("game is full");
			return badRequest("game is full");
		}

		// TODO error handling

		session("gameJoined", "true");
		return ok();
	}

	public static Result createdGame() {
		if ("true".equals(session("gameCreated")) && pendingGames.asMap().size() != 0) {
			return ok();
		}
		return badRequest();

	}

	public static Result joinedGame() {

		for (String current : pendingGames.asMap().keySet()) {
			if (pendingGames.asMap().get(current).getPlayers().size() < 2) {
				return badRequest();
			}
		}

		if ("true".equals(session("gameJoined"))) {
			return ok();
		}
		
		return badRequest();

	}
	
	public static Result isFull(String gameName) {
		PendingGame pendingGame = pendingGames.asMap().get(gameName);
		if (pendingGame == null) {
			return badRequest();
		}
		
		if (pendingGame.hasSpace()) {
			return badRequest();
		} else {
			return ok();
		}
	}
	
	public static Result startGame(String gameName) {
		PendingGame pendingGame = pendingGames.asMap().get(gameName);
		if (pendingGame == null) {
			return badRequest();
		}
		
		startNewGameNetwork(pendingGame.getPlayers(), gameName);

		return ok(views.html.index.render("Index", controllers.asMap().get(getSession())));
	}
	
	public static Result getJoinGameID(String gameName) {
		PendingGame pendingGame = pendingGames.asMap().get(gameName);
		if (pendingGame == null) {
			return badRequest();
		}
		
		if (pendingGame.getID() == 0) {
			return badRequest();
		}
		String ID = ((Integer) pendingGame.getID()).toString();
		
		JSONObject message = new JSONObject();
		message.put("ID", ID);
		return ok(message.toJSONString());
	}

	/**
	 * ************************ websockets ********************************
	 */

	public static WebSocket<String> connectWebSocket(final String game) {
		return new WebSocket<String>() {

			public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {
				observer.put(game, new MonopolyObserver(controllers.asMap().get(game), out));
			}

		};
	}
	
	
	/* 
    ************************** help functions ***********************
    * */
	private static String toJson(String key, String value) {
		JSONObject message = new JSONObject();
		message.put(key, value);
		return message.toJSONString();
	}


	private static String getMessage(String msg) {
		JSONObject message = new JSONObject();
		message.put("msg", msg);
		return msg(message.toJSONString());
	}
	
	private static String getSession() {
		final Set<Map.Entry<String,String[]>> entries = request().queryString().entrySet();
		for (Map.Entry<String,String[]> entry : entries) {
			final String key = entry.getKey();
			final String value = Arrays.toString(entry.getValue());
			Logger.debug(key + " " + value);
		}
		Logger.debug(request().getQueryString("session"));
		return request().getQueryString("session");
	}

	
}


