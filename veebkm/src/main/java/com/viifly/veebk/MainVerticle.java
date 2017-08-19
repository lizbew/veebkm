package com.viifly.veebk;

import com.viifly.veebk.database.BkmDatabaseVerticle;
import com.viifly.veebk.http.HttpServerVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by vika on 7/25/17.
 * https://github.com/vert-x3/vertx-guide-for-java-devs/tree/master/step-7
 */
public class MainVerticle extends AbstractVerticle {
  private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public void start(Future<Void> startFuture) throws Exception {

    Future<String> dbVerticleDeployment = Future.future();
    vertx.deployVerticle(new BkmDatabaseVerticle(), dbVerticleDeployment.completer());

    dbVerticleDeployment.compose(id -> {

      Future<String> httpVerticleDeployment = Future.future();
      vertx.deployVerticle(
        "com.viifly.veebk.http.HttpServerVerticle",
        new DeploymentOptions().setInstances(2),
        httpVerticleDeployment.completer()
        );
      LOGGER.debug("in httpVerticleDeployment");
      return httpVerticleDeployment;
    }).setHandler(ar -> {
      LOGGER.debug("start end. {}", ar);

      if (ar.succeeded()) {
        startFuture.complete();
      } else {
        startFuture.fail(ar.cause());
      }
    });
  }


}
