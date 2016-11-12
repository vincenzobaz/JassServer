package stats;

import model.Player;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class StatsBufferListener implements ChildEventListener {
    private DatabaseReference refStats = FirebaseDatabase.getInstance().getReference().child("Stats").child("UserStats");
    private DatabaseReference refBuffer = FirebaseDatabase.getInstance().getReference().child("Stats").child("StatsBuffer");

    // TODO: Delete match upon reception? Store in private section of db?
    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        StatsUpdate matchResult = dataSnapshot.getValue(StatsUpdate.class);
        refBuffer.child(dataSnapshot.getKey()).removeValue();
        List<Player.PlayerID> players = new ArrayList<>();
        players.addAll(matchResult.getWinners());
        players.addAll(matchResult.getLosers());
        for (Player.PlayerID id : players) {
            retrieveAndUpdateStats(id, matchResult);
        }
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
        refStats.child(id.toString())
                .addListenerForSingleValueEvent(new StatsUpdater(matchResult));
    }

    private class StatsUpdater implements ValueEventListener {
        private StatsUpdate matchResult;

        StatsUpdater(StatsUpdate matchResult) {
            this.matchResult = matchResult;
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            UserStats stats = dataSnapshot.getValue(UserStats.class);
            stats.update(matchResult);
            stats.updateRank(new NaiveCalculator(stats));
            refStats.child(dataSnapshot.getKey())
                    .setValue(stats);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    }
}
