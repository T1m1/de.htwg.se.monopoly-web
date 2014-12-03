package controllers;

import de.htwg.monopoly.controller.IController;
import de.htwg.monopoly.entities.IFieldObject;
import de.htwg.monopoly.entities.impl.Player;
import de.htwg.monopoly.game.Monopoly;
import de.htwg.monopoly.util.MonopolyUtils;
import de.htwg.monopoly.util.UserAction;
import models.MonopolyObserver;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Application extends Controller {

	static IController controller;

    public static Result welcome() {
        InputStream welcomePage;
        try {
            welcomePage = FileUtils.openInputStream(new File(
                    "app/views/welcome.html"));
        } catch (IOException e) {
            return ok("FAILURE");
        }

        if (welcomePage == null) {
            return ok("NO PAGE FOUND");
        }

        return ok(welcomePage).as("text/html");
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
		//controller.startNewGame(Arrays.asList(names));
        controller.startNewGame(names.length, names);

		return index();
	}

	public static Result rollDice() {
        if (controller != null && controller.getCurrentPlayer() != null && controller.getCurrentPlayer().isInPrison()) {
            return ok(getMessage("Sie sitzen im Gefängnis.. bitte wählen Sie eine entsprechende Gefängnis Option aus..."));
        }
        controller.startTurn();
        return ok(getMessage());
    }
    public static Result getDiceResult() {
        JSONObject dice  = new JSONObject();
        dice.put("dice1", ""+controller.getDice().getDice1());
        dice.put("dice2", ""+controller.getDice().getDice2());
        return ok(dice.toJSONString());
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

    public static Result prisonBuy() {
        if (controller.getOptions().contains(UserAction.REDEEM_WITH_MONEY)) {
            controller.redeemWithMoney();
            return ok(getMessage("Freigekauft"));
        }
        return ok(getMessage("Option nicht möglich"));
    }

    public static Result prisonCard() {
        if (controller.getOptions().contains(UserAction.REDEEM_WITH_CARD)) {
            controller.redeemWithCard();
            return ok(getMessage("Freikarte eingesetzt"));
        }
        return ok(getMessage("Keine Freikarte vorhanden.."));
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

    /**
     * ******************************** websockets ***************************************
     */

    public static WebSocket<String> connectWebSocket() {
        return new WebSocket<String>() {

            public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out) {
                new MonopolyObserver(controller, out);
            }
        };
    }
}
