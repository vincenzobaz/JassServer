package server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import model.Match;
import model.Player;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.TimerTask;

/**
 * Created by vinz on 11/1/16.
 */
public class ExpirationNotifier extends TimerTask {
    private Match m;
    private Jedis jedis = new Jedis("redis");
    private Gson gson = new Gson();

    ExpirationNotifier(Match m) {
        this.m = m;
    }

    @Override
    public void run() {
        List<Player> players = m.getPlayers();
        JsonArray tokens = new JsonArray();
        for (Player p : players) {
           tokens.add(jedis.get(p.getID().toString()));
        }
        JsonObject data = new JsonObject();
        data.addProperty("type", "matchexpired");
        data.addProperty("title", "Jass@EPFL");
        data.addProperty("body", "A match you joined expired!");
        data.addProperty("matchId", m.getMatchID());

        JsonObject msg = new JsonObject();
        msg.add("data", data);
        msg.add("registration_ids", tokens);
        try {
            Main.logger.info("Match " + m.getMatchID() + " has expired");
            Main.logger.info(Unirest.post(Main.FCM_URL).body(gson.toJson(msg)).asString().getBody());
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }
}
