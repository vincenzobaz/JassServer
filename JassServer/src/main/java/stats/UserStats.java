package stats;

import model.Match;
import model.Player;
import model.Rank;

import java.util.Calendar;
import java.util.List;
import java.util.Stack;

public class UserStats {
    private int playedMatches = 0;
    private int wonMatches = 0;
    private Stack<Counter<Calendar>> playedByDate = new Stack<>();
    private Stack<Counter<Calendar>> wonByDate = new Stack<>();
    private Stack<Counter<Match.GameVariant>> variants = new Stack<>();
    private Stack<Counter<Rank>> rankByDate = new Stack<>();
    private Stack<Counter<Player.PlayerID>> partners = new Stack<>();
    private Stack<Counter<Player.PlayerID>> wonWith = new Stack<>();

    public UserStats() {

    }

    public int getPlayedMatches() {
        return playedMatches;
    }

    public int getWonMatches() {
        return wonMatches;
    }

    public Stack<Counter<Calendar>> getPlayedByDate() {
        return playedByDate;
    }

    public Stack<Counter<Calendar>> getWonByDate() {
        return wonByDate;
    }

    public Stack<Counter<Match.GameVariant>> getVariants() {
        return variants;
    }

    public Stack<Counter<Rank>> getRankByDate() {
        return rankByDate;
    }

    public Stack<Counter<Player.PlayerID>> getPartners() {
        return partners;
    }

    public Stack<Counter<Player.PlayerID>> getWonWith() {
        return wonWith;
    }
}
