package stats;

import model.Match;
import model.Player;
import model.Rank;

import java.util.*;


/**
 * @author vincenzobaz
 *         <p>
 *         This class serves as a container for the statistics concerning one
 *         player. The tracked information are class fields and are documented below.
 */
public class UserStats {
    // The unique identifier of the player.
    private Player.PlayerID playerId;
    // How many matches he played.
    private int playedMatches = 0;
    // How many matches he won.
    private int wonMatches = 0;

    // Number of played matches by date (one counter per day).
    private LinkedList<Tuple2<Long, Integer>> playedByDate = new LinkedList<>();
    // Number of won matches by date (one counter per day).
    private LinkedList<Tuple2<Long, Integer>> wonByDate = new LinkedList<>();
    // The player rank by date (store the value each day to study progression).
    private LinkedList<Tuple2<Long, Rank>> rankByDate = new LinkedList<>();

    // How many times different game variants have been played.
    private Map<Match.GameVariant, Integer> variants = new HashMap<>();
    // How many matches have been as a partner of other players.
    private Map<Player.PlayerID, Integer> partners = new HashMap<>();
    // How many matches have been won as a partner of other players.
    private Map<Player.PlayerID, Integer> wonWith = new HashMap<>();

    /**
     * Constructor, only start with user id.
     *
     * @param id
     */
    public UserStats(Player.PlayerID id) {
        this.playerId = id;
    }

    /**
     * Empty constructor, needed for Firebase serialization.
     */
    public UserStats() {

    }

    /* Getters */
    public Player.PlayerID getPlayerId() {
        return playerId;
    }

    public int getPlayedMatches() {
        return playedMatches;
    }

    public int getWonMatches() {
        return wonMatches;
    }

    public List<Tuple2<Long, Integer>> getPlayedByDate() {
        return Collections.unmodifiableList(playedByDate);
    }

    public List<Tuple2<Long, Integer>> getWonByDate() {
        return Collections.unmodifiableList(wonByDate);
    }

    public List<Tuple2<Long, Rank>> getRankByDate() {
        return Collections.unmodifiableList(rankByDate);
    }

    public Map<Match.GameVariant, Integer> getVariants() {
        return Collections.unmodifiableMap(variants);
    }

    public Map<Player.PlayerID, Integer> getPartners() {
        return Collections.unmodifiableMap(partners);
    }

    public Map<Player.PlayerID, Integer> getWonWith() {
        return Collections.unmodifiableMap(wonWith);
    }
    /* End of getters */

    /**
     * Updates the stored statistics (except rank) using the update issued at the end of a match.
     *
     * @param update The results of a concluded match
     */
    protected UserStats update(StatsUpdate update) {
        prepareLastBuckets(update.getTimestamp());

        playedMatches += 1;
        playedByDate.peekLast().setValue(playedByDate.getLast().getValue() + 1);
        boolean isWinner = update.getWinners().contains(this.playerId);
        if (isWinner) {
            wonMatches += 1;
            wonByDate.peekLast().setValue(wonByDate.getLast().getValue() + 1);
        }
        List<Player.PlayerID> team = isWinner ? update.getWinners() : update.getLosers();
        for (Player.PlayerID id : team) {
            if (!playerId.equals(id)) {
                partners.put(id, getOrDefaultMap(partners, id, 0) + 1);
                if (isWinner) {
                    wonWith.put(id, getOrDefaultMap(wonWith, id, 0) + 1);
                }
            }
        }

        variants.put(update.getVariant(), getOrDefaultMap(variants, update.getVariant(), 0) + 1);
        return this;
    }

    /**
     * Utility method to compensate the lack of the getOrDefault method in Maps in Java7
     */
    private <K, V> V getOrDefaultMap(Map<K, V> map, K key, V defaultValue) {
        if (map.containsKey(key)) {
            return map.get(key);
        } else {
            return defaultValue;
        }
    }

    /**
     * Updates the rank object of the day specified in timestamp according to new information.
     *
     * @param rankCalculator A Strategy object that computes the new rank using the UserStats object.
     */
    protected void updateRank(RankCalculator rankCalculator) {
        Rank newRank = rankCalculator.computeNewRank();
        rankByDate.getLast().setValue(newRank);
    }

    /**
     * Utility method checking if a counter exists for the received date and creates it if it
     * does not exist in the list.
     *
     * @param time
     */
    private void prepareLastBuckets(Long time) {
        long updateDate = getDay(time);
        if (playedByDate.peekLast() == null || playedByDate.peekLast().getKey() != updateDate) {
            playedByDate.addLast(new Tuple2<>(updateDate, 0));
            wonByDate.addLast(new Tuple2<>(updateDate, 0));
            if (rankByDate.isEmpty()) {
                rankByDate.addLast(new Tuple2<Long, Rank>(updateDate, new Rank(0)));
            } else {
                rankByDate.addLast(new Tuple2<>(updateDate, rankByDate.getLast().getValue()));
            }
        }
    }

    /**
     * Normalizes the date: We interestad in tracking data day by day. Therefore we have to
     * make all hours and seconds the same in the same day. We settled for 23:59:59
     *
     * @param timestamp The time at the end of the match in milliseconds
     * @return the same date but with time set at 23:59:59
     */
    private long getDay(long timestamp) {
        Date thatDay = new Date(timestamp);
        Calendar thisDate = Calendar.getInstance();
        thisDate.setTime(thatDay);

        thisDate.set(Calendar.HOUR_OF_DAY, 23);
        thisDate.set(Calendar.MINUTE, 59);
        thisDate.set(Calendar.SECOND, 59);
        return thisDate.getTimeInMillis();
    }
}
