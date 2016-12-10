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
    private DatabaseReference root = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference refStats = root.child("userStats");

    // Read about finished games
    private DatabaseReference refBuffer = root.child("stats").child("buffer");

    // MatchStats archive
    private DatabaseReference refMatchStatsArchive = root.child("stats").child("matchStatsArchive");

    // Delete match we received matchStats of
    private DatabaseReference refMatches = root.child("matches");
    // Delete matchStats upon receiving matchStats in buffer
    private DatabaseReference refMatchStats = root.child("matchStats");

    // Update quote
    private DatabaseReference refPlayers = root.child("players");

    private DatabaseReference refMatchArchive = root.child("stats").child("matchArchive");

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        MatchStats matchResult = dataSnapshot.getValue(MatchStats.class);
        Main.logger.info("Received StatsUpdate for match " + matchResult.getMatchID());

        for (Player p : matchResult.getMatch().getPlayers()) {
            retrieveAndUpdateStats(p.getID(), matchResult);
        }

        refMatchStatsArchive.child(matchResult.getMatchID()).setValue(matchResult);
        refMatches.child(matchResult.getMatchID()).addListenerForSingleValueEvent(new ValueEventListener() {
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

            updateUserStats(stats, stats.getPlayerId().toString());
        }
        
        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
        
        private void updateUserStats(UserStats stats, String id) {
			refStats.child(id).child("playerId").setValue(stats.getPlayerId());
			refStats.child(id).child("playedMatches").setValue(stats.getPlayedMatches());
			refStats.child(id).child("wonMatches").setValue(stats.getWonMatches());
			refStats.child(id).child("playedByDate").setValue(stats.getPlayedByDate());
			refStats.child(id).child("wonByDate").setValue(stats.getWonByDate());
			refStats.child(id).child("quoteByDate").setValue(stats.getQuoteByDate());
			refStats.child(id).child("variants").setValue(stats.getVariants());
			refStats.child(id).child("partners").setValue(stats.getPartners());
			refStats.child(id).child("wonWith").setValue(stats.getWonWith());
		}
    }
}
