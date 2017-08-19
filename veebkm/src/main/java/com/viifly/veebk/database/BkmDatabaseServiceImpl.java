package com.viifly.veebk.database;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by vika on 7/25/17.
 */
public class BkmDatabaseServiceImpl implements BkmDatabaseService {
  private static final Logger LOGGER = LoggerFactory.getLogger(BkmDatabaseServiceImpl.class);

  private final HashMap<SqlQuery, String> sqlQueries;
  private final JDBCClient dbClient;

  public BkmDatabaseServiceImpl(JDBCClient dbClient, HashMap<SqlQuery, String> sqlQueries, Handler<AsyncResult<BkmDatabaseService>> readyHandler) {
    this.dbClient = dbClient;
    this.sqlQueries = sqlQueries;

    dbClient.getConnection(ar -> {
      if (ar.failed()) {
        LOGGER.error("Could not open a database connection", ar.cause());
        readyHandler.handle(Future.failedFuture(ar.cause()));
      } else {
        SQLConnection connection = ar.result();
        connection.execute(sqlQueries.get(SqlQuery.CREATE_BKM_TABLE), create -> {
          connection.close();
          if (create.failed()) {
            LOGGER.error("Database preparation error", create.cause());
            readyHandler.handle(Future.failedFuture(create.cause()));
          } else {
            readyHandler.handle(Future.succeededFuture(this));
          }
        });
      }
    });
  }

  @Override
  public BkmDatabaseService fetchAllBookmarks(Handler<AsyncResult<JsonArray>> resultHandler) {
    dbClient.getConnection(car -> {
      if (car.succeeded()) {
        SQLConnection connection = car.result();
        connection.query(sqlQueries.get(SqlQuery.ALL_BKMS), res -> {
          connection.close();
          if (res.succeeded()) {
            JsonArray pages = new JsonArray(res.result()
              .getResults()
              .stream()
              .map(json -> json.getString(0))
              .sorted()
              .collect(Collectors.toList()));
            resultHandler.handle(Future.succeededFuture(pages));
          } else {
            LOGGER.error("Database query error", res.cause());
            resultHandler.handle(Future.failedFuture(res.cause()));
          }
        });
      } else {
        LOGGER.error("Database query error", car.cause());
        resultHandler.handle(Future.failedFuture(car.cause()));
      }
    });
    return this;
  }

  @Override
  public BkmDatabaseService fetchBookmark(String title, Handler<AsyncResult<JsonObject>> resultHandler) {
    dbClient.getConnection(car -> {
      if (car.succeeded()) {
        SQLConnection connection = car.result();
        connection.queryWithParams(sqlQueries.get(SqlQuery.GET_BKM), new JsonArray().add(title), fetch -> {
          connection.close();
          if (fetch.succeeded()) {
            JsonObject response = new JsonObject();
            ResultSet resultSet = fetch.result();
            if (resultSet.getNumRows() == 0) {
              response.put("found", false);
            } else {
              response.put("found", true);
              JsonArray row = resultSet.getResults().get(0);
              response.put("id", row.getInteger(0));
              response.put("url", row.getString(1));
            }
            resultHandler.handle(Future.succeededFuture(response));
          } else {
            LOGGER.error("Database query error", fetch.cause());
            resultHandler.handle(Future.failedFuture(fetch.cause()));
          }
        });
      } else {
        LOGGER.error("Database query error", car.cause());
        resultHandler.handle(Future.failedFuture(car.cause()));
      }
    });
    return this;
  }

