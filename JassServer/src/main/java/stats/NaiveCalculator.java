package stats;

import model.Rank;

/**
 * @author vincenzobaz
 *         Naive implementation of a rankcalculator which considers the rank to be equal to the
 *         number of won matches.
 */
public class NaiveCalculator extends RankCalculator {
    public NaiveCalculator(UserStats stats) {
        super(stats);
    }

    public Rank computeNewRank() {
        int won = getStats().getWonMatches();
        return new Rank(won);
    }
}
