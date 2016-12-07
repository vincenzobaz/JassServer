package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import model.Match.Meld;

import static model.Match.Meld.SENTINEL;

/**
 * Class representing a round object. It contains the points and melds obtained in the round.
 */
public class Round {

    private int teamCount;
    private Map<String, Integer> scores;
    private Map<String, List<Meld>> melds;
    private Map<String, Integer> meldScores;

    public Round() {
    }

    public Round(int teamCount) {
        if (teamCount <= 0) {
            throw new IllegalArgumentException("Invalid number of teams");
        }
        this.teamCount = teamCount;
        this.scores = new HashMap<>();
        this.melds = new HashMap<>();
        this.meldScores = new HashMap<>();

        for (int i = 0; i < teamCount; ++i) {
            String key = concatKey(i);
            scores.put(key, 0);
            List<Meld> sentinelList = new ArrayList<>();
            sentinelList.add(SENTINEL);
            melds.put(key, sentinelList);
            meldScores.put(key, 0);
        }
    }

    public int getTeamCount() {
        return teamCount;
    }

    public Map<String, Integer> getScores() {
        return Collections.unmodifiableMap(scores);
    }

    public Map<String, List<Meld>> getMelds() {
        return Collections.unmodifiableMap(melds);
    }

    public Map<String, Integer> getMeldScores() {
        return Collections.unmodifiableMap(meldScores);
    }

    /**
     * Returns the points of the specified team, excluding points obtained
     * with melds.
     *
     * @param teamIndex the index of the team
     * @return the points of the team for this round
     */
    public Integer getTeamCardScore(int teamIndex) {
        if (teamIndex < 0 || teamIndex >= teamCount) {
            throw new IndexOutOfBoundsException("Invalid team index");
        }
        return scores.get(concatKey(teamIndex));
    }

    /**
     * Returns the points obtained with melds for the specified team.
     *
     * @param teamIndex the index of the team
     * @return the meld points of the team
     */
    public Integer getTeamMeldScore(int teamIndex) {
        if (teamIndex < 0 || teamIndex >= teamCount) {
            throw new IndexOutOfBoundsException("Invalid team index");
        }
        return meldScores.get(concatKey(teamIndex));
    }

    /**
     * Returns the total points obtained by the given team for this round
     * (melds and card points).
     *
     * @param teamIndex the index of the team
     * @return the total points for this round
     */
    public Integer getTeamTotalScore(int teamIndex) {
        if (teamIndex < 0 || teamIndex >= teamCount) {
            throw new IndexOutOfBoundsException("Invalid team index");
        }
        return getTeamCardScore(teamIndex) + getTeamMeldScore(teamIndex);
    }

    /**
     * Returns the melds obtained by the specified team for this round.
     *
     * @param teamIndex the index of the team
     * @return the melds
     */
    public List<Meld> getTeamMelds(int teamIndex) {
        if (teamIndex < 0 || teamIndex >= teamCount) {
            throw new IndexOutOfBoundsException("Invalid team index");
        }
        return Collections.unmodifiableList(melds.get(concatKey(teamIndex)));
    }

    /**
     * Sets the score of the given team.
     *
     * @param teamIndex the index of the team
     * @param score     the score of the team
     */
    public void setTeamScore(int teamIndex, int score) {
        if (teamIndex < 0 || teamIndex >= teamCount) {
            throw new IndexOutOfBoundsException("Invalid team index");
        }
        scores.put(concatKey(teamIndex), score);
    }

    /**
     * Adds the given meld to the current meld list and adds its point value
     * to the meld score.
     *
     * @param teamIndex the index of the team that got the meld
     * @param meld      the meld
     */
    public void addMeldToTeam(int teamIndex, Meld meld) {
        if (teamIndex < 0 || teamIndex >= teamCount) {
            throw new IndexOutOfBoundsException("Invalid team index");
        }

        String key = concatKey(teamIndex);
        melds.get(key).remove(SENTINEL);
        melds.get(key).add(meld);
        Integer tmp = meldScores.get(key);
        meldScores.put(key, tmp + meld.value());
    }

    /**
     * Cancels the last meld obtained by the specified team, and updates the score.
     *
     * @param teamIndex the index of the team
     * @return the value of the meld that was cancelled
     */
    public int cancelLastMeld(int teamIndex) {
        if (teamIndex < 0 || teamIndex >= teamCount) {
            throw new IndexOutOfBoundsException("Invalid team index");
        }

        String key = concatKey(teamIndex);
        List<Meld> teamMelds = melds.get(key);
        int meldValue = 0;
        if (!teamMelds.contains(SENTINEL)) {
            Meld meld = teamMelds.remove(teamMelds.size() - 1);
            Integer tmp = meldScores.get(key);
            meldValue = meld.value();
            meldScores.put(key, tmp - meldValue);
            if (teamMelds.isEmpty()) {
                teamMelds.add(SENTINEL);
            }
        }
        return meldValue;
    }

    /**
     * Checks whether a meld was obtained by a team this round.
     *
     * @return true if a meld was obtained, false otherwise
     */
    public boolean meldWasSetThisRound() {
        boolean toReturn = false;
        for (int i = 0; i < teamCount; ++i) {
            String key = concatKey(i);
            toReturn |= !melds.get(key).contains(SENTINEL);
        }
        return toReturn;
    }

    public String meldsToString(int teamIndex) {
        if (teamIndex < 0 || teamIndex >= teamCount) {
            throw new IndexOutOfBoundsException("Invalid team index");
        }

        List<Meld> teamMelds = melds.get(concatKey(teamIndex));

        if (teamMelds.contains(SENTINEL)) {
            return "-";
        }

        StringBuilder meldsString = new StringBuilder();
        for (Iterator<Meld> iterator = teamMelds.iterator(); iterator.hasNext(); ) {
            meldsString.append(iterator.next().toString());
            if (iterator.hasNext()) {
                meldsString.append(",").append(System.getProperty("line.separator"));
            }
        }
        return meldsString.toString();
    }

    private String concatKey(int index) {
        return "TEAM" + index;
    }

}
