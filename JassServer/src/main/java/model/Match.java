package model;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static model.Match.GameVariant.*;
import static tools.RankOperationsHelper.averageRank;

/**
 * Class representing a match between players.
 * Its rank is the mean of the ranks of the players in the match.
 * A match has an expiration date (the time before it is no longer available), and can be
 * private.
 */
public class Match {

    private List<Player> players;
    private GPSPoint location;
    private String description;
    private Rank rank;
    private boolean privateMatch;
    private GameVariant gameVariant;
    private int maxPlayerNumber;
    private long expirationTime;
    private String matchID;

    /**
     * Default constructor required for calls to DataSnapshot.getValue when using Firebase.
     */
    public Match() {
    }

    /**
     * Constructs a Match with a given variant.
     *
     * @param players        The list of players in the match
     * @param location       The location of the match
     * @param description    A brief description of the match (detailed location...)
     * @param privateMatch   The visibility of the match (public or private)
     * @param gameVariant    The variant of the match
     * @param expirationTime The time at which the match expires (in milliseconds after epoch)
     * @param matchID        The unique firebase ID of the match
     */
    public Match(List<Player> players,
                 GPSPoint location,
                 String description,
                 boolean privateMatch,
                 GameVariant gameVariant,
                 long expirationTime,
                 String matchID) {
        this.players = new ArrayList<>(players);
        this.location = location;
        this.description = description;
        rank = averageRank(players);
        this.privateMatch = privateMatch;
        this.gameVariant = gameVariant;
        this.maxPlayerNumber = gameVariant.getMaxPlayerNumber();
        this.expirationTime = expirationTime;
        this.matchID = matchID;
    }

    /**
     * Constructs a Match with default variant (Chibre).
     *
     * @param players        The list of players in the match
     * @param location       The location of the match
     * @param description    A brief description of the match (detailed location...)
     * @param privateMatch   The visibility of the match (public or private)
     * @param expirationTime The time at which the match expires (in milliseconds after epoch)
     * @param matchID        The unique firebase ID of the match
     */
    public Match(List<Player> players,
                 GPSPoint location,
                 String description,
                 boolean privateMatch,
                 long expirationTime,
                 String matchID) {
        this(players, location, description, privateMatch, CHIBRE, expirationTime, matchID);
    }

    /**
     * Getter for the players' list of the match.
     *
     * @return The players' list of the match
     */
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    /**
     * Getter for the location of the match.
     *
     * @return The location of the match in GPS format
     */
    public GPSPoint getLocation() {
        return location;
    }

    /**
     * Getter for the description of the match.
     *
     * @return The description of the match
     */
    public String getDescription() {
        return description;
    }

    /**
     * Getter for the rank of the match.
     *
     * @return The rank of the match
     */
    public Rank getRank() {
        return rank;
    }

    /**
     * Getter for the visibility of the match.
     *
     * @return The visibility of the match
     */
    public boolean isPrivateMatch() {
        return privateMatch;
    }

    /**
     * Getter for the variant of the match.
     *
     * @return The variant of the match
     */
    public GameVariant getGameVariant() {
        return gameVariant;
    }

    /**
     * Getter for the maximum number of players allowed in
     * the match, determined by the variant.
     *
     * @return The maximum number of players
     */
    public int getMaxPlayerNumber() {
        return maxPlayerNumber;
    }

    /**
     * Getter for the expiration date of the match.
     *
     * @return The expiration date of the match, in milliseconds from epoch
     */
    public long getExpirationTime() {
        return expirationTime;
    }

    /**
     * Getter for the firebase ID of the match.
     *
     * @return The firebase ID of the match
     */
    public String getMatchID() {
        return matchID;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (other == null || other.getClass() != this.getClass()) {
            return false;
        }

        Match otherMatch = (Match) other;

        return this.matchID.equals(otherMatch.matchID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matchID);
    }

