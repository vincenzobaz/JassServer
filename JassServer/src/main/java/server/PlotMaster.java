package server;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import stats.UserStats;

import java.util.Map;

/**
 * Created by vinz on 11/24/16.
 */
public class PlotMaster implements ChildEventListener {
    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        generatePlot(dataSnapshot.getValue(UserStats.class));
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        generatePlot(dataSnapshot.getValue(UserStats.class));
    }

    private void generatePlot(UserStats data) {
        Map<String, Integer> d = data.getVariants();
        JsonArray labels = new JsonArray();
        JsonArray values = new JsonArray();
        d.keySet().forEach(k -> {
            labels.add(k);
            values.add(d.get(k));
        });
        JsonObject body = new JsonObject();
        body.add("labels", labels);
        body.add("values", values);
        body.addProperty("sciper", data.getPlayerId().toString());
        body.addProperty("xlabel", "Jass Variants");
        body.addProperty("ylabel", "Matches Played");
	body.addProperty("graph", "variants");
        try {
            Main.logger.info("Sent plot request" + Unirest.post("http://graphplotter:5000/bar")
                    .header("Content-Type", "application/json")
                    .body(new Gson().toJson(body)).asString().getBody());
        } catch (UnirestException e) {
            Main.logger.error("Plot request failed " + e.getMessage());
        }
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
