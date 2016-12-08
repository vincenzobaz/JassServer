package stats;

import model.Match;
import model.Player;
import model.Rank;
import server.Main;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StatsBufferListener implements ChildEventListener {
    private DatabaseReference refStats = FirebaseDatabase.getInstance().getReference().child("userStats");
    private DatabaseReference refBuffer = FirebaseDatabase.getInstance().getReference().child("stats").child("buffer");

    private DatabaseReference refMatchStatsArchive = FirebaseDatabase.getInstance().getReference().child("stats").child("matchStatsArchive");
    private DatabaseReference refMatches = FirebaseDatabase.getInstance().getReference().child("stats").child("matches");

    private DatabaseReference refPlayers = FirebaseDatabase.getInstance().getReference().child("players");
    private DatabaseReference refMatchStats = FirebaseDatabase.getInstance().getReference().child("matchStats");

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        MatchStats matchResult = dataSnapshot.getValue(MatchStats.class);
        Main.logger.info("Received StatsUpdate for match " + matchResult.getMatchID());

        for (Player p : matchResult.getMatch().getPlayers()) {
            retrieveAndUpdateStats(p.getID(), matchResult);
        }

        refMatchStatsArchive.child(matchResult.getMatchID()).setValue(matchResult);
        refMatches.child(matchResult.getMatchID()).addListenerForSingleValueEvent(new ValueEventListener() {
            private DatabaseReference refMatchArchive = FirebaseDatabase.getInstance().getReference().child("stats").child("matchArchive");
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Match m = dataSnapshot.getValue(Match.class);
                refMatchArchive.child(m.getMatchID()).setValue(m);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        refMatches.child(matchResult.getMatchID()).removeValue();
        refBuffer.child(matchResult.getMatchID()).removeValue();
        refMatchStats.child(matchResult.getMatchID()).removeValue();
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    private void retrieveAndUpdateStats(Player.PlayerID id, MatchStats matchResult) {
        Main.logger.info("Updating stats of player " + id.toString() + " after match " + matchResult.getMatch().getMatchID());
        refStats.child(id.toString())
                .addListenerForSingleValueEvent(new StatsUpdater(id, matchResult));
    }

    private class StatsUpdater implements ValueEventListener {
        private MatchStats matchResult;
        private Player.PlayerID id;

        StatsUpdater(Player.PlayerID id, MatchStats matchResult) {
            this.matchResult = matchResult;
            this.id = id;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            UserStats stats = null;
            if (dataSnapshot.exists()) {
                stats = dataSnapshot.getValue(UserStats.class);
            } else {
                stats = new UserStats(id);
            }
            stats.update(matchResult);
            int newQuote = stats.updateRank(new NaiveCalculator(stats));

            refStats.child(dataSnapshot.getKey())
                    .setValue(stats);
            refPlayers.child(id.toString()).child("quote").setValue(newQuote);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }
}
