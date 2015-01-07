/**
 * 
 */
package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.Logger;
import play.Logger.ALogger;
import de.htwg.monopoly.entities.impl.Player;
import de.htwg.monopoly.util.PlayerIcon;

/**
 * @author stgorenf
 *
 */
public class PendingGame {

	private String name;
	private int playerCount;
	private Map<String, PlayerIcon> playerNames;

	private static final ALogger logger = Logger.of(PendingGame.class);

	/**
	 * 
	 */
	public PendingGame(String gameName, int numberOfPlayer,
			Map<String, PlayerIcon> firstPlayer) {
		this.name = gameName;
		this.playerCount = numberOfPlayer;
		this.playerNames = new HashMap<String, PlayerIcon>();
		this.playerNames.putAll(firstPlayer);
	}

	public String getName() {
		return name;
	}

	public int getPlayerCount() {
		return playerCount;
	}

	public Map<String, PlayerIcon> getPlayers() {
		return playerNames;
	}

	public boolean addPlayer(Map<String, PlayerIcon> playerToAdd) {
		if (playerNames.size() >= playerCount) {
			logger.error("unable to add player to pending game");
			return false;
		} else {
			playerNames.putAll(playerToAdd);
			return true;
		}
	}

	/**
	 * @return if there is space left for another player in this game
	 */
	public boolean hasSpace() {
		return (playerNames.size() < playerCount);
	}

}
