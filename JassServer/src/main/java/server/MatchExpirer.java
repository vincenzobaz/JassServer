package server;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import model.Match;

import java.util.TimerTask;

/**
 * Created by vinz on 10/31/16.
 */
public class MatchExpirer extends TimerTask{
    private final String matchId;

    MatchExpirer(String matchId) {
        this.matchId = matchId;
    }

    public void run() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        
        ref.child("matches").child(matchId).removeValue();
        ref.child("pendingMatches").child(matchId).removeValue();
    }
}
