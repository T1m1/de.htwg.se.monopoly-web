package controllers;

import de.htwg.monopoly.controller.IController;
import de.htwg.monopoly.game.Monopoly;
import play.mvc.Controller;
import play.mvc.Result;


public class Application extends Controller {

    static IController controller = Monopoly.getInstance().getController();

    public static Result index() {
        controller.startNewGame(2, new String[]{"Udo", "Maier"});
        controller.startTurn();
        return ok(views.html.index.render("Hello Play Framework", controller));
    }

    public static Result endTurn() {
        controller.endTurn();
        return ok(views.html.index.render("Hello Play Framework", controller));
    }
    
}
