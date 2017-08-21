package com.viifly.veebk.http;

import com.viifly.veebk.database.BkmDatabaseService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.ext.auth.shiro.ShiroAuth;
import io.vertx.ext.auth.shiro.ShiroAuthOptions;
import io.vertx.ext.auth.shiro.ShiroAuthRealmType;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.sstore.LocalSessionStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Created by vika on 7/25/17.
 */
public class HttpServerVerticle extends AbstractVerticle {

  public static final String CONFIG_HTTP_SERVER_PORT = "http.server.port";
  public static final String CONFIG_BKMDB_QUEUE = "bkmdb.queue";

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);


  private BkmDatabaseService dbService;

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    String bkmDbQueue = config().getString(CONFIG_BKMDB_QUEUE, "bkmdb.queue");
    dbService = BkmDatabaseService.createProxy(vertx, bkmDbQueue);

    // tag::https-server[]
    /*
    HttpServer server = vertx.createHttpServer(new HttpServerOptions()
      .setSsl(true)
      .setKeyCertOptions(new JksOptions()
        .setPath("server-keystore.jks")
        .setPassword("secret")));
        */
    HttpServer server = vertx.createHttpServer(new HttpServerOptions());
    // end::https-server[]

    // tag::shiro-auth[]
    AuthProvider auth = ShiroAuth.create(vertx, new ShiroAuthOptions()
    .setType(ShiroAuthRealmType.PROPERTIES)
      .setConfig(new JsonObject()
      .put("properties_path", "classpath:bkm-users.properties")));
    // end::shiro-auth[]

    // tag::shiro-routes[]
    Router router = Router.router(vertx);

    router.route().handler(CookieHandler.create());
    router.route().handler(BodyHandler.create());
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    router.route().handler(UserSessionHandler.create(auth));

    //AuthHandler authHandler = RedirectAuthHandler.create(auth,"/login");
    //router.route("/").handler(authHandler);

    router.route("/").handler(this::indexHandler);
    // end::shiro-routes[]

    // tag::shiro-login[]
    /*
    router.get("/login").handler(this::loginHandler);
    router.post("/login-auth").handler(FormLoginHandler.create(auth));  // <1>

    router.get("/logout").handler(context -> {
      context.clearUser();  // <2>
      context.response()
        .setStatusCode(302)
        .putHeader("Location", "/")
        .end();
    });
    */
    // end::shiro-login[]


    // tag::jwtAuth[]
    Router apiRouter = Router.router(vertx);

    JWTAuth jwtAuth = JWTAuth.create(vertx, new JsonObject()
      .put("keyStore", new JsonObject()
        .put("path", "keystore.jceks")
        .put("type", "jceks")
        .put("password", "secret")));

    apiRouter.route().handler(JWTAuthHandler.create(jwtAuth, "/api/token"));
    // end::jwtAuth[]

    // tag::issue-jwt[]
    apiRouter.get("/token").handler(context -> {

      JsonObject creds = new JsonObject()
        .put("username", context.request().getHeader("login"))
        .put("password", context.request().getHeader("password"));
      auth.authenticate(creds, authResult -> {  // <1>

        if (authResult.succeeded()) {
          User user = authResult.result();
          user.isAuthorised("create", canCreate -> {  // <2>
            user.isAuthorised("delete", canDelete -> {
              user.isAuthorised("update", canUpdate -> {

                String token = jwtAuth.generateToken( // <3>
                  new JsonObject()
                    .put("username", context.request().getHeader("login"))
                    .put("canCreate", canCreate.succeeded() && canCreate.result())
                    .put("canDelete", canDelete.succeeded() && canDelete.result())
                    .put("canUpdate", canUpdate.succeeded() && canUpdate.result()),
                  new JWTOptions()
                    .setSubject("Veebk API")
                    .setIssuer("Vert.x"));
                context.response().putHeader("Content-Type", "text/plain").end(token);
              });
            });
          });
        } else {
          context.fail(401);
        }
      });
    });
    // end::issue-jwt[]

    apiRouter.get("/bookmarks").handler(this::apiRoot);

    apiRouter.get("/bookmarks/:id").handler(this::apiGetBookmark);
    apiRouter.post().handler(BodyHandler.create());
    apiRouter.post("/bookmarks").handler(this::apiCreateBookmark);
    apiRouter.put().handler(BodyHandler.create());
    apiRouter.put("/bookmarks/:id").handler(this::apiUpdateBookmark);
    apiRouter.delete("/bookmarks/:id").handler(this::apiDeleteBookmark);

    router.mountSubRouter("/api", apiRouter);


    int portNumber = config().getInteger(CONFIG_HTTP_SERVER_PORT, 9090);
    server
      .requestHandler(router::accept)
      .listen(portNumber, ar -> {
        if (ar.succeeded()) {
          LOGGER.info("HTTP server running on port " + portNumber);
          startFuture.complete();
        } else {
          LOGGER.error("Could not start a HTTP server", ar.cause());
          startFuture.fail(ar.cause());
        }
      });

  }

  // tag::indexHandler[]
  private void indexHandler(RoutingContext context) {
    context.response().putHeader("Content-Type", "text/html");
    context.response().end("test");
  }
  // end::indexHandler[]

  // tag::loginHandler[]
  private void loginHandler(RoutingContext context) {
    context.put("title", "Login");
    /*
    templateEngine.render(context, "templates/login.ftl", ar -> {
      if (ar.succeeded()) {
        context.response().putHeader("Content-Type", "text/html");
        context.response().end(ar.result());
      } else {
        context.fail(ar.cause());
      }
    });
    */
    context.response().putHeader("Content-Type", "text/html");
    context.response().end("test");
  }
  // end::loginHandler[]

  private void apiRoot(RoutingContext context) {
    dbService.fetchAllBookmarksData(reply -> {
      JsonObject response = new JsonObject();
      if (reply.succeeded()) {
        List<JsonObject> bookmarks = reply.result()
          .stream()
          .map(obj -> new JsonObject()
            .put("id", obj.getInteger("ID"))
            .put("title", obj.getString("TITLE"))
            .put("url", obj.getString("URL")))
          .collect(Collectors.toList());
        response
          .put("success", true)
          .put("bookmarks", bookmarks);
        context.response().setStatusCode(200);
        context.response().putHeader("Content-Type", "application/json");
        context.response().end(response.encode());

      }else {
        response
          .put("success", false)
          .put("error", reply.cause().getMessage());
        context.response().setStatusCode(500);
        context.response().putHeader("Content-Type", "application/json");
        context.response().end(response.encode());
      }
    });
  }

  private void apiCreateBookmark(RoutingContext context) {
    if (context.user().principal().getBoolean("canCreate", false)) {
      JsonObject bookmark = context.getBodyAsJson();
      if (!validateJsonBookmarkDocument(context, bookmark, "title", "url")) {
        return;
      }
      dbService.createBookmark(bookmark.getString("title"), bookmark.getString("url"), reply -> {
        if (reply.succeeded()) {
          context.response().setStatusCode(201);
          context.response().putHeader("Content-Type", "application/json");
          context.response().end(new JsonObject().put("success", true).encode());
        } else {
          context.response().setStatusCode(500);
          context.response().putHeader("Content-Type", "application/json");
          context.response().end(new JsonObject()
            .put("success", false)
            .put("error", reply.cause().getMessage()).encode());
        }
      });
    } else {
      context.fail(401);
    }
  }

  private void apiGetBookmark(RoutingContext context) {
    int id = Integer.valueOf(context.request().getParam("id"));
    dbService.fetchBookmarkById(id, reply -> {
      JsonObject response = new JsonObject();
      if (reply.succeeded()) {
        JsonObject dbObject = reply.result();
        if (dbObject.getBoolean("found")) {
          JsonObject payload = new JsonObject()
            .put("title", dbObject.getString("title"))
            .put("id", dbObject.getInteger("id"))
            .put("url", dbObject.getString("url"));
          response
            .put("success", true)
            .put("bookmark", payload);
          context.response().setStatusCode(200);
        } else {
          context.response().setStatusCode(404);
          response
            .put("success", false)
            .put("error", "There is no bookmark with ID " + id);
        }
      } else {
        response
          .put("success", false)
          .put("error", reply.cause().getMessage());
        context.response().setStatusCode(500);
      }
      context.response().putHeader("Content-Type", "application/json");
      context.response().end(response.encode());
    });
  }

  private void handleSimpleDbReply(RoutingContext context, AsyncResult<Void> reply) {
    if (reply.succeeded()) {
      context.response().setStatusCode(200);
      context.response().putHeader("Content-Type", "application/json");
      context.response().end(new JsonObject().put("success", true).encode());
    } else {
      context.response().setStatusCode(500);
      context.response().putHeader("Content-Type", "application/json");
      context.response().end(new JsonObject()
        .put("success", false)
        .put("error", reply.cause().getMessage()).encode());
    }
  }

  private void apiUpdateBookmark(RoutingContext context) {
    if (context.user().principal().getBoolean("canUpdate", false)) {
      int id = Integer.valueOf(context.request().getParam("id"));
      JsonObject page = context.getBodyAsJson();
      if (!validateJsonBookmarkDocument(context, page, "url")) {
        return;
      }
      dbService.saveBookmark(id, page.getString("url"), reply -> {
        handleSimpleDbReply(context, reply);
      });
    } else {
      context.fail(401);
    }
  }

  private boolean validateJsonBookmarkDocument(RoutingContext context, JsonObject page, String... expectedKeys) {
    if (!Arrays.stream(expectedKeys).allMatch(page::containsKey)) {
      LOGGER.error("Bad page creation JSON payload: " + page.encodePrettily() + " from " + context.request().remoteAddress());
      context.response().setStatusCode(400);
      context.response().putHeader("Content-Type", "application/json");
      context.response().end(new JsonObject()
        .put("success", false)
        .put("error", "Bad request payload").encode());
      return false;
    }
    return true;
  }


  // tag::apiDeleteBookmark[]
  private void apiDeleteBookmark(RoutingContext context) {
    if (context.user().principal().getBoolean("canDelete", false)) {
      int id = Integer.valueOf(context.request().getParam("id"));
      dbService.deleteBookmark(id, reply -> {
        handleSimpleDbReply(context, reply);
      });
    } else {
      context.fail(401);
    }
  }
  // end::apiDeleteBookmark[]


}
