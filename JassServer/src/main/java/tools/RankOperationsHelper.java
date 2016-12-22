package tools;


import java.util.List;

import model.Player;

/**
 * Class used to perform arithmetic operations on
 */
public final class RankOperationsHelper {

    /**
     * Computes and returns the average Rank of a list of players.
     * <br>
     * If the list is empty, the Rank will be 0.
     *
     * @param players The list of players
     * @return The average Rank of the players
     */
    public static int averageRank(List<Player> players) {
        int total = 0;
        int numPlayers = 0;

        for (Player player : players) {
            total += player.getQuote();
            ++numPlayers;
        }

        return numPlayers == 0 ? total : total / numPlayers; // TODO: use ceiling function?
    }

}
