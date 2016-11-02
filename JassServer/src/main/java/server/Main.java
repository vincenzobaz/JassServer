package server;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import redis.clients.jedis.Jedis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static spark.Spark.*;

public class Main {
    static String FCM_KEY;
    static String FCM_URL;
    static boolean DELETE_EXPIRED;
    static String REDIS_URL;

    public static void main(String[] args) throws FileNotFoundException, UnirestException {
        Logger logger = LoggerFactory.getLogger(Main.class);

        FCM_KEY = "key=" + System.getenv("FCM_KEY");
        FCM_URL = System.getenv("FCM_URL");
        REDIS_URL = System.getenv("REDIS_HOST");
        DELETE_EXPIRED = Boolean.parseBoolean(System.getenv("DELETE_EXPIRED"));
        String Database = System.getenv("FIREBASE_DB");
        String FirebaseKey = System.getenv("FIREBASE_KEY");
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setServiceAccount(new FileInputStream(FirebaseKey))
                .setDatabaseUrl(Database)
                .build();
        FirebaseApp.initializeApp(options);
        ServerListener s = new ServerListener();
        FirebaseDatabase.getInstance().getReference().child("matches").addChildEventListener(s);
        System.out.println("Started listener");

        Unirest.setDefaultHeader("Content-Type", "application/json");
        Unirest.setDefaultHeader("Authorization", FCM_KEY);

        Gson gson = new Gson();
        Jedis jedis = new Jedis(REDIS_URL);

        threadPool(8);
        port(9999);

        post("/register", (req, res) -> {
            JsonObject body = gson.fromJson(req.body(), JsonObject.class);
            String sciper = body.get("sciper").getAsString();
            String token = body.get("token").getAsString();
            logger.info("Registered player " + " with token " + token);
            jedis.set(sciper, token);
            return "registered";
        });

        post("/invite", (req, res) -> {
            JsonObject jBody = gson.fromJson(req.body(), JsonObject.class);
            String sciper = jBody.get("sciper").getAsString();
            String matchId = jBody.get("matchId").getAsString();
            String by = jBody.get("by").getAsString();
            logger.info("Player " + by + " invited " + sciper + " to " + matchId);

            JsonObject data = new JsonObject();
            data.addProperty("type", "invite");
            data.addProperty("matchId", matchId);
            data.addProperty("by", by);
            data.addProperty("title", "You have been invited to join a match");
            data.addProperty("text", "Tap to join or refuse");

            JsonObject msg = new JsonObject();
            msg.addProperty("to", jedis.get(sciper));
            msg.add("data", data);

            Unirest.post(FCM_URL).body(gson.toJson(msg));

            return "invited";
        });
    }
}
