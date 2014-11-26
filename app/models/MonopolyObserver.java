package models;

import controllers.Application;
import de.htwg.monopoly.controller.IController;
import de.htwg.monopoly.observer.IObserver;
import de.htwg.monopoly.util.GameStatus;
import play.mvc.WebSocket;
import play.mvc.WebSocket.Out;

public class MonopolyObserver implements IObserver {
	
	private Out<String> out;
	private IController controller;


	public MonopolyObserver(IController controller, WebSocket.Out<String> out) {
		controller.addObserver(this);
		this.controller = controller;
		this.out = out;
	}

    @Override
    public void update(GameStatus gameStatus) {
        out.write(Application.getPlayersAsJSON());
        System.out.println("WUI was updated");
    }

    @Override
	public void update(int i) {
		out.write(Application.getPlayersAsJSON());
		System.out.println("WUI was updated");
	}

}
