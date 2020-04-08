package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

public class MainVerticle extends AbstractVerticle {

    private DBConnector connector;
    private BackgroundPoller poller = new BackgroundPoller();

    @Override
    public void start(Future<Void> startFuture) {
        connector = new DBConnector(vertx);
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        vertx.setPeriodic(1000 * 60, timerId -> poller.pollServices());
        setRoutes(router);
        vertx
                .createHttpServer()
                .requestHandler(router)
                .listen(8080, result -> {
                    if (result.succeeded()) {
                        System.out.println("KRY code test service started");
                        startFuture.complete();
                    } else {
                        startFuture.fail(result.cause());
                    }
                });
    }

    private void setRoutes(Router router) {
        router.route("/*").handler(StaticHandler.create());

        router.get("/service").handler(req -> {

            connector.query("SELECT id,url,description, status,creation_time, last_poll_time FROM service").setHandler(event -> {
                List<JsonObject> jsonServices = event.result().getRows()
                        .stream()
                        .map(this::convertRowToJsonObject)
                        .collect(Collectors.toList());

                req.response()
                        .putHeader("content-type", "application/json")
                        .end(new JsonArray(jsonServices).encode());


            });

        });

        router.post("/service").handler(req -> {
            JsonObject jsonBody = req.getBodyAsJson();

            connector.query("INSERT INTO service(url, description, creation_time) VALUES(?,?,?)",
                    new JsonArray().add(jsonBody.getString("url")).
                            add(jsonBody.getString("description")).add(System.currentTimeMillis()))
                    .setHandler(event -> {
                        req.response().setStatusCode(200).end();

                    });

        });

    }

    private JsonObject convertRowToJsonObject(JsonObject row) {
        return new JsonObject()
                .put("id", row.getInteger("id"))
                .put("url", row.getString("url"))
                .put("status", row.getString("status"))
                .put("creationTime", dateTimeToString(row.getLong("creation_time")))
                .put("lastPollTime", dateTimeToString(row.getLong("last_poll_time")))
                .put("description", row.getString("description"));
    }

    private String dateTimeToString(Long dateTime) {
        if (dateTime == null)
            return null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Timestamp(dateTime));
    }
}



