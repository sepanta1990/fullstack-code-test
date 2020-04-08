package se.kry.codetest;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.client.WebClient;

public class BackgroundPoller {

    private final DBConnector connector = new DBConnector(Vertx.vertx());
    private final WebClient client = WebClient.create(Vertx.vertx());

    public void pollServices() {

        System.out.println("Starting poll service");
        connector.query("SELECT url FROM service").setHandler(response -> {
            if (response.succeeded()) {
                response.result().getRows().forEach(row -> {
                    try {
                        String url = row.getString("url");
                        System.out.println("Updating url: " + url);

                        client.getAbs(url).send(ar -> {
                            String status = "FAIL";
                            if (ar.succeeded()) {
                                status = "OK";
                            } else {
                                System.out.println("Error on calling url: " + url);
                                ar.cause().printStackTrace();
                            }
                            connector.query("UPDATE service SET status =?, last_poll_time=? WHERE url=?",
                                    new JsonArray().add(status).add(System.currentTimeMillis()).add(url))
                                    .setHandler(res -> {
                                        if (res.succeeded()) {
                                            System.out.println("Updated successfully");
                                        } else {
                                            res.cause().printStackTrace();
                                        }
                                    });


                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            } else {
                System.out.println("Polling failed");
                response.cause().printStackTrace();
            }

        });
    }
}