  @Override
  public BkmDatabaseService fetchBookmarkById(int id, Handler<AsyncResult<JsonObject>> resultHandler) {
    dbClient.getConnection(car -> {
      if (car.succeeded()) {
        SQLConnection connection = car.result();
        connection.queryWithParams(sqlQueries.get(SqlQuery.GET_BKM_BY_ID), new JsonArray().add(id), res -> {
          if (res.succeeded()) {
            if (res.result().getNumRows() > 0) {
              JsonObject result = res.result().getRows().get(0);
              resultHandler.handle(Future.succeededFuture(new JsonObject()
                .put("found", true)
                .put("id", result.getInteger("ID"))
                .put("title", result.getString("TITLE"))
                .put("url", result.getString("URL"))));
            } else {
              resultHandler.handle(Future.succeededFuture(
                new JsonObject().put("found", false)));
            }
          } else {
            LOGGER.error("Database query error", res.cause());
            resultHandler.handle(Future.failedFuture(res.cause()));
          }
        });
      } else {
        LOGGER.error("Database query error", car.cause());
        resultHandler.handle(Future.failedFuture(car.cause()));
      }
    });
    return this;
  }

  @Override
  public BkmDatabaseService createBookmark(String title, String url, Handler<AsyncResult<Void>> resultHandler) {
    dbClient.getConnection(car -> {

      if (car.succeeded()) {
        SQLConnection connection = car.result();
        JsonArray data = new JsonArray().add(title).add(url);
        connection.updateWithParams(sqlQueries.get(SqlQuery.CREATE_BKM), data, res -> {
          connection.close();
          if (res.succeeded()) {
            resultHandler.handle(Future.succeededFuture());
          } else {
            LOGGER.error("Database query error", res.cause());
            resultHandler.handle(Future.failedFuture(res.cause()));
          }
        });
      } else {
        LOGGER.error("Database query error", car.cause());
        resultHandler.handle(Future.failedFuture(car.cause()));
      }
    });
    return this;
  }

  @Override
  public BkmDatabaseService saveBookmark(int id, String url, Handler<AsyncResult<Void>> resultHandler) {
    dbClient.getConnection(car -> {

      if (car.succeeded()) {
        SQLConnection connection = car.result();
        JsonArray data = new JsonArray().add(url).add(id);
        connection.updateWithParams(sqlQueries.get(SqlQuery.SAVE_BKM), data, res -> {
          connection.close();
          if (res.succeeded()) {
            resultHandler.handle(Future.succeededFuture());
          } else {
            LOGGER.error("Database query error", res.cause());
            resultHandler.handle(Future.failedFuture(res.cause()));
          }
        });
      } else {
        LOGGER.error("Database query error", car.cause());
        resultHandler.handle(Future.failedFuture(car.cause()));
      }
    });
    return this;
  }

  @Override
  public BkmDatabaseService deleteBookmark(int id, Handler<AsyncResult<Void>> resultHandler) {
    dbClient.getConnection(car -> {
      if (car.succeeded()) {
        SQLConnection connection = car.result();
        JsonArray data = new JsonArray().add(id);
        connection.updateWithParams(sqlQueries.get(SqlQuery.DELETE_BKM), data, res -> {
          connection.close();
          if (res.succeeded()) {
            resultHandler.handle(Future.succeededFuture());
          } else {
            LOGGER.error("Database query error", res.cause());
            resultHandler.handle(Future.failedFuture(res.cause()));
          }
        });
      } else {
        LOGGER.error("Database query error", car.cause());
        resultHandler.handle(Future.failedFuture(car.cause()));
      }
    });
    return this;
  }

  @Override
  public BkmDatabaseService fetchAllBookmarksData(Handler<AsyncResult<List<JsonObject>>> resultHandler) {
    dbClient.getConnection(car -> {
      if (car.succeeded()) {
        SQLConnection connection = car.result();
        connection.query(sqlQueries.get(SqlQuery.ALL_BKMS_DATA), queryResult -> {
          if (queryResult.succeeded()) {
            resultHandler.handle(Future.succeededFuture(queryResult.result().getRows()));
          } else {
            LOGGER.error("Database query error", queryResult.cause());
            resultHandler.handle(Future.failedFuture(queryResult.cause()));
          }
        });
      } else {
        LOGGER.error("Database query error", car.cause());
        resultHandler.handle(Future.failedFuture(car.cause()));
      }
    });
    return this;
  }

}
