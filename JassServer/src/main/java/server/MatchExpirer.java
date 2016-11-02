package server;

import com.google.firebase.database.FirebaseDatabase;
import model.Match;

import java.util.TimerTask;

/**
 * Created by vinz on 10/31/16.
 */
public class MatchExpirer extends TimerTask{
    private final Match m;

    MatchExpirer(Match match) {
        this.m = match;
    }

    public void run() {
        FirebaseDatabase.getInstance().getReference()
                .child("matches")
                .child(m.getMatchID().toString())
                .removeValue();
        notifyDeleted();
    }

    private void notifyDeleted() {

    }
}
