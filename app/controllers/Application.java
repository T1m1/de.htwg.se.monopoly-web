package controllers;

import de.htwg.monopoly.controller.IController;
import play.Logger;
import play.Logger.ALogger;
import de.htwg.monopoly.entities.IFieldObject;
import de.htwg.monopoly.entities.impl.Player;
import de.htwg.monopoly.game.Monopoly;
import de.htwg.monopoly.util.IMonopolyUtil;
import de.htwg.monopoly.util.MonopolyUtils;
import de.htwg.monopoly.util.PlayerIcon;
import de.htwg.monopoly.util.UserAction;
import models.MonopolyObserver;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;

import java.util.*;

public class Application extends Controller {

    private static Map<String, IController> controllers = new HashMap<String, IController>();
    private static Map<String, MonopolyObserver> observer = new HashMap<String, MonopolyObserver>();
    private static Map<String, String> lastMessage = new HashMap<String, String>();

	private static final ALogger logger = Logger.of(Application.class);
	private static boolean prisonRollFlag;



	public static Result welcome() {
		logger.debug("Welcome page loading");
		return ok(views.html.welcome.render(""));
	}

	public static Result index() {
		logger.debug("index site loading");
		return ok(views.html.index.render("Index", controllers.get(session("game"))));
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

			players.put(playerName,
					PlayerIcon.valueOf(playerIcon.toUpperCase()));
		}

		if (players.isEmpty()) {
			logger.error("could not start game... players: " + players);
			return badRequest("Some error during initialization!");
		}

		startNewGame(players);
		
