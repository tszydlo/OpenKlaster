package com.openklaster.api.app;


import com.openklaster.api.handler.*;
import com.openklaster.api.model.*;
import com.openklaster.api.parser.DefaultParseStrategy;
import com.openklaster.api.properties.EndpointRouteProperties;
import com.openklaster.api.properties.EventBusAddressProperties;
import com.openklaster.common.config.NestedConfigAccessor;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import java.util.Arrays;
import java.util.List;

public class OpenKlasterAPIVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(OpenKlasterAPIVerticle.class);
    private static final int VERSION1 = 1;
    private ConfigRetriever configRetriever;
    private NestedConfigAccessor configAccessor;
    private Vertx vertx;
    private EventBus eventBus;
    private List<Handler> handlers;


    public OpenKlasterAPIVerticle(Vertx vertx, ConfigRetriever configRetriever) {
        this.vertx = vertx;
        this.configRetriever = configRetriever;
        this.eventBus = vertx.eventBus();
    }

    @Override
    public void start(Promise<Void> promise) {
        Promise<Void> deployPrepared = Promise.promise();
        prepareDeploy(deployPrepared);
        deployPrepared.future().onComplete(result -> {
            prepareConfig();
            promise.complete();
        });
    }

    private void prepareDeploy(Promise<Void> deployPrepared) {
        Promise<Void> promise = Promise.promise();
        //createClusteredVertx(promise);
        deployPrepared.complete();
    }

    private void prepareConfig() {
        configRetriever.getConfig(config -> {
            if (config.succeeded()) {
                this.configAccessor = new NestedConfigAccessor(config.result());
                startVerticle();
            } else {
                logger.error(config.cause());
                vertx.close();
            }
        });
    }

    private void startVerticle() {
        Router router = Router.router(vertx);
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(configAccessor.getInteger(EndpointRouteProperties.listeningPortKey));
        handlers = Arrays.asList(
                new PostHandler(buildEndpoint(configAccessor, VERSION1, EndpointRouteProperties.loginEndpoint),
                        configAccessor.getString(EventBusAddressProperties.userCoreAddressKey), "login",
                        eventBus, configAccessor, new DefaultParseStrategy<Login>(Login.class)),

                new PostHandler(buildEndpoint(configAccessor, VERSION1, EndpointRouteProperties.userEndpoint),
                        configAccessor.getString(EventBusAddressProperties.userCoreAddressKey), "register",
                        eventBus, configAccessor, new DefaultParseStrategy<Register>(Register.class)),

                new PutHandler(buildEndpoint(configAccessor, VERSION1, EndpointRouteProperties.userEndpoint),
                        configAccessor.getString(EventBusAddressProperties.userCoreAddressKey), "updateUser",
                        eventBus, configAccessor, new DefaultParseStrategy<UpdateUser>(UpdateUser.class)),

                new GetHandler(buildEndpoint(configAccessor, VERSION1, EndpointRouteProperties.userEndpoint),
                        configAccessor.getString(EventBusAddressProperties.userCoreAddressKey), "info",
                        eventBus, configAccessor, new DefaultParseStrategy<Username>(Username.class)),

                new PostHandler(buildEndpoint(configAccessor, VERSION1, EndpointRouteProperties.tokenEndpoint),
                        configAccessor.getString(EventBusAddressProperties.userCoreAddressKey), "generateToken",
                        eventBus, configAccessor, new DefaultParseStrategy<Username>(Username.class)),

                new DeleteHandler(buildEndpoint(configAccessor, VERSION1, EndpointRouteProperties.tokenEndpoint),
                        configAccessor.getString(EventBusAddressProperties.userCoreAddressKey), "deleteToken",
                        eventBus, configAccessor, new DefaultParseStrategy<Model>(Model.class)),

                new DeleteHandler(buildEndpoint(configAccessor, VERSION1, EndpointRouteProperties.tokenEndpoint) + "/all",
                        configAccessor.getString(EventBusAddressProperties.userCoreAddressKey), "deleteAllTokens",
                        eventBus, configAccessor, new DefaultParseStrategy<Model>(Model.class)),

                new GetHandler(buildEndpoint(configAccessor, VERSION1, EndpointRouteProperties.installationEndpoint),
                        configAccessor.getString(EventBusAddressProperties.installationCoreAddressKey),
                        eventBus, configAccessor, new DefaultParseStrategy<InstallationRequest>(InstallationRequest.class)),

                new PostHandler(buildEndpoint(configAccessor, VERSION1, EndpointRouteProperties.installationEndpoint),
                        configAccessor.getString(EventBusAddressProperties.installationCoreAddressKey),
                        eventBus, configAccessor, new DefaultParseStrategy<PostInstallation>(PostInstallation.class)),

                new PutHandler(buildEndpoint(configAccessor, VERSION1, EndpointRouteProperties.installationEndpoint),
                        configAccessor.getString(EventBusAddressProperties.installationCoreAddressKey),
                        eventBus, configAccessor, new DefaultParseStrategy<Installation>(Installation.class)),

                new DeleteHandler(buildEndpoint(configAccessor, VERSION1, EndpointRouteProperties.installationEndpoint),
                        configAccessor.getString(EventBusAddressProperties.installationCoreAddressKey),
                        eventBus, configAccessor, new DefaultParseStrategy<InstallationRequest>(InstallationRequest.class)),

                new GetHandler(buildEndpoint(configAccessor, VERSION1, EndpointRouteProperties.powerconsumptionEndpoint),
                        configAccessor.getString(EventBusAddressProperties.powerconsumptionCoreAddressKey),
                        eventBus, configAccessor, new DefaultParseStrategy<MeasurementRequest>(MeasurementRequest.class)),

                new PostHandler(buildEndpoint(configAccessor, VERSION1, EndpointRouteProperties.powerconsumptionEndpoint),
                        configAccessor.getString(EventBusAddressProperties.powerconsumptionCoreAddressKey),
                        eventBus, configAccessor, new DefaultParseStrategy<MeasurementPower>(MeasurementPower.class)),

                new GetHandler(buildEndpoint(configAccessor, VERSION1, EndpointRouteProperties.powerproductionEndpoint),
                        configAccessor.getString(EventBusAddressProperties.powerproductionCoreAddressKey),
                        eventBus, configAccessor, new DefaultParseStrategy<MeasurementRequest>(MeasurementRequest.class)),

                new PostHandler(buildEndpoint(configAccessor, VERSION1, EndpointRouteProperties.powerproductionEndpoint),
                        configAccessor.getString(EventBusAddressProperties.powerproductionCoreAddressKey),
                        eventBus, configAccessor, new DefaultParseStrategy<MeasurementPower>(MeasurementPower.class)),

                new GetHandler(buildEndpoint(configAccessor, VERSION1, EndpointRouteProperties.energyconsumedEndpoint),
                        configAccessor.getString(EventBusAddressProperties.energyconsumedCoreAddressKey),
                        eventBus, configAccessor, new DefaultParseStrategy<MeasurementRequest>(MeasurementRequest.class)),

                new PostHandler(buildEndpoint(configAccessor, VERSION1, EndpointRouteProperties.energyconsumedEndpoint),
                        configAccessor.getString(EventBusAddressProperties.energyconsumedCoreAddressKey),
                        eventBus, configAccessor, new DefaultParseStrategy<MeasurementEnergy>(MeasurementEnergy.class)),

                new GetHandler(buildEndpoint(configAccessor, VERSION1, EndpointRouteProperties.energyproducedEndpoint),
                        configAccessor.getString(EventBusAddressProperties.energyproducedCoreAddressKey),
                        eventBus, configAccessor, new DefaultParseStrategy<MeasurementRequest>(MeasurementRequest.class)),

                new PostHandler(buildEndpoint(configAccessor, VERSION1, EndpointRouteProperties.energyproducedEndpoint),
                        configAccessor.getString(EventBusAddressProperties.energyproducedCoreAddressKey),
                        eventBus, configAccessor, new DefaultParseStrategy<MeasurementEnergy>(MeasurementEnergy.class))
        );


        routerConfig(router);
    }

    private void routerConfig(Router router) {
        handlers.forEach(handler -> {
            configureRouteHandler(router);
            switch (handler.getMethod()) {
                case "get":
                    router.get(handler.getRoute()).handler(handler::handle);
                    break;
                case "post":
                    router.post(handler.getRoute()).consumes("application/json").handler(handler::handle);
                    break;
                case "put":
                    router.put(handler.getRoute()).consumes("application/json").handler(handler::handle);
                    break;
                case "delete":
                    router.delete(handler.getRoute()).handler(handler::handle);
                    break;
            }
        });
    }

    private void configureRouteHandler(Router router) {
        router.route().handler(BodyHandler.create())
                .handler(CorsHandler.create("*")
                        .allowedHeader("Content-Type")
                        .allowedHeader("responseType"));
    }

    public static String buildEndpoint(NestedConfigAccessor configAccessor, int version, String route) {
        return configAccessor.getString(EndpointRouteProperties.prefix) +
                "/" + version + configAccessor.getString(route);
    }
}
