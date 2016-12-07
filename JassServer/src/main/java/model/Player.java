package model;


import java.util.Objects;

/**
 * Class representing a player, identified by his/her name, sciper ID,
 * and quote in the scoreboard.
 */
public class Player {

    private PlayerID id;
    private String lastName;
    private String firstName;
    private int quote;

    /**
     * Default constructor required for calls to DataSnapshot.getValue when using Firebase
     */
    public Player() {
    }

    /**
     * Constructs a new Player.
     *
     * @param id        The ID of the player (sciper)
     * @param lastName  The last name of the player
     * @param firstName The first name of the player
     * @param quote     The quote of the player
     */
    public Player(PlayerID id, String lastName, String firstName, int quote) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.quote = quote;
    }

    /**
     * Constructs a new Player with a default quote.
     *
     * @param id        The ID of the player (sciper)
     * @param lastName  The last name of the player
     * @param firstName The first name of the player
     */
    public Player(PlayerID id, String lastName, String firstName) {
        this(id, lastName, firstName, 0);
    }

    /**
     * Getter for the ID of the player.
     *
     * @return The ID of the player
     */
    public PlayerID getID() {
        return id;
    }

    /**
     * Getter for the last name of the player.
     *
     * @return The last name of the player
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Getter for the first name of the player.
     *
     * @return The first name of the player
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Getter for the quote of the player.
     *
     * @return The quote of the player
     */
    public int getQuote() {
        return quote;
    }

    /**
     * Returns the string representation of a player as his/her full name.
     *
     * @return The full name of the player
     */
    @Override
    public String toString() {
        return firstName + ' ' + lastName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (other == null || other.getClass() != this.getClass()) {
            return false;
        }

        Player otherPlayer = (Player) other;
        return this.id.equals(otherPlayer.id)
                && this.lastName.equals(otherPlayer.lastName)
                && this.firstName.equals(otherPlayer.firstName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, lastName, firstName);
    }

    public void copy(Player p) {
        id = p.getID();
        lastName = p.getLastName();
        firstName = p.getLastName();
        quote = p.getQuote();
    }

    /**
     * Sets the quote of the player.
     *
     * @param newQuote The new value of the quote
     */
    public void setQuote(int newQuote) {
        quote = newQuote;
    }

    public static class PlayerID extends ID {

        public PlayerID(long id) {
            super(id);
        }

        public PlayerID(String textId) {
            super(Long.parseLong(textId));
        }

        public PlayerID() {
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            } else if (other == null || other.getClass() != this.getClass()) {
                return false;
            }
            return this.getID() == ((PlayerID) other).getID();
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.getID());
        }

    }

}
