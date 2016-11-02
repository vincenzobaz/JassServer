package server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.Unirest;
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
        data.addProperty("title", "A match you joined expired!");
        data.addProperty("text", "Tap to join another one!");

        JsonObject msg = new JsonObject();
        msg.add("data", data);
        msg.add("registration_ids", tokens);
        Unirest.post(Main.FCM_URL).body(gson.toJson(msg));
    }
}
