package com.viifly.veebk.database;

import com.viifly.veebk.http.HttpServerVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.serviceproxy.ProxyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by vika on 7/25/17.
 */
public class BkmDatabaseVerticle extends AbstractVerticle {
  private static final Logger LOGGER = LoggerFactory.getLogger(BkmDatabaseVerticle.class);

  public static final String CONFIG_BKMDB_JDBC_URL = "bkmdb.jdbc.url";
  public static final String CONFIG_BKMDB_JDBC_DRIVER_CLASS = "bkmdb.jdbc.driver_class";
  public static final String CONFIG_BKMDB_JDBC_MAX_POOL_SIZE = "bkmdb.jdbc.max_pool_size";
  public static final String CONFIG_BKMDB_SQL_QUERIES_RESOURCE_FILE = "bkmdb.sqlqueries.resource.file";
  public static final String CONFIG_BKMDB_QUEUE = "bkmdb.queue";

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    HashMap<SqlQuery, String> sqlQueries = loadSqlQueries();

    JDBCClient dbClient = JDBCClient.createShared(vertx, new JsonObject()
      .put("url", config().getString(CONFIG_BKMDB_JDBC_URL, "jdbc:hsqldb:file:db/bkm"))
      .put("driver_class", config().getString(CONFIG_BKMDB_JDBC_DRIVER_CLASS, "org.hsqldb.jdbcDriver"))
      .put("max_pool_size", config().getInteger(CONFIG_BKMDB_JDBC_MAX_POOL_SIZE, 30)));

    //LOGGER.debug("JDBCClient created.");

    BkmDatabaseService.create(dbClient, sqlQueries, ready -> {

      if (ready.succeeded()) {
        ProxyHelper.registerService(BkmDatabaseService.class, vertx, ready.result(), CONFIG_BKMDB_QUEUE);
        startFuture.complete();
      } else {
        startFuture.fail(ready.cause());
      }
    });
  }

  /*
 * Note: this uses blocking APIs, but data is small...
 */
  private HashMap<SqlQuery, String> loadSqlQueries() throws IOException {
    String queriesFile = config().getString(CONFIG_BKMDB_SQL_QUERIES_RESOURCE_FILE);
    InputStream queriesInputStream;
    if (queriesFile != null) {
      queriesInputStream = new FileInputStream(queriesFile);
    } else {
      queriesInputStream = getClass().getResourceAsStream("/db-queries.properties");
    }

    Properties queriesProps = new Properties();
    queriesProps.load(queriesInputStream);
    queriesInputStream.close();

    HashMap<SqlQuery, String> sqlQueries = new HashMap<>();
    sqlQueries.put(SqlQuery.CREATE_BKM_TABLE, queriesProps.getProperty("create-bkm-table"));

    sqlQueries.put(SqlQuery.ALL_BKMS, queriesProps.getProperty("all-bkms"));
    sqlQueries.put(SqlQuery.GET_BKM, queriesProps.getProperty("get-bkm"));
    sqlQueries.put(SqlQuery.CREATE_BKM, queriesProps.getProperty("create-bkm"));
    sqlQueries.put(SqlQuery.SAVE_BKM, queriesProps.getProperty("save-bkm"));
    sqlQueries.put(SqlQuery.DELETE_BKM, queriesProps.getProperty("delete-bkm"));
    sqlQueries.put(SqlQuery.ALL_BKMS_DATA, queriesProps.getProperty("all-bkms-data"));
    sqlQueries.put(SqlQuery.GET_BKM_BY_ID, queriesProps.getProperty("get-bkm-by-id"));
    return sqlQueries;

  }
}
