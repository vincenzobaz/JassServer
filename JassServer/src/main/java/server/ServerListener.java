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

import static java.util.stream.Collectors.toList;

class ServerListener implements ChildEventListener {
    private Map<String, Match> matches;
    private final Timer timer;
    private final Gson gson;
    private Jedis jedis;

    ServerListener() {
        Unirest.setDefaultHeader("Content-Type", "application/json");
        Unirest.setDefaultHeader("Authorization", Main.FCM_KEY);
        this.jedis = new Jedis("redis");
        this.matches = new HashMap<>();
        this.timer = new Timer(true);
        gson = new Gson();
        jedis = new Jedis(Main.REDIS_URL);
    }

    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        String id = dataSnapshot.getKey();
        Match m = dataSnapshot.getValue(Match.class);
        matches.put(id, m);

        Date expirationDate = new Date(m.getExpirationTime());
        timer.schedule(new ExpirationNotifier(m), expirationDate);
        if (Main.DELETE_EXPIRED) {
            timer.schedule(new MatchExpirer(m), expirationDate);
        }
    }

    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        String matchId = dataSnapshot.getKey();
        Match newMatch = dataSnapshot.getValue(Match.class);
        Match oldMatch = matches.get(matchId);
        matches.put(matchId, newMatch);

        List<Player> newPlayers = newMatch.getPlayers();
        List<Player> oldPlayers = oldMatch.getPlayers();

        if (newPlayers.size() == newMatch.getMaxPlayerNumber()) {
            notifyFull(matchId, newPlayers);
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
        List<String> res = new ArrayList<>(players.size());
        return players.stream().map(p -> p.getID().toString()).collect(Collectors.toList());
    }

    private void notifyLeaveMatch(String traitor, String matchId, List<Player> remaining) {
        JsonObject data = new JsonObject();
        data.addProperty("type", "playerleft");
        data.addProperty("matchId", matchId);
        data.addProperty("sciper", traitor);
        data.addProperty("title", "A player left your match!");
        data.addProperty("text", "Tap to see who is still in");

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
        data.addProperty("title", "A player joined your match!");
        data.addProperty("text", "Tap to see who it is");

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
        data.addProperty("title", "The match you joined is full!");
        data.addProperty("text", "Tap to know more");

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
