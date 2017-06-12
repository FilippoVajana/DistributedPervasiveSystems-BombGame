package main.java;

import com.fv.sdp.model.Match;

import java.util.HashMap;

/**
 * Created by filip on 6/12/2017.
 */
public class SessionConfig
{
    private static SessionConfig instance = null;
    public static SessionConfig getInstance()
    {
        if (instance == null)
            instance = new SessionConfig();
        return instance;
    }

    private SessionConfig()
    {
        //init REST
        RESTConfig();
    }


    //REST
    public String REST_BASE_URL;
    public HashMap<String, String> REST_ENDPOINTS = new HashMap<>();

    private void RESTConfig()
    {
        //endpoints
        REST_ENDPOINTS.put("Match", "match");
    }

    //SOCKET
    public String LISTENER_ADDR;
    public int LISTENER_PORT;

    //PLAYER
    //todo player params
    public String PLAYER_NICKNAME;
    public Match PLAYER_MATCH;
}
