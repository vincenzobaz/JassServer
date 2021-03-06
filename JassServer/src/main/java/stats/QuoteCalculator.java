package stats;

/**
 * @author vincenzobaz
 *         <p>
 *         Abstract class defining the interface of the objects used to compute a new rank. This is part
 *         of our Strategy pattern implementation.
 */
public abstract class QuoteCalculator {
    private UserStats stats;

    /**
     * Constructor
     *
     * @param stats the UserStats used to compute new value of rank.
     */
    public QuoteCalculator(UserStats stats) {
        this.stats = stats;
    }

    /**
     * Stats getter, needed by subclasses
     *
     * @return the stats for the user
     */
    protected UserStats getStats() {
        return this.stats;
    }

    /**
     * Computes a new rank.
     *
     * @return Rank the updated version of the rank.
     */
    public abstract int computeNewQuote();
}