    /**
     * Getter for the creator of the match, its first player.
     *
     * @return The creator of the match
     */
    public Player createdBy() {
        return players.get(0);
    }

    /**
     * Adds the given player to the match.
     * <p>
     * Adding a player that is already present in the match does nothing,
     * and adding a player if the match if full throws an exception.
     *
     * @param player The player to add to the match
     * @throws IllegalStateException  When the match is full
     * @throws IllegalAccessException When the player is already in the match
     */
    public void addPlayer(Player player) throws IllegalStateException, IllegalAccessException {
        if (players.size() >= maxPlayerNumber) {
            throw new IllegalStateException("Match is full.");
        }
        if (!players.contains(player)) {
            players.add(player);
        } else {
            throw new IllegalAccessException("Player already in that Match.");
        }
    }

    /**
     * Removes the given player to the player from list.
     * <p>
     * Removing a player that is not present do nothing
     *
     * @param toRemove The id of the player to remove from the match
     */
    public void removePlayerById(Player.PlayerID toRemove) throws IllegalStateException {
        if (players.isEmpty()) {
            throw new IllegalStateException("No players in the match.");
        } else {
            int index = -1;
            for (Player p : players) {
                if (p.getID().equals(toRemove)) {
                    index = players.indexOf(p);
                }
            }
            if(index != -1) {
                players.remove(index);
            }
        }

    }

     /* Checks whether the given player is taking part in the match.
     *
     * @param player The player
     * @return true if the player is in the match, false otherwise
     */
    public boolean hasParticipant(Player player) {
        return players.contains(player);
    }

    public static class MatchRank extends Rank {

        public MatchRank(int rank) {
            super(rank);
        }

    }

    /**
     * The different meld available
     */
    public enum Meld {
        MARRIAGE("Stöck"),
        THREE_CARDS("Trois carte"),
        FIFTY("Cinquante"),
        HUNDRED("Cent"),
        FOUR_NINE("Cent cinquante"),
        FOUR_JACKS("Deux cent");

        private final String meldName;

        private Meld(String meldName) {
            this.meldName = meldName;
        }

        /**
         * Returns the value of the current meld
         * @return The value of the meld
         */
        public int value() {
            switch (this) {
                case MARRIAGE:
                case THREE_CARDS:
                    return 20;
                case FIFTY:
                    return 50;
                case HUNDRED:
                    return 100;
                case FOUR_NINE:
                    return 150;
                case FOUR_JACKS:
                    return 200;
                default:
                    return 0;
            }
        }

    }

    /**
     * The different variants of a Jass game.
     */
    public enum GameVariant {
        CHIBRE("Chibre"),
        PIQUE_DOUBLE("Pique Double"),
        OBEN_ABE("Oben Abe"),
        UNDEN_UFE("Unden Ufe"),
        SLALOM("Slalom"),
        CHICANE("Chicane"),
        JASS_MARANT("Jass Marant"),
        ROI("Roi"),
        POMME("Pomme");

        private final String variantName;

        GameVariant(String variantName) {
            this.variantName = variantName;
        }

        @Override
        public String toString() {
            return variantName;
        }

        /**
         * Returns the max number of players allowed in the current variant.
         *
         * @return The max number of players
         */
        public int getMaxPlayerNumber() {
            switch (this) {
                case CHIBRE:
                case PIQUE_DOUBLE:
                case OBEN_ABE:
                case UNDEN_UFE:
                case SLALOM:
                case CHICANE:
                case JASS_MARANT:
                    return 4;
                case ROI:
                    return 3;
                case POMME:
                    return 2;
                default:
                    return 4;
            }
        }

        /**
         * Returns the number of team for the current game variant
         *
         * @return The number of team
         */
        public int getNumberOfTeam() {
            switch (this) {
                case CHIBRE:
                case PIQUE_DOUBLE:
                case OBEN_ABE:
                case UNDEN_UFE:
                case SLALOM:
                case CHICANE:
                case JASS_MARANT:
                case POMME:
                    return 2;
                case ROI:
                    return 3;
                default:
                    return 2;
            }
        }

