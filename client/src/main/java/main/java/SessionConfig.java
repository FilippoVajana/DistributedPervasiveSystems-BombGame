package main.java;

import com.fv.sdp.model.Match;

/**
 * Created by filip on 6/12/2017.
 */
public class SessionConfiguration
{
    private static SessionConfiguration instance = null;
    public static SessionConfiguration getInstance()
    {
        if (instance == null)
            instance = new SessionConfiguration();
        return instance;
    }

    private SessionConfiguration()
    {

    }

    //todo REST params
    public String REST_BASE_URL;

    //todo Socket params
    public String LISTENER_ADDR;
    public int LISTENER_PORT;

    //todo player params
    public String PLAYER_NICKNAME;
    public Match PLAYER_MATCH;
}
