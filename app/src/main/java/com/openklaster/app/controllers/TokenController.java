package com.openklaster.app.controllers;

import com.openklaster.app.model.requests.ApiTokenRequest;
import com.openklaster.app.model.responses.TokenResponse;
import com.openklaster.app.services.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("token")
public class TokenController {

    @Autowired
    UsersService usersService;

    @PostMapping()
    public TokenResponse generateApiToken(@RequestBody ApiTokenRequest tokenRequest) {
        return usersService.generateApiTokenForUser(tokenRequest);
    }

    @DeleteMapping()
    public void removeApiToken(@RequestParam String apiToken, @RequestParam String username) {
        usersService.removeToken(apiToken, username);
    }

    @DeleteMapping()
    public void removeAllTokens(@RequestParam String username) {
        usersService.removeAllTokens(username);
    }
}
