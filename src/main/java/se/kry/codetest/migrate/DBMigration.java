package se.kry.codetest.migrate;

import io.vertx.core.Vertx;
import se.kry.codetest.DBConnector;

public class DBMigration {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    DBConnector connector = new DBConnector(vertx);
    connector.query("create table service\n" +
            "(\n" +
            "\turl VARCHAR(128) not null,\n" +
            "\tdescription VARCHAR(128),\n" +
            "\tstatus VARCHAR(10),\n" +
            "\tid INTEGER\n" +
            "\t\tconstraint service_pk\n" +
            "\t\t\tprimary key autoincrement,\n" +
            "\tcreation_time INTEGER,\n" +
            "\tlast_poll_time integer\n" +
            ");\n" +
            "\n").setHandler(done -> {
      if(done.succeeded()){

        System.out.println("completed db migrations");
      } else {
        done.cause().printStackTrace();
      }
      vertx.close(shutdown -> {
        System.exit(0);
      });
    });
  }
}
