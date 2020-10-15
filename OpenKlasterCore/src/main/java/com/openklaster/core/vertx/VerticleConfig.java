package com.openklaster.core.vertx;

import io.vertx.core.json.JsonObject;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.FileReader;
import java.io.IOException;


@Configuration
@ComponentScan
public class VerticleConfig {
    private JsonObject jsonObject;

    public VerticleConfig() {
        JSONParser parser = new JSONParser();
        try {
            Object object = parser.parse(new FileReader("OpenKlasterCore\\src\\main\\resources\\config-dev.json"));
            JSONObject jsonSimple = (JSONObject) object;
            //noinspection unchecked
            this.jsonObject = new JsonObject(jsonSimple);
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

}
