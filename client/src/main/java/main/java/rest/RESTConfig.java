package main.java.rest;

import java.util.HashMap;

/**
 * Created by filip on 28/05/2017.
 */
public class RESTConfig
{
    //base url
    public final String BASE_URL = "http://localhost:8080/server_war_exploded";

    //service endpoints
    public HashMap<String, String> SERVICE_ENDPOINTS = new HashMap<>();

    public RESTConfig()
    {
        //endpoints
        SERVICE_ENDPOINTS.put("Match", "match");
    }

}
