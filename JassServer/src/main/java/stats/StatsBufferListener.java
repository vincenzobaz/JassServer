package stats;

import model.Player;
import model.Rank;
import server.Main;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class StatsBufferListener implements ChildEventListener {
    private DatabaseReference refStats = FirebaseDatabase.getInstance().getReference().child("stats").child("user");
    private DatabaseReference refBuffer = FirebaseDatabase.getInstance().getReference().child("stats").child("buffer");
    private DatabaseReference refArchive = FirebaseDatabase.getInstance().getReference().child("stats").child("matchArchive");
    private DatabaseReference refPlayers = FirebaseDatabase.getInstance().getReference().child("players");

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        StatsUpdate matchResult = dataSnapshot.getValue(StatsUpdate.class);
        Main.logger.info("Received StatsUpdate for match " + matchResult.getMatchId());
        refBuffer.child(dataSnapshot.getKey()).removeValue();
        List<Player.PlayerID> players = new ArrayList<>();
        players.addAll(matchResult.getWinners());
        players.addAll(matchResult.getLosers());
        for (Player.PlayerID id : players) {
            retrieveAndUpdateStats(id, matchResult);
        }
        refArchive.child(matchResult.getMatchId()).setValue(matchResult);
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

    private void retrieveAndUpdateStats(Player.PlayerID id, StatsUpdate matchResult) {
        Main.logger.info("Updating stats of player " + id.toString() + " after match " + matchResult.getMatchId());
        refStats.child(id.toString())
                .addListenerForSingleValueEvent(new StatsUpdater(id, matchResult));
    }

    private class StatsUpdater implements ValueEventListener {
        private StatsUpdate matchResult;
        private Player.PlayerID id;

        StatsUpdater(Player.PlayerID id, StatsUpdate matchResult) {
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
            Rank newRank = stats.updateRank(new NaiveCalculator(stats));
            refStats.child(dataSnapshot.getKey())
                    .setValue(stats);
            refPlayers.child(id.toString()).child("rank").setValue(newRank);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }
}
