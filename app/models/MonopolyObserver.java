package models;

import controllers.Application;
import de.htwg.monopoly.controller.IController;
import de.htwg.monopoly.entities.IFieldObject;
import de.htwg.monopoly.entities.impl.Player;
import de.htwg.monopoly.observer.IObserver;
import de.htwg.monopoly.util.GameStatus;
import de.htwg.monopoly.util.UserAction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import play.mvc.WebSocket;
import play.mvc.WebSocket.Out;

public class MonopolyObserver implements IObserver {

    private Out<String> out;
    private IController controller;
    private String lastMessage;
    private String currentPlayer;
    private String gameName;
    private int indexOfNextPlayer;
    private boolean changePlayer;
    private boolean isMultiGame;


    public MonopolyObserver(IController controller, WebSocket.Out<String> out, boolean isMultiGame) {
        controller.addObserver(this);
        this.controller = controller;
        this.out = out;
        this.isMultiGame = isMultiGame;
        this.gameName = Application.getGameNameOf("" + controller.hashCode());
        out.write(getGameInfoAsJSON());
    }

    public void update(GameStatus gameStatus) {
        lastMessage = controller.getMessage();
        if (GameStatus.AFTER_TURN.equals(gameStatus)) {
            lastMessage = controller.getPlayer(indexOfNextPlayer).getName() + " ist dran!";
        }
        out.write(getGameInfoAsJSON());

    }

    public void update(int i) {

    }

    private String getGameInfoAsJSON() {
        JSONObject game = new JSONObject();
        game.put("players", getPlayersAsJSON());
        game.put("buttons", getPossibleOptionsAsJSON());

        JSONObject dice = new JSONObject();
        dice.put("dice1", "" + ((controller.getDice().getDice1() == 0) ? 6 : controller.getDice().getDice1()));
        dice.put("dice2", "" + ((controller.getDice().getDice2() == 0) ? 6 : controller.getDice().getDice2()));
        game.put("dices", dice);

        game.put("msg", lastMessage);
        JSONObject player = new JSONObject();
        player.put("name", controller.getCurrentPlayer().getName());
        game.put("currentPlayer", player);

        game.put("currentPlayerName", controller.getPlayer(indexOfNextPlayer).getName());
        game.put("changePlayer", Boolean.toString(changePlayer));
        game.put("isMultiGame", isMultiGame);
        game.put("gameName", gameName);

        return game.toJSONString();
    }

    private String getPlayersAsJSON() {
        int numberOfPlayer = controller.getNumberOfPlayers();
        JSONObject all[] = new JSONObject[numberOfPlayer];

        for (int i = 0; i < numberOfPlayer; i++) {
            Player currentPlayer = controller.getPlayer(i);
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

    private String getPossibleOptionsAsJSON() {
        JSONObject options = new JSONObject();
        int i = 0;
        for (UserAction action : controller.getOptions()) {
            options.put("" + i, "" + action);
            i++;
        }
        return options.toJSONString();

    }

    public void setIndexOfNextPlayer(int indexOfNextPlayer) {
        this.indexOfNextPlayer = indexOfNextPlayer;
        //  this.lastMessage = controller.getPlayer(indexOfNextPlayer).getName() + " du bist dran!";
        this.currentPlayer = controller.getPlayer(indexOfNextPlayer).getName();
        changePlayer = true;
    }

    public void setChangePlayer(boolean value) {
        this.changePlayer = value;
    }

    public void setLastMessage(String currentMessage) {
        this.lastMessage = currentMessage;
    }

    public String getLastMessage() {
        return this.lastMessage;
    }
}
