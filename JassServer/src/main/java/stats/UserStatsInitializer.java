package stats;

import com.google.firebase.database.*;
import model.Player;
import server.Main;

/**
 * Created by vinz on 11/13/16.
 */
public class UserStatsInitializer implements ChildEventListener {
    private DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
            .child("Stats").child("UserStats");

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Player p = dataSnapshot.getValue(Player.class);
        ref.child(p.getID().toString()).setValue(new UserStats(p.getID()));
        Main.logger.info("New player " + p.getID().toString() + " registered, creating its stats");
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
}
