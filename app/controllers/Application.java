package controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import de.htwg.monopoly.controller.IController;
import de.htwg.monopoly.entities.IFieldObject;
import de.htwg.monopoly.entities.impl.Player;
import de.htwg.monopoly.game.Monopoly;
import de.htwg.monopoly.observer.Event;
import de.htwg.monopoly.util.MonopolyUtils;
import models.MonopolyObserver;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;

public class Application extends Controller {

	static IController controller;

	public static Result welcome() {
		InputStream welcomePage = null;
		try {
			welcomePage = FileUtils.openInputStream(new File(
					"/app/views/welcome.html"));
		} catch (IOException e) {
			System.out.println("Failure to open file");
			e.printStackTrace();
		}

		return ok(welcomePage);
	}

	public static Result index() {

		return ok(views.html.index.render("Index", controller));
	}

	public static Result startGame(Integer number) {

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
		controller.startTurn();

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

	public static Result endTurn() {

		controller.endTurn();
		return ok(getMessage());
	}

	public static Result buy() {
		if (!controller.buyStreet()) {
			return ok(getMessage("Kein Geld um die Straße zu kaufen!!!"));
		}
		return ok(getMessage());
	}

	public static Result endGame() {

		controller.endTurn();
		controller.exitGame();

		return ok("END GAME");
	}

	public void update(Event arg0) {
		// TODO Auto-generated method stub
	}

	public void update(int arg0) {
		// TODO Auto-generated method stub

	}

	public static Result update() {
		return ok(getPlayersAsJSON());
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

	/*********************************** websockets ****************************************/

	public static WebSocket<String> connectWebSocket() {
		return new WebSocket<String>() {

			public void onReady(WebSocket.In<String> in,
					WebSocket.Out<String> out) {
				new MonopolyObserver(controller, out);
			}

		};
	}
}
