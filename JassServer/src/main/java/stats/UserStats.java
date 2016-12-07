package stats;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Match;
import model.Player;


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
    private List<Tuple2<Long, Integer>> playedByDate = new ArrayList<>();
    // Number of won matches by date (one counter per day).
    private List<Tuple2<Long, Integer>> wonByDate = new ArrayList<>();
    // The player rank by date (store the value each day to study progression).
    private List<Tuple2<Long, Integer>> quoteByDate = new ArrayList<>();

    // We have to use strings instead of real objects as Firebase does not support Maps with
    // no string - keys
    // How many times different game variants have been played.
    private Map<String, Integer> variants = new HashMap<>();
    // How many matches have been as a partner of other players.
    private Map<String, Integer> partners = new HashMap<>();
    // How many matches have been won as a partner of other players.
    private Map<String, Integer> wonWith = new HashMap<>();

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

    public List<Tuple2<Long, Integer>> getQuoteByDate() {
        return Collections.unmodifiableList(quoteByDate);
    }

    public Map<String, Integer> getVariants() {
        return Collections.unmodifiableMap(variants);
    }

    public Map<String, Integer> getPartners() {
        return Collections.unmodifiableMap(partners);
    }

    public Map<String, Integer> getWonWith() {
        return Collections.unmodifiableMap(wonWith);
    }
    /* End of getters */

    /**
     * Updates the stored statistics (except rank) using the update issued at the end of a match.
     *
     * @param stats The results of a concluded match
     */
    protected UserStats update(MatchStats stats) {
        prepareLastBuckets(Calendar.getInstance().getTimeInMillis());

        Match match = stats.getMatch();
        playedMatches += 1;
        int lastIndex = playedByDate.size() - 1;
        playedByDate.get(lastIndex).setValue(playedByDate.get(lastIndex).getValue() + 1);
        Map<String, List<String>> teams = match.getTeams();
        List<String> winners = teams.get("Team" + (stats.getWinnerIndex()));
        List<String> losers = teams.get("Team" + ((stats.getWinnerIndex() + 1) % 2));
        boolean isWinner = winners.contains(this.playerId.toString());
        if (isWinner) {
            wonMatches += 1;
            wonByDate.get(lastIndex).setValue(wonByDate.get(lastIndex).getValue() + 1);
        }
        List<String> team = isWinner ? winners : losers;
        for (String id : team) {
            if (!playerId.toString().equals(id)) {
                partners.put(id, getOrDefaultMap(partners, id, 0) + 1);
                if (isWinner) {
                    wonWith.put(id, getOrDefaultMap(wonWith, id, 0) + 1);
                }
            }
        }

        variants.put(match.getGameVariant().toString(),
                getOrDefaultMap(variants, match.getGameVariant().toString(), 0) + 1);
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
     * @param quoteCalculator A Strategy object that computes the new rank using the UserStats object.
     */
    protected int updateRank(QuoteCalculator quoteCalculator) {
        Integer newRank = quoteCalculator.computeNewQuote();
        quoteByDate.get(quoteByDate.size() - 1).setValue(newRank);
        return newRank;
    }

    /**
     * Utility method checking if a counter exists for the received date and creates it if it
     * does not exist in the list.
     *
     * @param time
     */
    private void prepareLastBuckets(Long time) {
        long updateDate = getDay(time);
        int lastIndex = playedByDate.size() - 1;
        if (playedByDate.isEmpty() || playedByDate.get(lastIndex).getKey() != updateDate) {
            playedByDate.add(new Tuple2<>(updateDate, 0));
            wonByDate.add(new Tuple2<>(updateDate, 0));
            if (quoteByDate.isEmpty()) {
                quoteByDate.add(new Tuple2<Long, Integer>(updateDate, 0));
            } else {
                quoteByDate.add(new Tuple2<>(updateDate, quoteByDate.get(lastIndex).getValue()));
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
