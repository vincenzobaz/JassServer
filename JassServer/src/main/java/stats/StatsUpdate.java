package stats;

import model.Match;
import model.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author vincenzobaz
 *         A stats update is created at the end of each match and pushed in the /Stats/StatsBuffer section
 *         of the FirebaseDatabase. It a small object containing information about the finished that will be
 *         used to update the user statistics by the server.
 */
class StatsUpdate {
    private long timestamp;
    private List<Player.PlayerID> winners;
    private List<Player.PlayerID> losers;
    private int scoreWin;
    private int scoreLost;
    private Match.GameVariant variant;
    private String matchId;

    /**
     * Constructor of the object
     *
     * @param timestamp The timestamp of the end of the match, in milliseconds.
     * @param winners   The list of the IDs of the winner team.
     * @param losers    The list of the IDs of the losing team.
     * @param scoreWin  The score of the winners.
     * @param scoreLost The score of the losers.
     * @param variant   The {@link model.Match.GameVariant} variant of the
     *                  match.
     * @param matchId   The unique identifier of the match.
     */
    private StatsUpdate(long timestamp,
                        List<Player.PlayerID> winners,
                        List<Player.PlayerID> losers,
                        int scoreWin,
                        int scoreLost,
                        Match.GameVariant variant,
                        String matchId) {
        this.timestamp = timestamp;
        this.winners = winners;
        this.losers = losers;
        this.scoreWin = scoreWin;
        this.scoreLost = scoreLost;
        this.variant = variant;
        this.matchId = matchId;
    }

    /**
     * Empty constructor for Firebase serialization
     */
    public StatsUpdate() {

    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<Player.PlayerID> getWinners() {
        return Collections.unmodifiableList(winners);
    }

    public List<Player.PlayerID> getLosers() {
        return Collections.unmodifiableList(losers);
    }

    public int getScoreWin() {
        return scoreWin;
    }

    public int getScoreLost() {
        return scoreLost;
    }

    public Match.GameVariant getVariant() {
        return variant;
    }

    public String getMatchId() {
        return matchId;
    }

    /**
     * Builder class for StatsUpdate.
     * Only way to instantiate StatsUpdate
     */
    public static class Builder {
        private long timestamp;
        private List<Player.PlayerID> winners;
        private List<Player.PlayerID> losers;
        private int scoreWin;
        private int scoreLost;
        private Match.GameVariant variant;
        private String matchId;

        public Builder() {
            winners = new LinkedList<>();
            losers = new LinkedList<>();
        }

        public void addLosers(Player.PlayerID... losers) {
            for (Player.PlayerID id : losers) {
                this.losers.add(id);
            }
        }

        public void addWinners(Player.PlayerID... winners) {
            for (Player.PlayerID id : winners) {
                this.winners.add(id);
            }
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public void setWinScore(int winScore) {
            this.scoreWin = winScore;
        }

        public void setLoseScore(int loseScore) {
            this.scoreLost = loseScore;
        }

        public void setGameVariant(Match.GameVariant v) {
            this.variant = v;
        }

        public void setMatchId(String matchId) {
            this.matchId = matchId;
        }

        public StatsUpdate build() {
            return new StatsUpdate(timestamp, winners, losers, scoreWin, scoreLost, variant, matchId);
        }
    }
}
