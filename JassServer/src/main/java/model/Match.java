package model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import static model.Match.GameVariant.CHIBRE;
import static model.QuoteOperationsHelper.averageQuote;

public class Match {

    private List<Player> players;
    private GPSPoint location;
    private String description;
    private int quote;
    private boolean privateMatch;
    private GameVariant gameVariant;
    private int maxPlayerNumber;
    private long time;
    private String matchID;
    private Map<String, Boolean> hasCards;
    private MatchStatus matchStatus;
    private Map<String, List<String>> teams;
    private final static String SENTINEL = "SENTINEL";
    private final static int ONE_HOUR = 3600000;

    /**
     * Default constructor required for calls to DataSnapshot.getValue when using Firebase.
     */
    public Match() {
    }

    /**
     * Constructs a Match with a given variant and a given status.
     *
     * @param players        The list of players in the match
     * @param location       The location of the match
     * @param description    A brief description of the match (detailed location...)
     * @param privateMatch   The visibility of the match (public or private)
     * @param gameVariant    The variant of the match
     * @param time The time at which the match expires (in milliseconds after epoch)
     * @param matchID        The unique firebase ID of the match
     * @param hasCards       A list of who has cards available for the match
     * @param status         The status of the match
     */
    public Match(List<Player> players,
                 GPSPoint location,
                 String description,
                 boolean privateMatch,
                 GameVariant gameVariant,
                 long time,
                 String matchID,
                 Map<String, Boolean> hasCards,
                 MatchStatus status) {
        this.players = new ArrayList<>(players);
        this.location = location;
        this.description = description;
        quote = averageQuote(players);
        this.privateMatch = privateMatch;
        this.gameVariant = gameVariant;
        this.maxPlayerNumber = gameVariant.getMaxPlayerNumber();
        this.time = time;
        this.matchID = matchID;
        this.hasCards = hasCards;
        this.hasCards.put(SENTINEL, false);
        this.matchStatus = status;
        this.teams = new HashMap<>();
        for (int teamNb = 0; teamNb < gameVariant.getNumberOfTeam(); ++teamNb) {
            //If we don't do this, Firebase will not add the team map since the value are 0.....
            //We need the key to be a string without beginning by a number of Firebase with think it's an array...
            this.teams.put("Team" + Integer.toString(teamNb), new ArrayList<String>());

            this.teams.get("Team" + Integer.toString(teamNb)).add(SENTINEL);
        }
    }

    public Map<String, Boolean> getHasCards() {
        return hasCards;
    }

    /**
     * Constructs a Match with a given variant and the default status (pending).
     *
     * @param players        The list of players in the match
     * @param location       The location of the match
     * @param description    A brief description of the match (detailed location...)
     * @param privateMatch   The visibility of the match (public or private)
     * @param gameVariant    The variant of the match
     * @param time The time at which the match expires (in milliseconds after epoch)
     * @param matchID        The unique firebase ID of the match
     */
    public Match(List<Player> players,
                 GPSPoint location,
                 String description,
                 boolean privateMatch,
                 GameVariant gameVariant,
                 long time,
                 String matchID) {
        this(players, location, description, privateMatch, gameVariant, time, matchID, new HashMap<String, Boolean>(), MatchStatus.PENDING);
    }

    /**
     * Constructs a Match with default variant (Chibre) and the default status (pending).
     *
     * @param players        The list of players in the match
     * @param location       The location of the match
     * @param description    A brief description of the match (detailed location...)
     * @param privateMatch   The visibility of the match (public or private)
     * @param time The time at which the match expires (in milliseconds after epoch)
     * @param matchID        The unique firebase ID of the match
     * @param hasCards       A list of who has cards available for the match
     */
    public Match(List<Player> players,
                 GPSPoint location,
                 String description,
                 boolean privateMatch,
                 long time,
                 String matchID,
                 Map<String, Boolean> hasCards) {
        this(players, location, description, privateMatch, CHIBRE, time, matchID, hasCards, MatchStatus.PENDING);
    }

    /**
     * Checks if the match has changed
     *
     * @param other the match to compare to
     * @return true if the match has changed, false otherwise
     */
    public boolean matchHasChanged(Match other) {
        if (!other.equals(this)) {
            return false;
        } else {
            if (!other.players.equals(this.players)) {
                return true;
            } else if (!other.location.equals(this.location)) {
                return true;
            } else if (!other.description.equals(this.description)) {
                return true;
            } else if(other.quote != this.quote) {
                return true;
            } else if (other.privateMatch != this.privateMatch) {
                return true;
            } else if (other.maxPlayerNumber != this.maxPlayerNumber) {
                return true;
            } else if (other.time != this.time) {
                return true;
            } else if (!other.matchStatus.equals(this.matchStatus)) {
                return true;
            } else if (!other.teams.equals(this.teams)) {
                return true;
            }else if (!other.hasCards.equals(this.hasCards)) {
                return true;
            }
            return false;
        }
    }

