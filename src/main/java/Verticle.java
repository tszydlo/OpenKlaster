import config.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import parser.*;
import service.MongoPersistenceService;

import java.util.Arrays;
import java.util.List;

import static com.sun.org.apache.xalan.internal.xsltc.compiler.util.Util.println;

public class Verticle extends AbstractVerticle {

    private final DbConfig dbConfig;
    private final MongoClient client;
    private final MongoPersistenceService persistenceService;
    private final List<EntityConfig> entityConfigs;

    public Verticle(Vertx vertx) {
        this.vertx = vertx;
        this.dbConfig = new DbConfig();
        this.client = MongoClient.createShared(vertx, dbConfig.getMongoConfig());
        this.persistenceService = new MongoPersistenceService(client);
        this.entityConfigs = Arrays.asList(
                new CalculatorConfig(persistenceService, new EnergySourceCalculatorParser()),
                new InstallationConfig(persistenceService, new InstallationParser()),
                new InverterConfig(persistenceService, new InverterParser()),
                new LoadConfig(persistenceService, new LoadParser()),
                new SourceConfig(persistenceService, new SourceParser()),
                new UserConfig(persistenceService, new UserParser())
        );

    }

    @Override
    public void start(Promise<Void> promise) {

        Router router = Router.router(vertx);
        router.route("/").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response
                    .end("Ended...");
        });

        HttpServer server = vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080);
        routerConfig(router);

    }

    private void routerConfig(Router router) {
        entityConfigs.forEach(config -> {
            router.route(config.getRoute()).handler(BodyHandler.create());
            router.post(config.getRoute()).consumes("application/json").handler(config.getHandler()::add);
            router.get(config.getRoute()).consumes("application/json").handler(config.getHandler()::findById);
            router.delete(config.getRoute()).consumes("application/json").handler(config.getHandler()::delete);
        });
    }


}
