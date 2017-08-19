package com.viifly.veebk.database;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

import java.util.HashMap;
import java.util.List;

/**
 * Created by vika on 7/25/17.
 */
@ProxyGen
public interface BkmDatabaseService {
  static BkmDatabaseService create(JDBCClient dbClient, HashMap<SqlQuery, String> sqlQueries, Handler<AsyncResult<BkmDatabaseService>> readyHandler) {
    return new BkmDatabaseServiceImpl(dbClient, sqlQueries, readyHandler);
  }

  static BkmDatabaseService createProxy(Vertx vertx, String address) {
    return new BkmDatabaseServiceVertxEBProxy(vertx, address);
  }


  @Fluent
  BkmDatabaseService fetchAllBookmarks(Handler<AsyncResult<JsonArray>> resultHandler);

  @Fluent
  BkmDatabaseService fetchBookmark(String title, Handler<AsyncResult<JsonObject>> resultHandler);

  @Fluent
  BkmDatabaseService fetchBookmarkById(int id, Handler<AsyncResult<JsonObject>> resultHandler);

  @Fluent
  BkmDatabaseService createBookmark(String title, String url, Handler<AsyncResult<Void>> resultHandler);

  @Fluent
  BkmDatabaseService saveBookmark(int id, String url, Handler<AsyncResult<Void>> resultHandler);

  @Fluent
  BkmDatabaseService deleteBookmark(int id, Handler<AsyncResult<Void>> resultHandler);

  @Fluent
  BkmDatabaseService fetchAllBookmarksData(Handler<AsyncResult<List<JsonObject>>> resultHandler);
}