    /**
     * Constructs a Match with default variant (Chibre) and the default status (pending).
     *
     * @param players        The list of players in the match
     * @param location       The location of the match
     * @param description    A brief description of the match (detailed location...)
     * @param privateMatch   The visibility of the match (public or private)
     * @param time The time at which the match expires (in milliseconds after epoch)
     * @param matchID        The unique firebase ID of the match
     */
    public Match(List<Player> players,
                 GPSPoint location,
                 String description,
                 boolean privateMatch,
                 long time,
                 String matchID) {
        this(players, location, description, privateMatch, CHIBRE, time, matchID);
    }

    /**
     * Getter for the players' list of the match.
     *
     * @return An immutable list of the players in the match
     */
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    /**
     * Getter for the player's index in the players list
     *
     * @param id The player' id
     * @return The index of the player if he is in match, -1 otherwise
     */
    public int getPlayerIndex(Player.PlayerID id) {
        for (int i = 0; i < players.size(); ++i) {
            if (players.get(i).getID().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Return a sentinelMatch that can be used to avoid null pointer exception
     *
     * @return a sentinel match
     */
    public static Match sentinelMatch() {
        Player bricoloBob = new Player(new Player.PlayerID(696969), "LeBricoleur", "Bob", 1000);
        List<Player> players = new ArrayList<>();
        players.add(bricoloBob);
        GPSPoint BCCoord = new GPSPoint(46.518470, 6.561907);
        return new Match(players, BCCoord, "Sentinel match", true,
                GameVariant.CHIBRE, Calendar.getInstance().getTimeInMillis() + 2 * 3600 * 1000,
                "sentiMatch", new HashMap<String, Boolean>(), MatchStatus.PENDING);
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
     * Getter for the quote of the match.
     *
     * @return The quote of the match
     */
    public int getQuote() {
        return quote;
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
    public long getTime() {
        return time;
    }

    /**
     * Getter for the card parameter, return true if player has cards, false othewise.
     *
     * @return return boolean if have cards available.
     */
    public boolean getPlayerCards(String playerID) {
        if (playerID != null && hasCards.containsKey(playerID)){
            return hasCards.get(playerID);
        }
        else{
            return false;
        }
    }

    /**
     * Getter for the player if he is already in the list
     *
     * @return boolean if player in list
     */
    public boolean playerInCardList(String playerID) {
        if (playerID != null && hasCards.containsKey(playerID)){
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Checker for hasCards, return false if empty, true otherwise.
     *
     * @return boolean true if someone have cards, false otherwise.
     */
    public boolean hasCards() {
        if(hasCards.containsValue(true)){
            return true;
        }
        else {
            return false;
        }
     }

    /**
     * Set
     * @param hasCards
     */
    public void setHasCards(Map<String, Boolean> hasCards) {
        this.hasCards = hasCards;
    }

    /**
     * Setter for player cards
     * @param playerID  the id of the player to set cards
     * @param cards boolean if player has cards
     */
    public void setPlayerCards(String playerID, Boolean cards){
        if (playerID != null) {
            hasCards.put(playerID, cards);
        }
    }

    /**
     * Setter for the expiration time
     *
     * @param expTime The expiration time to set
     */
    public void setTime(long expTime) {
        this.time = expTime;
    }

    /**
     * Getter for the firebase ID of the match.
     *
     * @return The firebase ID of the match
     */
    public String getMatchID() {
        return matchID;
    }

    public boolean matchFull() {
        return players.size() == getMaxPlayerNumber();
    }

    /**
     * Set the player with the specified id in the specified team
     *
     * @param teamNb The team we want to add a player
     * @param id     The id of the player
     * @throws IllegalArgumentException If the team number is not valid or if the id is not one of a match player
     */
    public void setTeam(int teamNb, Player.PlayerID id) throws IllegalArgumentException {
        List<String> team = teams.get("Team" + Integer.toString(teamNb));
        if (team == null) {
            throw new IllegalArgumentException("Invalid team number specified.");
        }
        if (!hasParticipantWithID(id)) {
            throw new IllegalArgumentException("The player is not in this match.");
        }
        if (team.contains(SENTINEL)) {
            team.set(team.indexOf(SENTINEL), id.toString());
        } else if (!team.contains(id.toString())) {
            team.add(id.toString());
        }
        //remove it from other team if he was in one
        for (List<String> t : teams.values()) {
            if (t != team) {
                t.remove(id.toString());
                if (t.isEmpty()) {
                    t.add(SENTINEL);
                }
            }
        }
    }

    /**
     * Getter for the team map
     *
     * @return An immutable map of the team in this match
     */
    public Map<String, List<String>> getTeams() {
        Map<String, List<String>> tmp = new HashMap<>();
        for (String k : teams.keySet()) {
            tmp.put(k, Collections.unmodifiableList(teams.get(k)));
        }
        return Collections.unmodifiableMap(tmp);
    }

    /**
     * Getter for the match' status
     *
     * @return True if the match is active, false if it is waiting for players
     */
    public MatchStatus getMatchStatus() {
        return matchStatus;
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
     * <br>
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

    public Player getPlayerById(String id) {
        Player.PlayerID playerId = new Player.PlayerID(id);
        for (Player player : players) {
            if (player.getID().equals(playerId)) {
                return player;
            }
        }
        throw new NoSuchElementException("No player with this id in this match");
    }

    /**
     * Removes the given player to the player list and change the match status to pending
     *
     * @param toRemove The id of the player to remove from the match
     * @return True if the player was removed, false otherwise
     */
    public boolean removePlayerById(Player.PlayerID toRemove) {
        if (!players.isEmpty()) {
            int index = -1;

            for (Player p : players) {
                if (p.getID().equals(toRemove)) {
                    index = players.indexOf(p);
                }
            }
            if (index != -1) {
                players.remove(index);
                matchStatus = MatchStatus.PENDING;
            }
            //Remove player from team if he was in one
            for (List<String> t : teams.values()) {
                t.remove(toRemove.toString());
                if (t.isEmpty()) {
                    t.add(SENTINEL);
                }
            }
            if (hasCards.containsKey(toRemove)) {
                hasCards.remove(toRemove);
            }
            return true;
        }
        return false;
    }

    /**
     * Check if the team assignment is correct regarding the match' GameVariant
     *
     * @return True if the team assignment is correct
     */
    public boolean teamAssignmentIsCorrect() {
        //Test if all player of the match are in the team list and no more
        Set<String> assignedPlayer = new HashSet<>();
        //Correct number of team
        //this could only happen if the teams map is modified inside the Match class without caution
        if (teams.size() != gameVariant.getNumberOfTeam()) {
            return false;
        }

        //For all team
        for (List<String> team : teams.values()) {
            //Correct number of player by team
            if (team.size() != gameVariant.getNumberOfPlayerByTeam()) {
                return false;
            }
            //The team does not contain the sentinel
            else if (team.contains(SENTINEL)) {
                return false;
            }
            assignedPlayer.addAll(team);
        }

        //The teams does not contain duplicates
        //this could only happen if the teams map is modified inside the Match class without caution
        if (assignedPlayer.size() != players.size()) {
            return false;
        } else {
            //Every player from the team is in the match
            //this could only happen if the teams map is modified inside the Match class without caution
            for (String s : assignedPlayer) {
                if (!hasParticipantWithID(new Player.PlayerID(s))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Setter for the match status
     *
     * @param status The status we want to give to this match
     */
    public void setStatus(MatchStatus status) {
        this.matchStatus = status;
    }

    /**
     * Checks whether the given player is taking part in the match.
     *
     * @param player The player
     * @return true if the player is in the match, false otherwise
     */
    public boolean hasParticipant(Player player) {
        return players.contains(player);
    }


    /**
     * Checks if the match has a participant with the given ID
     *
     * @param userID The ID to check
     * @return true if a player with the id is in the match, false otherwise
     */
    public boolean hasParticipantWithID(Player.PlayerID userID) {
        for (Player p : players) {
            if (p.getID().equals(userID)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the team number the player is in
     *
     * @param p The player
     * @return the team number if the player is in the match, -1 otherwise
     */
    public int teamNbForPlayer(Player p) {
        if (players.contains(p)) {
            for (int teamNb = 0; teamNb < teams.size(); teamNb++) {
                if (teams.get("Team" + teamNb).contains(p.getID().toString())) {
                    return teamNb;
                }
            }
        }
        return -1;
    }

    /**
     * The different status a match can have
     */
    public enum MatchStatus {
        ACTIVE("ACTIVE"),
        PENDING("PENDING");

        private final String statusName;

        MatchStatus(String statusName) {
            this.statusName = statusName;
        }

        @Override
        public String toString() {
            return statusName;
        }
    }

    /**
     * The different meld available
     */
    public enum Meld {
        SENTINEL("Sentinel"),
        MARRIAGE("Stöck"),
        THREE_CARDS("Twenty"),
        FIFTY("Fifty"),
        HUNDRED("Hundred"),
        FOUR_NINE("Hundred fifty"),
        FOUR_JACKS("Two hundred");

        private final String meldName;

        Meld(String meldName) {
            this.meldName = meldName;
        }

        @Override
        public String toString() {
            return meldName;
        }

        /**
         * Returns the value of the current meld
         *
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
                case SENTINEL:
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
        JASS_MARANT("Jass Marant");

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
                default:
                    return 2;
            }
        }

        /**
         * Returns the number of player by team for the current game variant
         *
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
                default:
                    return 2;
            }
        }

        /**
         * Returns the point goal for the current game variant
         *
         * @return The point goal
         */
        public int getPointGoal() {
            switch (this) {
                case CHIBRE:
                    return 1000;
                case PIQUE_DOUBLE:
                    return 1500;
                case OBEN_ABE:
                case UNDEN_UFE:
                case SLALOM:
                case CHICANE:
                case JASS_MARANT:
                default:
                    return 2500;
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
        private long time;
        private String matchID;
        private Map<String, Boolean> hasCards;
        private MatchStatus matchStatus;

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
            time = Calendar.getInstance().getTimeInMillis() + ONE_HOUR; // 1 hour after current time
            matchID = DEFAULT_ID;
            hasCards = new HashMap<>();
            matchStatus = MatchStatus.PENDING;
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

        /**
         * Removes a player from this matchBuilder players list
         *
         * @param player The player to remove
         * @throws IllegalStateException    If the players list is empty
         * @throws IllegalArgumentException If the player to be removed is not in the list
         */
        public void removePlayer(Player player) throws IllegalStateException, IllegalArgumentException {
            if (players.isEmpty()) {
                throw new IllegalStateException("No players in the match.");
            }
            if (!players.contains(player)) {
                throw new IllegalArgumentException("Player not in the Match.");
            }
            players.remove(player);
            matchStatus = MatchStatus.PENDING;
        }

        /**
         * Getter for this matchBuilder players list
         *
         * @return The players lists
         */
        public List<Player> getPlayerList() {
            return new ArrayList<>(players);
        }

        /**
         * Setter for the location of this match
         *
         * @param location the location to be set
         * @return A builder containing this location
         */
        public Builder setLocation(GPSPoint location) {
            this.location = location;
            return this;
        }

        /**
         * Setter for the description of this match
         *
         * @param description the description to be set
         * @return A builder containing this description
         */
        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        /**
         * Setter for the privacy of the match
         *
         * @param privateMatch the privacy of the match
         * @return A builder containing this privacy
         */
        public Builder setPrivacy(boolean privateMatch) {
            this.privateMatch = privateMatch;
            return this;
        }

        public Map<String, Boolean> getHasCards() {
            return new HashMap<>(hasCards);
        }

        public void setHasCards(Map<String, Boolean> hasCards) {
            this.hasCards = hasCards;
            if(this.hasCards == null) {
                this.hasCards = new HashMap<>();
            }
            if(this.hasCards.isEmpty()) {
                this.hasCards.put(SENTINEL, false);
            }
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

        /**
         * Setter for the expiration time of this match
         *
         * @param time the expiration time to be set
         * @return A builder containing this expiration time
         */
        public Builder setTime(long time) {
            this.time = time;
            return this;
        }

        /**
         * Setter for the match ID of this match
         *
         * @param matchID the match ID to be set
         * @return A builder containing this match id
         */
        public Builder setMatchID(String matchID) {
            this.matchID = matchID;
            return this;
        }

        /**
         * Setter for the match status
         *
         * @param status The match status to set
         * @return A builder containing this match status
         */
        public Builder setStatus(MatchStatus status) {
            this.matchStatus = status;
            return this;
        }

        /**
         * Getter for the match status
         *
         * @return The match status
         */
        public MatchStatus getMatchStatus() {
            return this.matchStatus;
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
            // If an error occured puts parameters back to basic state
            if(location == null) {
                location = new GPSPoint(46.520450, 6.567737);
            }

            if(description == null || description.equals("")) {
                description = DEFAULT_DESCRIPTION;
            }

            if(maxPlayerNumber != gameVariant.getMaxPlayerNumber()) {
                maxPlayerNumber = gameVariant.getMaxPlayerNumber();
            }

            if(time < Calendar.getInstance().getTimeInMillis()) {
                time = Calendar.getInstance().getTimeInMillis() + (3600 * 1000);
            }

            // Need to generate random matchId when error.
            if(matchID == null || matchID.equals("")) {
                Random generator = new Random();
                matchID = DEFAULT_ID + generator.nextInt(100000);
            }

            if (players.isEmpty()) {
                throw new IllegalStateException("Cannot create match without any player.");
            } else if (players.size() > maxPlayerNumber) {
                throw new IllegalStateException("Too many players.");
            } else {
                return new Match(players, location, description, privateMatch,
                        gameVariant, time, matchID, hasCards, matchStatus);
            }
        }

    }

}
