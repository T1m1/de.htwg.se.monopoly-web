package controllers;

import de.htwg.monopoly.controller.IController;
import de.htwg.monopoly.game.Monopoly;
import play.mvc.Controller;
import play.mvc.Result;


public class Application extends Controller {

    static IController controller = Monopoly.getInstance().getController();

    public static Result index() {

        return ok(views.html.index.render("Hello Play Framework", controller));
    }
    
}
