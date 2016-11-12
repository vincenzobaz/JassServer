package model;

/**
 * Class representing a player, identified by his/her name, sciper ID,
 * and rank in the scoreboard.
 */
public class Player {

    private PlayerID id;
    private String lastName;
    private String firstName;
    private Rank rank;

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
     * @param rank      The rank of the player
     */
    public Player(PlayerID id, String lastName, String firstName, Rank rank) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.rank = rank;
    }

    /**
     * Constructs a new Player with a default rank.
     *
     * @param id        The ID of the player (sciper)
     * @param lastName  The last name of the player
     * @param firstName The first name of the player
     */
    public Player(PlayerID id, String lastName, String firstName) {
        this(id, lastName, firstName, new PlayerRank(0));
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
     * Getter for the rank of the player.
     *
     * @return The rank of the player
     */
    public Rank getRank() {
        return rank;
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

    /**
     * Sets the rank of the player.
     *
     * @param newRank The new value of the rank
     */
    public void setRank(Rank newRank) {
        rank = newRank;
    }

    public static class PlayerRank extends Rank {

        public PlayerRank(int rank) {
            super(rank);
        }

        public PlayerRank() {
            // TODO get rank of last player
        }

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
        // TODO: implement equals and hascode

        public String toString() {
            return Long.toString(getID());
        }

    }

}
