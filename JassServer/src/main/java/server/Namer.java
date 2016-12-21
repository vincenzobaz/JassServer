package server;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import model.Player;
import redis.clients.jedis.Jedis;

/**
 * Created by vinz on 12/21/16.
 */
public class Namer implements ChildEventListener {
    private final Jedis jedis = new Jedis(Main.REDIS_URL);

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Player p = dataSnapshot.getValue(Player.class);
        jedis.set(p.getID().toString() + 'N', p.getFirstName());
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