		return ok(views.html.index.render("Index", controllers.get(session("game"))));

	}

	private static boolean startNewGame(Map<String, PlayerIcon> player) {

        IController game = new de.htwg.monopoly.controller.impl.Controller(IMonopolyUtil.FIELD_SIZE);
        game.startNewGame(player);
        controllers.put("" + game.hashCode(), game);
        session("game", "" + game.hashCode());

		// TODO start tui for each session

		logger.info("New Game started");
		// start the game and begin with first player
        controllers.get((session("game"))).startNewGame(player);

		return true;

	}


	public static Result rollDice() {
		
		logger.debug("User started turn: ");

		if (prisonRollFlag) {
			logger.debug("tries to roll dice to redeem");
			return handlePrisonRoll();
		}

		if (controller.getCurrentPlayer().isInPrison()) {
			logger.debug("is in prison and needs to select a option");
			return ok(getMessage("Sie sitzen im Gefängnis.. bitte wählen Sie eine entsprechende Gefängnis Option aus..."));
		}

		if (!controller.isCorrectOption(UserAction.START_TURN)) {
			logger.debug("user choose wrong action");
			return ok(getMessage("Aktion nicht verfügbar"));
		}

		logger.debug("user starts his turn by throwing the dice and moving");
		controller.startTurn();
		return ok(getMessage());
	}

	public static Result getDiceResult() {
		JSONObject dice = new JSONObject();
		dice.put("dice1", "" + controllers.get(session("game")).getDice().getDice1());
		dice.put("dice2", "" + controllers.get(session("game")).getDice().getDice2());
		return ok(dice.toJSONString());
	}

	private static Result handlePrisonRoll() {
		if (!controllers.get(session("game")).isCorrectOption(UserAction.ROLL_DICE)) {
			prisonRollFlag = false;
			return ok(getMessage("Aktion nicht verfügbar"));
		}

        controllers.get(session("game")).rollDiceToRedeem();

		if (!controllers.get(session("game")).getCurrentPlayer().isInPrison()) {
			prisonRollFlag = false;
		}

		return ok(getMessage());
	}

	private static String getMessage() {
		JSONObject message = new JSONObject();
		message.put("msg", controllers.get(session("game")).getMessage());
		return msg(message.toJSONString());
	}

	private static String getMessage(String msg) {
		JSONObject message = new JSONObject();
		message.put("msg", msg);
		return msg(message.toJSONString());
	}

	public static Result getCurrentPlayerAsJSON() {
		JSONObject message = new JSONObject();
		message.put("name", controllers.get(session("game")).getCurrentPlayer().getName());
		return ok(message.toJSONString());
	}

	public static Result endTurn() {
		if (!controllers.get(session("game")).isCorrectOption(UserAction.END_TURN)) {
			// wrong input, option not available
			return ok(getMessage("Aktion nicht verfügbar"));
		}
        controllers.get(session("game")).endTurn();
		return ok(getMessage());
	}

	public static Result buy() {
		if (!controllers.get(session("game")).isCorrectOption(UserAction.BUY_STREET)) {
			// wrong input, option not available
			return ok(getMessage("Aktion nicht verfügbar"));
		}

		if (controllers.get(session("game")).buyStreet()) {
			return ok(getMessage());
		}
		return ok(getMessage("Kein Geld um die Straße zu kaufen!!!"));

	}

	public static Result endGame() {

        controllers.get(session("game")).endTurn();
        controllers.get(session("game")).exitGame();

		return ok("END GAME");
	}

	public static Result prisonBuy() {
		logger.debug("tries to redeem with money");
		if (!controllers.get(session("game")).isCorrectOption(UserAction.REDEEM_WITH_MONEY)) {
			// wrong input, option not available
			return ok(getMessage("Aktion nicht verfügbar"));
		}

		logger.debug("redeem with money action available ");
		
		if (controllers.get(session("game")).redeemWithMoney()) {
			logger.debug("enough money --> sets free");
			return ok(getMessage("Freigekauft"));
		} else {
			logger.debug("not enough money --> still in prison");
			return ok(getMessage("Nicht genug Geld!"));
		}
	}

	public static Result prisonCard() {
		if (!controllers.get(session("game")).isCorrectOption(UserAction.REDEEM_WITH_CARD)) {
			// wrong input, option not available
			return ok(getMessage("Aktion nicht verfügbar"));
		}
		if (controllers.get(session("game")).redeemWithCard()) {
			return ok(getMessage("Freikarte eingesetzt"));
		} else {
			return ok(getMessage("Keine Freikarte vorhanden.."));
		}
	}

	public static Result prisonRoll() {
		if (!controllers.get(session("game")).isCorrectOption(UserAction.REDEEM_WITH_DICE)) {
			// wrong input, option not available
			return ok(getMessage("Aktion nicht verfügbar"));
		}
		prisonRollFlag = true;

		if (controllers.get(session("game")).redeemWithDice()) {
			return ok(getMessage());
		} else {
			return ok(getMessage("Irgendwas ist gehörig schief gelaufen..."));
		}

	}

	public static Result checkAnswer(Boolean answer) {

		if (!controllers.get(session("game")).isCorrectOption(UserAction.REDEEM_WITH_QUESTION)) {
			// wrong input, option not available
			return ok(getMessage("Aktion nicht verfügbar"));
		}
		if (controllers.get(session("game")).checkPlayerAnswer(answer)) {
			return ok(getMessage("Korrekte Antwort. Sie sind frei gekommen"));
		} else {
			return ok(getMessage("Leider falsche Antwort, der nächste Spieler ist dran."));
		}

	}

	public static Result getQuestion() {

		JSONObject message = new JSONObject();
		message.put("question", controllers.get(session("game")).getPrisonQuestion());

		return ok(message.toJSONString());

	}

	public static Result update() {
		return ok(getPlayersAsJSON());
	}

	public static Result getPossibleOptions() {
		JSONObject options = new JSONObject();
		int i = 0;
		for (UserAction action : controllers.get(session("game")).getOptions()) {
			options.put("" + i, "" + action);
			i++;
		}
		return ok(options.toJSONString());
	}

	public static String msg(String msg) {
		lastMessage.put(session("game"), msg);
		return msg;
	}

	public static Result getLastMessage() {
		return ok(lastMessage.get(session("game")));
	}

	public static String getPlayersAsJSON() {
		int numberOfPlayer = controllers.get(session("game")).getNumberOfPlayers();
		JSONObject all[] = new JSONObject[numberOfPlayer];

		for (int i = 0; i < numberOfPlayer; i++) {
			Player currentPlayer = controllers.get(session("game")).getPlayer(i);
			all[i] = new JSONObject();
			all[i].put("name", currentPlayer.getName());
			all[i].put("pos", currentPlayer.getPosition());
			all[i].put("budget", currentPlayer.getBudget());
			all[i].put("pic", currentPlayer.getFigure());
			JSONArray ownershipt = new JSONArray();
			for (IFieldObject field : currentPlayer.getOwnership()) {
				ownershipt.add(field.toString());
			}
			all[i].put("ownership", " " + ownershipt);
			
			if (currentPlayer.hasPrisonFreeCard()){
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
		JSONObject message = new JSONObject();
		message.put("id", "gameinstancename");

		return ok(message.toJSONString());
	}
	public static Result createGameInstance(){
			
		// add game instance if not already present
		return ok();
	}
	public static Result addPlayertoGameInstance(Integer id) {
		
		
		// add player to game instance, if not already present
		return ok();
	}

	/**
	 * ************************ websockets ********************************
	 */

	public static WebSocket<String> connectWebSocket(String game) {
		return new WebSocket<String>() {

			public void onReady(WebSocket.In<String> in,
					WebSocket.Out<String> out) {
                observer.put(game, new MonopolyObserver(controllers.get(game), out));
			}

		};
	}


}
