package controllers;

import de.htwg.monopoly.controller.IController;
import de.htwg.monopoly.entities.IFieldObject;
import de.htwg.monopoly.entities.impl.Player;
import de.htwg.monopoly.game.Monopoly;
import de.htwg.monopoly.util.MonopolyUtils;
import de.htwg.monopoly.util.PlayerIcon;
import de.htwg.monopoly.util.UserAction;
import models.MonopolyObserver;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Application extends Controller {

	static IController controller;
	private static boolean prisonRollFlag;

	public static Result welcometest() {
		InputStream welcomePage;
		try {
			welcomePage = FileUtils.openInputStream(new File(
					"app/views/welcome.scala.html"));
		} catch (IOException e) {
			return ok("FAILURE");
		}

		if (welcomePage == null) {
			return ok("NO PAGE FOUND");
		}

		return ok(welcomePage).as("text/html");
	}

	public static Result welcome() {
		return ok(views.html.welcome.render("Test"));
	}

	public static Result index() {
		return ok(views.html.index.render("Index", controller));
	}

	public static Result start() {

		ArrayNode json = (ArrayNode) request().body().asJson();

		if (json == null) {
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

		if (!startNewGame(players)) {
			return badRequest("Some error during initialization!");
		}

		return redirect(routes.Application.index());

	}

	private static boolean startNewGame(Map<String, PlayerIcon> player) {
		controller = Monopoly.getInstance().getController();
		// start logger
		Monopoly.getInstance().getTextUI().printInitialisation();

		// start the game and begin with first player
		controller.startNewGame(player);

		return true;

	}

	public static Result startGame(Integer number) {

		// is a singleton, needs to be handled
		controller = Monopoly.getInstance().getController();

		// start logger
		Monopoly.getInstance().getTextUI().printInitialisation();

		// check if a correct number of players is committed
		if (!MonopolyUtils.verifyPlayerNumber(number)) {
			return ok(views.html.index.render(
					"Wrong number of players entered!", controller));
		}

		// fill it for now with predefined strings TODO: change implementation
		String[] names = new String[number];
		for (int i = 0; i < names.length; i++) {
			names[i] = "Player " + i;
		}

		// start the game and begin with first player
		controller.startNewGame(Arrays.asList(names));

		return index();
	}

	public static Result rollDice() {

		if (prisonRollFlag) {
			return handlePrisonRoll();
		}

		if (controller.getCurrentPlayer().isInPrison()) {
			return ok(getMessage("Sie sitzen im Gefängnis.. bitte wählen Sie eine entsprechende Gefängnis Option aus..."));
		}

		if (!controller.isCorrectOption(UserAction.START_TURN)) {
			// wrong input, option not available
			return ok(getMessage("Aktion nicht verfügbar"));
		}

		controller.startTurn();
		return ok(getMessage());
	}

	public static Result getDiceResult() {
		JSONObject dice = new JSONObject();
		dice.put("dice1", "" + controller.getDice().getDice1());
		dice.put("dice2", "" + controller.getDice().getDice2());
		return ok(dice.toJSONString());
	}

	private static Result handlePrisonRoll() {
		if (!controller.isCorrectOption(UserAction.ROLL_DICE)) {
			prisonRollFlag = false;
			return ok(getMessage("Aktion nicht verfügbar"));
		}

		controller.rollDiceToRedeem();

		if (!controller.getCurrentPlayer().isInPrison()) {
			prisonRollFlag = false;
		}

		return ok(getMessage());
	}

	private static String getMessage() {
		JSONObject message = new JSONObject();
		message.put("msg", controller.getMessage());
		return message.toJSONString();
	}

	private static String getMessage(String msg) {
		JSONObject message = new JSONObject();
		message.put("msg", msg);
		return message.toJSONString();
	}

	public static Result getCurrentPlayerAsJSON() {
		JSONObject message = new JSONObject();
		message.put("name", controller.getCurrentPlayer().getName());
		return ok(message.toJSONString());
	}

	public static Result endTurn() {
		if (!controller.isCorrectOption(UserAction.END_TURN)) {
			// wrong input, option not available
			return ok(getMessage("Aktion nicht verfügbar"));
		}
		controller.endTurn();
		return ok(getMessage());
	}

	public static Result buy() {
		if (!controller.isCorrectOption(UserAction.BUY_STREET)) {
			// wrong input, option not available
			return ok(getMessage("Aktion nicht verfügbar"));
		}

		if (controller.buyStreet()) {
			return ok(getMessage());
		}
		return ok(getMessage("Kein Geld um die Straße zu kaufen!!!"));

	}

	public static Result endGame() {

		controller.endTurn();
		controller.exitGame();

		return ok("END GAME");
	}

	public static Result prisonBuy() {
		if (!controller.isCorrectOption(UserAction.REDEEM_WITH_MONEY)) {
			// wrong input, option not available
			return ok(getMessage("Aktion nicht verfügbar"));
		}

		if (controller.redeemWithMoney()) {
			return ok(getMessage("Freigekauft"));
		} else {
			return ok(getMessage("Nicht genug Geld!"));
		}
	}

	public static Result prisonCard() {
		if (!controller.isCorrectOption(UserAction.REDEEM_WITH_CARD)) {
			// wrong input, option not available
			return ok(getMessage("Aktion nicht verfügbar"));
		}
		if (controller.redeemWithCard()) {
			return ok(getMessage("Freikarte eingesetzt"));
		} else {
			return ok(getMessage("Keine Freikarte vorhanden.."));
		}
	}

	public static Result prisonRoll() {
		if (!controller.isCorrectOption(UserAction.REDEEM_WITH_DICE)) {
			// wrong input, option not available
			return ok(getMessage("Aktion nicht verfügbar"));
		}
		prisonRollFlag = true;

		if (controller.redeemWithDice()) {
			return ok(getMessage());
		} else {
			return ok(getMessage("Irgendwas ist gehörig schief gelaufen..."));
		}

	}

	public static Result update() {
		return ok(getPlayersAsJSON());
	}

	public static Result getPossibleOptions() {
		JSONObject options = new JSONObject();
		int i = 0;
		for (UserAction action : controller.getOptions()) {
			options.put("" + i, "" + action);
			i++;
		}
		return ok(options.toJSONString());
	}

	public static String getPlayersAsJSON() {
		int numberOfPlayer = controller.getNumberOfPlayers();
		JSONObject all[] = new JSONObject[numberOfPlayer];

		for (int i = 0; i < numberOfPlayer; i++) {
			Player currentPlayer = controller.getPlayer(i);
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
		}

		JSONObject allPlayer = new JSONObject();
		for (int i = 0; i < all.length; i++) {
			allPlayer.put(i, all[i]);
		}
		return allPlayer.toString();

	}

	/**
	 * ******************************** websockets
	 * ***************************************
	 */

	public static WebSocket<String> connectWebSocket() {
		return new WebSocket<String>() {

			public void onReady(WebSocket.In<String> in,
					WebSocket.Out<String> out) {
				new MonopolyObserver(controller, out);
			}

		};
	}
}
