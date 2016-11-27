package server;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.RequestBodyEntity;
import org.eclipse.jetty.server.Authentication;
import stats.UserStats;

import java.util.Map;

/**
 * Created by vinz on 11/24/16.
 */
public class PlotMaster implements ChildEventListener {
    private final Gson gson = new Gson();

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        /*
        UserStats stats = dataSnapshot.getValue(UserStats.class);
        allGraphs(stats);
        */
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        UserStats stats = dataSnapshot.getValue(UserStats.class);
        allGraphs(stats);
    }

    private void allGraphs(UserStats stats) {
        generateBars(stats, stats.getPlayerId().toString());//, "variants");
    }

    private void generateBars(UserStats stats, String s) {
        JsonObject variants = preparePayload(stats.getVariants(), s, "variants");
        JsonObject partners = preparePayload(stats.getPartners(), s, "partners");
        JsonObject wonWith = preparePayload(stats.getWonWith(), s, "wonWith");

        JsonObject payload = new JsonObject();
        payload.add("variants", variants);
        payload.add("partners", partners);
        payload.add("wonWith", wonWith);
        try {
            RequestBodyEntity req = Unirest.post("http://graphplotter:5000/bars")
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(payload));
            Main.logger.info("Sending request for " + gson.toJson(payload));
            Main.logger.info("Sent bar plot request " + req.asString().getBody());
            Main.logger.info("Request response was " + req.getBody());
        } catch (UnirestException e) {
            Main.logger.error("Plot request failed " + e.getMessage());
        }
    }

    private void generateBar(Map<String, Integer> d, String playerId, String graph) {
        JsonObject body = preparePayload(d, playerId, graph);
        try {
            RequestBodyEntity req = Unirest.post("http://graphplotter:5000/bar")
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(body));
            Main.logger.info("Sending request for " + gson.toJson(body));
            Main.logger.info("Sent bar plot request" + req.asString().getBody());
            Main.logger.info("Request response was " + req.getBody());
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
