package com.viifly.veebk.database;

import com.sun.corba.se.impl.orb.ParserTable;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by vika on 8/6/17.
 */
@RunWith(VertxUnitRunner.class)
public class BkmDatabaseServiceTest {

  private Vertx vertx;
  private BkmDatabaseService service;

  @Before
  public void prepare(TestContext context) throws InterruptedException {
    vertx = Vertx.vertx();

    JsonObject conf = new JsonObject()
      .put(BkmDatabaseVerticle.CONFIG_BKMDB_JDBC_URL, "jdbc:hsqldb:mem:testdb;shutdown=true")
      .put(BkmDatabaseVerticle.CONFIG_BKMDB_JDBC_MAX_POOL_SIZE, 4);

    vertx.deployVerticle(new BkmDatabaseVerticle(), new DeploymentOptions().setConfig(conf),
      context.asyncAssertSuccess(id ->
      service = BkmDatabaseService.createProxy(vertx, BkmDatabaseVerticle.CONFIG_BKMDB_QUEUE)));
  }

  @After
  public void finish(TestContext context){
    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public void crud_operations(TestContext context) {
    Async async = context.async();

    service.createBookmark("test Page", "http://localhost:8080", context.asyncAssertSuccess(v1 -> {
      service.fetchBookmark("test Page", context.asyncAssertSuccess(json1 -> {
        context.assertTrue(json1.getBoolean("found"));
        context.assertTrue(json1.containsKey("id"));
        context.assertEquals("http://localhost:8080", json1.getString("url"));

        service.saveBookmark(json1.getInteger("id"), "https://localhost:8080", context.asyncAssertSuccess(v2 -> {
          service.fetchAllBookmarks(context.asyncAssertSuccess(array1 -> {
            context.assertEquals(1, array1.size());

            service.fetchBookmark("test Page", context.asyncAssertSuccess(json2 -> {
              context.assertEquals("https://localhost:8080", json2.getString("url"));

              service.deleteBookmark(json1.getInteger("id"), v3 -> {
                service.fetchAllBookmarks(context.asyncAssertSuccess(array2 -> {
                  context.assertTrue(array2.isEmpty());

                  async.complete();
                }));
              });
            }));
          }));
        }));
      }));
    }));

  }


}
