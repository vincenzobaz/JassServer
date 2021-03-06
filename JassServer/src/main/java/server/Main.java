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
import stats.PlotMaster;
import stats.StatsBufferListener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static spark.Spark.*;

public class Main {
    static String FCM_KEY;
    static String FCM_URL;
    static boolean DELETE_EXPIRED;
    public static final String REDIS_URL = "redis";
    public static Logger logger = LoggerFactory.getLogger(Main.class);
    static Gson gson;
    static Jedis jedis;

    public static void main(String[] args) throws FileNotFoundException, UnirestException {

        FCM_KEY = "key=" + System.getenv("FCM_KEY");
        FCM_URL = System.getenv("FCM_URL");
        DELETE_EXPIRED = Boolean.parseBoolean(System.getenv("DELETE_EXPIRED"));
        String Database = System.getenv("FIREBASE_DB");
        String FirebaseKey = System.getenv("FIREBASE_KEY");

        gson = new Gson();
        jedis = new Jedis(REDIS_URL);
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setServiceAccount(new FileInputStream(FirebaseKey))
                .setDatabaseUrl(Database)
                .build();
        FirebaseApp.initializeApp(options);

        FirebaseDatabase.getInstance().getReference().child("matches")
                .addChildEventListener(new MatchListener());
        System.out.println("Started matches listener");

        FirebaseDatabase.getInstance().getReference()
                .child("stats").child("buffer").addChildEventListener(new StatsBufferListener());

        FirebaseDatabase.getInstance().getReference()
                .child("userStats").addChildEventListener(new PlotMaster());

        FirebaseDatabase.getInstance().getReference()
                .child("players").addChildEventListener(new Namer());

        Unirest.setDefaultHeader("Content-Type", "application/json");
        Unirest.setDefaultHeader("Authorization", FCM_KEY);


        threadPool(8);
        port(9999);

        post("/register", (req, res) -> {
            JsonObject body = gson.fromJson(req.body(), JsonObject.class);
            if (!validRegister(body)) {
                res.status(400);
                logger.error("bad request received, dropping");
                return "bad request";
            }
            String sciper = body.get("sciper").getAsString();
            String token = body.get("token").getAsString();
            logger.info("Registered player " + sciper + " with token " + token);
            jedis.set(sciper, token);
            return "registered";
        });

        post("/invite", (req, res) -> {
            JsonObject jBody = gson.fromJson(req.body(), JsonObject.class);
            if (!validInvite(jBody)) {
                res.status(400);
                logger.error("bad request received, dropping");
                return "bad request";
            }
            String sciper = jBody.get("sciper").getAsString();
            String matchId = jBody.get("matchId").getAsString();
            String by = jBody.get("by").getAsString();
            logger.info("Player " + by + " invited " + sciper + " to " + matchId);

            JsonObject data = new JsonObject();
            data.addProperty("type", "invite");
            data.addProperty("matchId", matchId);
            data.addProperty("by", by);
            data.addProperty("title", "Jass@EPFL");
            data.addProperty("body", "You have invited to join a match");

            JsonObject msg = new JsonObject();
            msg.addProperty("to", jedis.get(sciper));
            msg.add("data", data);

            logger.info(Unirest.post(FCM_URL).body(gson.toJson(msg)).asString().getBody());

            return "invited";
        });
    }

    private static boolean validInvite(JsonObject data) {
        return data.has("sciper") &&
                data.has("matchId") &&
                data.has("by") &&
                jedis.exists(data.get("by").getAsString()) &&
                jedis.exists(data.get("sciper").getAsString());
    }

    private static boolean validRegister(JsonObject data) {
        return data.has("sciper") && data.has("token");
    }
}
