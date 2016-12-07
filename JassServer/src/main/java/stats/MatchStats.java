package stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Match;
import model.Match.GameVariant;
import model.Match.Meld;
import model.Round;

/**
 * This class contains the methods and fields necessary to make statistics about a match and to
 * be used to count points during the match
 */

public class MatchStats {

    // The unique identifiers of the match used in the database
    private Match match;
    // The match' gameVariant. Used to choose how points are counted and so on
    private int nbTeam;
    private List<Round> rounds;
    private Map<String, Integer> totalScores;
    // Index to the current round
    private int currentRoundIndex;
    private int winnerIndex;

    public MatchStats() {
    }

    /**
     * Constructs a MatchStats with the given id and variant.
     *
     * @param match the match you want to record the stats of
     */
    public MatchStats(Match match) {
        this.match = match;
        this.nbTeam = match.getGameVariant().getNumberOfTeam();
        this.rounds = new ArrayList<>();
        this.rounds.add(new Round(nbTeam));
        this.totalScores = new HashMap<>();
        for (int i = 0; i < nbTeam; ++i) {
            totalScores.put(concatKey(i), 0);
        }
        this.currentRoundIndex = 0;
        this.winnerIndex = -1;
    }

    public Match getMatch() {
        return match;
    }

    /**
     * Getter for the matchID
     *
     * @return The matchID
     */
    public String getMatchID() {
        return match.getMatchID();
    }

    /**
     * Getter for the game variant of this match
     *
     * @return The game variant
     */
    public GameVariant getGameVariant() {
        return match.getGameVariant();
    }

    public int getNbTeam() {
        return nbTeam;
    }

    public List<Round> getRounds() {
        return Collections.unmodifiableList(rounds);
    }

    public Map<String, Integer> getTotalScores() {
        return Collections.unmodifiableMap(totalScores);
    }

    public int getCurrentRoundIndex() {
        return currentRoundIndex;
    }

    public int getWinnerIndex() {
        return winnerIndex;
    }

    public boolean goalHasBeenReached() {
        return updateGoalAndWinner();
    }

    public boolean meldWasSetThisRound() {
        return rounds.get(currentRoundIndex).meldWasSetThisRound();
    }

    public boolean allTeamsHaveReachedGoal() {
        boolean allTeamSHaveReachedGoal = true;
        for (String key : totalScores.keySet()) {
            allTeamSHaveReachedGoal &= totalScores.get(key) >= match.getGameVariant().getPointGoal();
        }
        return allTeamSHaveReachedGoal;
    }

    /**
     * Returns the score of the team at the given index for the current Round.
     *
     * @param teamIndex the index of the team
     * @return the score of the team, for the currentRound
     */
    public Integer getCurrentRoundTeamScore(int teamIndex) {
        if (teamIndex < 0 || teamIndex >= nbTeam) {
            throw new IndexOutOfBoundsException("Invalid team index");
        }
        return rounds.get(currentRoundIndex).getTeamTotalScore(teamIndex);
    }

    /**
     * Returns the total score of the team at the given index.
     *
     * @param teamIndex the index of the team
     * @return the total score of the team for the match
     */
    public Integer getTotalMatchScore(int teamIndex) {
        if (teamIndex < 0 || teamIndex >= nbTeam) {
            throw new IndexOutOfBoundsException("Invalid team index");
        }
        return totalScores.get(concatKey(teamIndex));
    }

    /**
     * Closes the currentRound, updating the scores, and starts a new round.
     */
    public void finishRound() {
        if (!goalHasBeenReached()) {
            rounds.add(new Round(nbTeam));
            ++currentRoundIndex;
        }
    }

    /**
     * Cancels the last round, deleting the points obtained in that round.
     *
     * @param teamIndex the index of the last team that updated its score
     * @throws UnsupportedOperationException if there is no round to cancel
     */
    public void cancelLastRound(int teamIndex) throws UnsupportedOperationException {
        if (teamIndex < 0 || teamIndex >= nbTeam) {
            throw new IndexOutOfBoundsException("Invalid team index");
        }
        if (currentRoundIndex == 0 && !meldWasSetThisRound()) {
            throw new UnsupportedOperationException("Nothing to cancel");
        }
        if (meldWasSetThisRound()) {
            Round currentRound = rounds.get(currentRoundIndex);
            int meldValue = currentRound.cancelLastMeld(teamIndex);
            updateTotalScore(teamIndex, -meldValue);
        } else {
            rounds.remove(currentRoundIndex);
            --currentRoundIndex;
            for (int i = 0; i < nbTeam; ++i) {
                int cardValue = rounds.get(currentRoundIndex).getTeamCardScore(i);
                updateTotalScore(i, -cardValue);
                rounds.get(currentRoundIndex).setTeamScore(i, 0);
            }
        }
    }

    /**
     * Sets the score of the team at the given index to the given points.
     *
     * @param teamIndex the index of the team
     * @param score     the score of the team
     */
    public void setScore(int teamIndex, int score) {
        if (teamIndex < 0 || teamIndex >= nbTeam) {
            throw new IndexOutOfBoundsException("Invalid team index");
        }
        rounds.get(currentRoundIndex).setTeamScore(teamIndex, score);
        updateTotalScore(teamIndex, score);
    }

    /**
     * Adds the given meld to the team at the given index.
     *
     * @param teamIndex the index of the team
     * @param meld      the meld
     */
    public void setMeld(int teamIndex, Meld meld) {
        if (teamIndex < 0 || teamIndex >= nbTeam) {
            throw new IndexOutOfBoundsException("Invalid team index");
        }
        rounds.get(currentRoundIndex).addMeldToTeam(teamIndex, meld);
        updateTotalScore(teamIndex, meld.value());
    }

    /**
     * Set the match to the given one
     * @param m The match
     */
    public void setMatch(Match m) {
        this.match = m;
    }

    public void setWinnerIndex(int winnerIndex) {
        this.winnerIndex = winnerIndex;
    }

    private String concatKey(int index) {
        return "TEAM" + index;
    }

    private void updateTotalScore(int teamIndex, int score) {
        String key = concatKey(teamIndex);
        Integer tmp = totalScores.get(key);
        tmp += score;
        totalScores.put(key, tmp);
        updateGoalAndWinner();
    }

    private boolean updateGoalAndWinner() {
        boolean goalHasBeenReached = false;
        for (String key : totalScores.keySet()) {
            if (!goalHasBeenReached && totalScores.get(key) >= match.getGameVariant().getPointGoal()) {
                goalHasBeenReached = true;
                winnerIndex = winnerIndex == -1 ? key.charAt(4) - '0' : winnerIndex;
            }
        }
        return goalHasBeenReached;
    }

}
