package stats;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.RequestBodyEntity;
import server.Main;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by vinz on 11/24/16.
 */
public class PlotMaster implements ChildEventListener {
    private final Gson gson = new Gson();
    private static final String PLOTTER_URL = "http://graphplotter:5000";

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
        generateBars(stats, stats.getPlayerId().toString());
        generateTimes(stats, stats.getPlayerId().toString());
    }

    private void generateTimes(UserStats stats, String s) {
        String id = stats.getPlayerId().toString();
        JsonObject played = preparePayloadTime(stats.getPlayedByDate(), id, "played");
        JsonObject won = preparePayloadTime(stats.getPlayedByDate(), id, "won");
        List<Tuple2<Long, Integer>> ranks = stats.getRankByDate().stream()
                .map(tuple -> new Tuple2<>(tuple.getKey(), tuple.getValue().getRank()))
                .collect(Collectors.toList());
        JsonObject rank = preparePayloadTime(ranks, id, "rank");

        JsonObject payload = new JsonObject();
        payload.add("played", played);
        payload.add("won", won);
        payload.add("rank", rank);
        String body = gson.toJson(payload);
        RequestBodyEntity req = Unirest.post(PLOTTER_URL + "/times")
                .header("Content-Type", "application/json")
                .body(body);
        Main.logger.info("Sending request for " + body);
        Main.logger.info("Response " + req.getBody());
    }

    private JsonObject preparePayloadTime(List<Tuple2<Long, Integer>> timeSeries, String playerId, String graph) {
        JsonArray dates = new JsonArray();
        JsonArray ints = new JsonArray();
        timeSeries.forEach(x -> {
            dates.add(x.getKey());
            ints.add(x.getValue());
        });
        JsonObject body = new JsonObject();
        body.add("dates", dates);
        body.add("ints", ints);
        body.addProperty("sciper", playerId);
        String xAxis = "Date";
        String yAxis = "";
        switch (graph) {
            case "played":
                yAxis = "Matches played";
                break;
            case "won":
                yAxis = "Matches won";
                break;
            case "rank":
                yAxis = "rank";
                break;
        }
        body.addProperty("xlabel", xAxis);
        body.addProperty("ylabel", yAxis);
        body.addProperty("graph", graph);
        return body;
    }

    private void generateBars(UserStats stats, String s) {
        JsonObject variants = preparePayloadBars(stats.getVariants(), s, "variants");
        JsonObject partners = preparePayloadBars(stats.getPartners(), s, "partners");
        JsonObject wonWith = preparePayloadBars(stats.getWonWith(), s, "wonWith");

        JsonObject payload = new JsonObject();
        payload.add("variants", variants);
        payload.add("partners", partners);
        payload.add("wonWith", wonWith);
        try {
            RequestBodyEntity req = Unirest.post(PLOTTER_URL + "/bars")
                    .header("Content-Type", "application/json")
                    .body(gson.toJson(payload));
            Main.logger.info("Sending request for " + gson.toJson(payload));
            Main.logger.info("Sent bar plot request " + req.asString().getBody());
            Main.logger.info("Request response was " + req.getBody());
        } catch (UnirestException e) {
            Main.logger.error("Plot request failed " + e.getMessage());
        }
    }

    private JsonObject preparePayloadBars(Map<String, Integer> d, String playerId, String graph) {
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
        String xAxis = "";
        String yAxis = "";
        switch (graph) {
            case "variants":
                xAxis = "Jass Variants";
                yAxis = "Matches played";
                break;
            case "partners":
                xAxis = "Team mates";
                yAxis = "Matches played";
                break;
            case "woWith":
                xAxis = "Team mates";
                yAxis = "Matches won";
                break;
        }
        body.addProperty("xlabel", xAxis);
        body.addProperty("ylabel", yAxis);
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
