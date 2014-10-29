package controllers;

import de.htwg.monopoly.controller.IController;
import de.htwg.monopoly.game.Monopoly;
import de.htwg.monopoly.observer.Event;
import de.htwg.monopoly.observer.IObserver;
import de.htwg.monopoly.util.IMonopolyUtil;
import de.htwg.monopoly.util.MonopolyUtils;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller implements IObserver{

	static IController controller;

	public static Result index() {

		return ok(views.html.index.render("Hello Play Framework", controller));
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

		controller.startNewGame(number, names);

		controller.startTurn();

		return ok(views.html.index.render("Hello Play Framework " + number, controller));
	}
	
	public static Result rollDice() {
		
		controller.startTurn();
		
		
		return ok(views.html.index.render("", controller));
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

    

}
