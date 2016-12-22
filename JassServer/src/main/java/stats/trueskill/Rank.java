package stats.trueskill;


import java.util.Objects;

/**
 * Class that represents a rank in a competition system.
 * It implements the Comparable interface.
 */
public class Rank {
    private Double mean;
    private Double standardDeviation;

    /**
     * Default constructor required for calls to DataSnapshot.getValue when using Firebase
     */
    public Rank() {

    }

    /**
     * Constructs a new Rank with the given value.
     *
     * @param mean The value of the rank
     */
    public Rank(double mean) {
        this.mean = mean;
    }

    /**
     * Construct a new rank with the mean and standardDeviation given
     * @param mean
     * @param standardDeviation
     */
    public Rank(double mean, double standardDeviation) {
        this.mean = mean;
        this.standardDeviation = standardDeviation;
    }

    public void copy(Rank rank) {
        mean = rank.getMean();
        standardDeviation = rank.getStandardDeviation();
    }
    /**
     * Getter for the rank.
     *
     * @return The rank value
     */
    public Double getMean() {
        return mean;
    }

    public Double getStandardDeviation() {
        return standardDeviation;
    }

    public int computeRank() {
        return (int) Math.ceil((mean - 3 * standardDeviation) * 10 + 1000);
    }

    public static Rank getDefaultRank() {
        return new Rank(GameInfo.defaultInitialMean, GameInfo.defaultInitialStandardDeviation);
    }
}
