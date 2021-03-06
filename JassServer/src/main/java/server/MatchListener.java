package server;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import model.Match;
import model.Player;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.stream.Collectors;

class MatchListener implements ChildEventListener {
    private final Map<String, Match> matches;
    private final Map<String, Boolean> shoudNotifyfull;
    private final Timer timer;
    private final Gson gson;
    private final Jedis jedis;

    MatchListener() {
        Unirest.setDefaultHeader("Content-Type", "application/json");
        Unirest.setDefaultHeader("Authorization", Main.FCM_KEY);
        this.matches = new HashMap<>();
        this.timer = new Timer(true);
        this.gson = new Gson();
        this.jedis = new Jedis(Main.REDIS_URL);
        this.shoudNotifyfull = new HashMap<>();
    }

    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        String id = dataSnapshot.getKey();
        Match m = dataSnapshot.getValue(Match.class);
        matches.put(id, m);

        Date expirationDate = new Date(m.getTime());
        timer.schedule(new ExpirationNotifier(m), expirationDate);
        if (Main.DELETE_EXPIRED) {
            timer.schedule(new MatchExpirer(m.getMatchID()), expirationDate);
        }
    }

    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        String matchId = dataSnapshot.getKey();
        Match newMatch = dataSnapshot.getValue(Match.class);
        Match oldMatch = matches.get(matchId);
        matches.put(matchId, newMatch);

        List<Player> newPlayers = newMatch.getPlayers();
        List<Player> oldPlayers = oldMatch.getPlayers();

        if (newPlayers.size() == newMatch.getMaxPlayerNumber() && shoudNotifyfull.getOrDefault(matchId, true)) {
            notifyFull(matchId, newPlayers);
            shoudNotifyfull.put(matchId, false);
        } else if (newPlayers.size() == oldPlayers.size() + 1) {
            Player lastArrived = newPlayers.get(oldPlayers.size());
            notifyJoinMatch(lastArrived.getID().toString(), oldMatch.getMatchID(), oldPlayers);
        } else if (newPlayers.size() + 1 == oldPlayers.size()) {
            List<String> oldScipers = collectScipers(oldPlayers);
            List<String> newScipers = collectScipers(newPlayers);
            oldScipers.removeAll(newScipers);
            notifyLeaveMatch(oldScipers.get(0), matchId, newPlayers);
        }
    }

    private List<String> collectScipers(List<Player> players) {
        return players.stream().map(p -> p.getID().toString()).collect(Collectors.toList());
    }

    private void notifyLeaveMatch(String traitor, String matchId, List<Player> remaining) {
        JsonObject data = new JsonObject();
        data.addProperty("type", "playerleft");
        data.addProperty("matchId", matchId);
        data.addProperty("sciper", traitor);
        data.addProperty("title", "Jass@EPFL");
        data.addProperty("body", "A player left your match!");

        JsonObject msg = new JsonObject();
        msg.add("data", data);
        msg.add("registration_ids", getIds(remaining));
        Main.logger.info("Player " + traitor + " left match " + matchId);
        try {
            Main.logger.info(Unirest.post(Main.FCM_URL).body(gson.toJson(msg)).asString().getBody());
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    private void notifyJoinMatch(String sciper, String matchID, List<Player> oldPlayers) {
        JsonObject data = new JsonObject();
        data.addProperty("type", "playerjoined");
        data.addProperty("matchId", matchID);
        data.addProperty("sciper", sciper);
        data.addProperty("title", "Jass@EPFL");
        data.addProperty("body", "A player joined your match!");

        JsonObject msg = new JsonObject();
        msg.add("data", data);
        msg.add("registration_ids", getIds(oldPlayers));
        Main.logger.info("Player " + sciper + " has joined match " + matchID);
        try {
            Main.logger.info(Unirest.post(Main.FCM_URL).body(gson.toJson(msg)).asString().getBody());
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    private void notifyFull(String id, List<Player> players) {
        JsonObject data = new JsonObject();
        data.addProperty("type", "matchfull");
        data.addProperty("matchId", id);
        data.addProperty("title", "Jass@EPFL");
        data.addProperty("body", "The match you joined is full!");

        JsonObject msg = new JsonObject();
        msg.add("registration_ids", getIds(players));
        msg.add("data", data);
        Main.logger.info("Match " + id + " is full");
        try {
            Main.logger.info(Unirest.post(Main.FCM_URL).body(gson.toJson(msg)).asString().getBody());
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    private JsonArray getIds(List<Player> players) {
        JsonArray res = new JsonArray();
        for (Player p : players) {
            res.add(jedis.get(p.getID().toString()));
        }
        return res;
    }

    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    public void onCancelled(DatabaseError databaseError) {

    }
}
