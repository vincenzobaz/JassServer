package stats;

import model.Player;
import model.Rank;
import server.Main;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StatsBufferListener implements ChildEventListener {
    private DatabaseReference refStats = FirebaseDatabase.getInstance().getReference().child("userStats");
    private DatabaseReference refBuffer = FirebaseDatabase.getInstance().getReference().child("matchStats");
    private DatabaseReference refArchive = FirebaseDatabase.getInstance().getReference().child("stats").child("matchArchive");
    private DatabaseReference refPlayers = FirebaseDatabase.getInstance().getReference().child("players");

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        MatchStats matchResult = dataSnapshot.getValue(MatchStats.class);
        Main.logger.info("Received StatsUpdate for match " + matchResult.getMatchID());
        refBuffer.child(dataSnapshot.getKey()).removeValue();

        for (Player p : matchResult.getMatch().getPlayers()) {
            retrieveAndUpdateStats(p.getID(), matchResult);
        }
        refArchive.child(matchResult.getMatch().getMatchID()).setValue(matchResult);
        refBuffer.child(dataSnapshot.getKey()).removeValue();
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
