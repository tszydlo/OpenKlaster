package com.openklaster.mongo.service;

import com.openklaster.common.messages.BusMessageReplyUtils;
import com.openklaster.common.model.Installation;
import com.openklaster.mongo.parser.EntityParser;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.LoggerFactory;

public class InstallationHandler extends EntityHandler {

    static final String installationCounter = "installationId";
    private static final String counterValueKey = "seq";

    public InstallationHandler(EntityParser<Installation> parser,
                               MongoPersistenceService service,
                               String collectionName) {
        super(parser, service, collectionName);
        logger = LoggerFactory.getLogger(InstallationHandler.class);
    }

    //TODO it should have proper refactor
    @Override
    public void add(Message<JsonObject> busMessage) {
        JsonObject jsonObject = busMessage.body();
        if (!jsonObject.containsKey(ID_KEY) || jsonObject.getValue(ID_KEY) == null) {
            persistenceService.getCounter(installationCounter, handler -> {
                if (handler.succeeded()) {
                    if (handler.result() != null && handler.result().getInteger(counterValueKey) != null) {
                        int seq = handler.result().getInteger(counterValueKey);
                        updateCounter(busMessage, seq);
                    } else {
                        updateCounter(busMessage, 0);
                    }
                } else {
                    BusMessageReplyUtils.replyWithError(busMessage, HttpResponseStatus.BAD_REQUEST, "Problem with installation counter.");
                }
            });
        } else {
            super.add(busMessage);
        }
    }

    private void updateCounter(Message<JsonObject> busMessage, int counterValue) {
        String id = "installation:" + counterValue;
        busMessage.body().put(ID_KEY, id);
        persistenceService.updateCounter(installationCounter, counterValue + 1, handler -> {
            if (handler.succeeded()) {
                super.add(busMessage);
            } else {
                BusMessageReplyUtils.replyWithError(busMessage, HttpResponseStatus.INTERNAL_SERVER_ERROR, "Problem with installation counter");
            }
        });
    }
}
