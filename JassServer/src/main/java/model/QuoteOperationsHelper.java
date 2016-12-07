package model;


import java.util.List;

import model.Player;

/**
 * Class used to perform arithmetic operations on quotes
 */
public final class QuoteOperationsHelper {

    /**
     * Computes and returns the average Quote of a list of players.
     * <br>
     * If the list is empty, the Quote will be 0.
     *
     * @param players The list of players
     * @return The average Quote of the players
     */
    public static int averageQuote(List<Player> players) {
        int total = 0;
        int numPlayers = 0;

        for (Player player : players) {
            total += player.getQuote();
            ++numPlayers;
        }

        return numPlayers == 0 ? total : total / numPlayers;
    }

}