        /**
         * Returns the number of player by team for the current game variant
         * @return The number of player by team
         */
        public int getNumberOfPlayerByTeam() {
            switch (this) {
                case CHIBRE:
                case PIQUE_DOUBLE:
                case OBEN_ABE:
                case UNDEN_UFE:
                case SLALOM:
                case CHICANE:
                case JASS_MARANT:
                    return 2;
                case ROI:
                case POMME:
                    return 1;
                default:
                    return 2;
            }
        }
    }

    /**
     * Builder for the Match class.
     */
    public static final class Builder {

        public static final String DEFAULT_DESCRIPTION = "New Match";
        public static final String DEFAULT_ID = "Default Match ID";

        private final List<Player> players;
        private GPSPoint location;
        private String description;
        private boolean privateMatch;
        private GameVariant gameVariant;
        private int maxPlayerNumber;
        private long expirationTime;
        private String matchID;

        /**
         * Constructs a new match builder with default values, but with an empty player list.
         */
        public Builder() {
            players = new ArrayList<>();
            location = new GPSPoint(46.520450, 6.567737); // Satellite
            description = DEFAULT_DESCRIPTION;
            privateMatch = false;
            gameVariant = CHIBRE;
            maxPlayerNumber = CHIBRE.getMaxPlayerNumber();
            expirationTime = Calendar.getInstance().getTimeInMillis() + 1 * 3600 * 1000; // 1 hour after current time
            matchID = DEFAULT_ID;
        }

        /**
         * Adds the given player to the player list.
         * <p>
         * Adding a player that is already present in the list does nothing,
         * and adding a player if the match if full throws an exception.
         *
         * @param player The player to add to the match
         * @return The updated builder
         */
        public Builder addPlayer(Player player) throws IllegalStateException, IllegalAccessException {
            if (players.size() >= maxPlayerNumber) {
                throw new IllegalStateException("Match is full.");
            }
            if (!players.contains(player)) {
                players.add(player);
            } else {
                throw new IllegalAccessException("Player already in that Match.");
            }
            return this;
        }

        public void removePlayer(Player player) throws IllegalStateException, IllegalArgumentException {
            if (players.isEmpty()) {
                throw new IllegalStateException("No players in the match.");
            }
            if (!players.contains(player)) {
                throw new IllegalArgumentException("Player not in the Match.");
            }
            players.remove(player);
        }

        public List<Player> getPlayerList() {
            return new ArrayList<>(players);
        }

        public Builder setLocation(GPSPoint location) {
            this.location = location;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setPrivacy(boolean privateMatch) {
            this.privateMatch = privateMatch;
            return this;
        }

        /**
         * Sets the game variant to the given value.
         * <p>
         * Changes the max player number according to the new variant.
         *
         * @param gameVariant The new game variant
         * @return The updated builder
         */
        public Builder setVariant(GameVariant gameVariant) {
            this.gameVariant = gameVariant;
            maxPlayerNumber = gameVariant.getMaxPlayerNumber();
            return this;
        }

        public Builder setExpirationTime(long expirationTime) {
            this.expirationTime = expirationTime;
            return this;
        }

        public Builder setMatchID(String matchID) {
            this.matchID = matchID;
            return this;
        }

        /**
         * Builds and returns the match.
         * <p>
         * Building a match with an empty player list or with more players than allowed
         * throws an exception
         *
         * @return The new match
         * @throws IllegalStateException If building with no players or too many players
         */
        public Match build() throws IllegalStateException {
            // TODO: check validity of arguments
            if (players.isEmpty()) {
                throw new IllegalStateException("Cannot create match without any player.");
            } else if (players.size() > maxPlayerNumber) {
                throw new IllegalStateException("Too many players.");
            } else {
                return new Match(players, location, description, privateMatch,
                        gameVariant, expirationTime, matchID);
            }
        }

    }

}
