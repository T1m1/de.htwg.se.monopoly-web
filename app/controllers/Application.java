package controllers;

import de.htwg.monopoly.controller.IController;
import de.htwg.monopoly.game.Monopoly;
import de.htwg.monopoly.util.GameStatus;
import de.htwg.monopoly.util.MonopolyUtils;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {

	static IController controller = Monopoly.getInstance().getController();

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
		controller.startNewGame(number, names);

		return index();
	}

	public static Result rollDice() {
		controller.startTurn();
		return index();
	}

	public static Result endTurn() {

		controller.endTurn();
		// TODO "print" events happened
		return index();
	}

	public static Result buy() {

		if (!controller.buyStreet()) {
			return ok(views.html.index.render(
					"Kein Geld um die Straße zu kaufen!!!", controller));
		}
		return index();
	}

	public static Result doIt(GameStatus status) {
		
		if (status == GameStatus.STOPPED) {
			return ok("COOOOOOL");
		}
		return ok("SCHEIIßßßßße");
	}
	public static Result endGame() {

		controller.endTurn();
		controller.exitGame();

		return ok("END GAME");
	}


}
