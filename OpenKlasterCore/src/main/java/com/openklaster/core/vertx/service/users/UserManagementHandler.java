package com.openklaster.core.vertx.service.users;

import com.openklaster.common.authentication.tokens.TokenHandler;
import com.openklaster.common.model.User;
import com.openklaster.core.vertx.authentication.AuthenticationClient;
import com.openklaster.core.vertx.messages.repository.CrudRepository;
import com.openklaster.core.vertx.service.EndpointService;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.openklaster.common.messages.BusMessageReplyUtils.METHOD_KEY;

public class UserManagementHandler extends EndpointService {

    private AuthenticatedUserManager authUserManager;
    //Note that new User Manager should be added to this map in order to work
    private Map<String, UserManager> userManagerMap;
    private final AuthenticationClient authenticationClient;
    private final TokenHandler tokenHandler;
    private final CrudRepository<User> userCrudRepository;

    public UserManagementHandler(AuthenticationClient authenticationClient,
                                 TokenHandler tokenHandler, CrudRepository<User> userCrudRepository, String busAddress) {
        super(busAddress);
        logger = LoggerFactory.getLogger(UserManagementHandler.class);

        this.authenticationClient = authenticationClient;
        this.tokenHandler = tokenHandler;
        this.userCrudRepository = userCrudRepository;
        prepareManagers();
    }

    private void prepareManagers() {
        this.userManagerMap = new HashMap<>();
        this.authUserManager = new AuthenticatedUserManager(authenticationClient, userCrudRepository);

        RegisterManager registerManager = new RegisterManager(authenticationClient, userCrudRepository);
        userManagerMap.put(registerManager.getMethodName(), registerManager);

        LoginManager loginManager = new LoginManager(authenticationClient, userCrudRepository);
        userManagerMap.put(loginManager.getMethodName(), loginManager);

        UpdateUserManager updateUserManager = new UpdateUserManager(authenticationClient, userCrudRepository);
        userManagerMap.put(updateUserManager.getMethodName(), updateUserManager);

        InformationManager informationManager = new InformationManager();
        authUserManager.addMethodHelper(informationManager.getMethodName(), informationManager);

        GenerateTokenManager generateTokenManager = new GenerateTokenManager(tokenHandler, userCrudRepository);
        authUserManager.addMethodHelper(generateTokenManager.getMethodName(), generateTokenManager);

        DeleteTokenManager deleteTokenManager = new DeleteTokenManager(userCrudRepository);
        authUserManager.addMethodHelper(deleteTokenManager.getMethodName(), deleteTokenManager);

        DeleteAllTokensManager deleteAllTokensManager = new DeleteAllTokensManager(userCrudRepository);
        authUserManager.addMethodHelper(deleteAllTokensManager.getMethodName(), deleteAllTokensManager);
    }

    @Override
    public void configureEndpoints(EventBus eventBus) {
        MessageConsumer<JsonObject> consumer = eventBus.consumer(getEventBusAddress());
        consumer.handler(this::handlerMap);
    }

    private void handlerMap(Message<JsonObject> message) {
        String methodName = message.headers().get(METHOD_KEY);
        UserManager manager = userManagerMap.get(methodName);

        if (manager == null) {
            if(!authUserManager.hasMessageHandler(methodName)){
                handleUnknownMethod(message, methodName);
            } else{
                authUserManager.handleMessage(message, methodName);
            }
        } else {
            manager.handleMessage(message);
        }
    }
}
