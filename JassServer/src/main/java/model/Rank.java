package model;


import java.util.Objects;

/**
 * Class that represents a rank in a competition system.
 * It implements the Comparable interface.
 */
public class Rank implements Comparable<Rank> {

    private int rank;

    /**
     * Default constructor required for calls to DataSnapshot.getValue when using Firebase
     */
    public Rank() {
    }

    /**
     * Constructs a new Rank with the given value.
     *
     * @param rank The value of the rank
     */
    public Rank(int rank) {
        this.rank = rank;
    }

    /**
     * Getter for the rank.
     *
     * @return The rank value
     */
    public int getRank() {
        return rank;
    }

    /**
     * Provides a string representation for the Rank class.
     *
     * @return A string representation of the Rank class
     */
    @Override
    public String toString() {
        return Integer.toString(rank);
    }

    @Override
    public int compareTo(Rank o) {
        return ((Integer) rank).compareTo(o.rank);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (other == null || other.getClass() != this.getClass()) {
            return false;
        }
        return this.getRank() == ((Rank) other).getRank();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getRank());
    }

    /**
     * Computes and returns the sum of the current Rank and another.
     *
     * @param other The Rank to add to the current Rank
     * @return A new Rank with value equal to the sum of the other two
     */
    public Rank add(Rank other) {
        return new Rank(this.rank + other.rank);
    }

}
