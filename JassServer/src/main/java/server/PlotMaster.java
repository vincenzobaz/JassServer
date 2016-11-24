package server;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.eclipse.jetty.server.Authentication;
import stats.UserStats;

import java.util.Map;

/**
 * Created by vinz on 11/24/16.
 */
public class PlotMaster implements ChildEventListener {
    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        UserStats stats = dataSnapshot.getValue(UserStats.class);
        allGraphs(stats);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        UserStats stats = dataSnapshot.getValue(UserStats.class);
        allGraphs(stats);
    }

    private void allGraphs(UserStats stats) {
        generateBar(stats.getVariants(), stats.getPlayerId().toString(), "variants");
        generateBar(stats.getPartners(), stats.getPlayerId().toString(), "partners");
        generateBar(stats.getWonWith(), stats.getPlayerId().toString(), "wonWith");
    }

    private void generateBar(Map<String, Integer> d, String playerId, String graph) {
        JsonObject body = preparePayload(d, playerId, graph);
        try {
	    Main.logger.info("Sending request for " + new Gson().toJson(body));
            Main.logger.info("Sent bar plot request" + Unirest.post("http://graphplotter:5000/bar")
                    .header("Content-Type", "application/json")
                    .body(new Gson().toJson(body)).asString().getBody());
        } catch (UnirestException e) {
            Main.logger.error("Plot request failed " + e.getMessage());
        }
    }

    private JsonObject preparePayload(Map<String, Integer> d, String playerId, String graph) {
        JsonArray labels = new JsonArray();
        JsonArray values = new JsonArray();
        d.keySet().forEach(k -> {
            labels.add(k);
            values.add(d.get(k));
        });
        JsonObject body = new JsonObject();
        body.add("labels", labels);
        body.add("values", values);
        body.addProperty("sciper", playerId);
        body.addProperty("xlabel", "Jass Variants");
        body.addProperty("ylabel", "Matches Played");
        body.addProperty("graph", graph);
        return body;
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
