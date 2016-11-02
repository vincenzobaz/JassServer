package tools;


import java.util.List;

import model.Player;
import model.Rank;

/**
 * Class used to perform arithmetic operations on
 * {@link Rank Ranks}.
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
    public static Rank averageRank(List<Player> players) {
        Rank total = new Rank(0);
        int numPlayers = 0;

        for (Player player : players) {
            total = total.add(player.getRank());
            ++numPlayers;
        }

        return numPlayers == 0 ? total : new Rank(total.getRank() / numPlayers); // TODO: use ceiling function?
    }

}
