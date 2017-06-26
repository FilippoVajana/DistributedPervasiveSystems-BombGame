package com.fv.sdp.rest;

import com.fv.sdp.ApplicationContext;
import com.fv.sdp.model.Match;
import com.fv.sdp.model.Player;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by filip on 5/27/2017.
 */
public class RESTConnector
{
    private Client restClient = null;
    private WebTarget restBaseUrl = null; //TODO: remove, use ApplicationContext
    private Map<String, String> restEndpointsIndex = null; //TODO: remove

    public RESTConnector()
    {
        //config
        ApplicationContext configuration = ApplicationContext.getInstance();
        //client
        restClient = ClientBuilder.newClient();
        //base url
        restBaseUrl = restClient.target(configuration.REST_BASE_URL);
        //endpoints
        restEndpointsIndex = configuration.REST_ENDPOINTS;
    }

    public boolean requestSetTestModel()
    {
        //set web target
        WebTarget matchTarget = restBaseUrl.path(restEndpointsIndex.get("Match"));
        WebTarget testModelTarget = matchTarget.path("setTest");

        //invocation
        Invocation.Builder invocation = testModelTarget.request();

        //make request
        Response response = invocation.get();
        return true;
    }

    public ArrayList<Match> getServerMatchList()
    {
        //set web target
        WebTarget matchTarget = restBaseUrl.path(restEndpointsIndex.get("Match"));

        //invocation
        Invocation.Builder invocation = matchTarget.request();

        //make request
        Response response = invocation.get();

        //read response payload
        String entity = response.readEntity(String.class);
        System.out.println(entity);

        //unpack json data
        Type listType = new TypeToken<ArrayList<Match>>(){}.getType(); //sembra aver risolto il problema del riconoscimento tipi generics
        Gson jsonizer = new Gson();
        ArrayList<Match> matches = jsonizer.fromJson(entity, listType);

        return matches;
    }

    //TODO: test
    public boolean joinServerMatch(Match match, Player player)
    {
        //set web target
        WebTarget matchTarget = restBaseUrl.path(restEndpointsIndex.get("Match"));
        WebTarget joinTarget = matchTarget.path(String.format("%s/join", match.getId()));

        //invocation
        Invocation.Builder invocation = joinTarget.request();

        //make request
        Response response = invocation.post(Entity.entity(player, MediaType.APPLICATION_JSON));

        //read response
        if (response.getStatus() == 200)
        {
            Match joinedMatch = response.readEntity(Match.class);
            //set match
            ApplicationContext.getInstance().PLAYER_MATCH = joinedMatch;
            //set ring node
            ApplicationContext.getInstance().RING_NETWORK = joinedMatch.getPlayers();

            return true;
        }
        else
        {
            System.out.println(response.getStatusInfo().getReasonPhrase());
            return false;
        }
    }

    public boolean createServerMatch(Match match)
    {
        //set web target
        WebTarget matchTarget = restBaseUrl.path(restEndpointsIndex.get("Match"));

        //invocation
        Invocation.Builder invocation = matchTarget.request();

        //make request
        //String matchJson = new Gson().toJson(match, new TypeToken<Match>(){}.getType());
        Response response = invocation.post(Entity.entity(match, MediaType.APPLICATION_JSON)); //todo change to String + Gson

        //read response
        if (response.getStatus() == 201)
            return true;
        else
        {
            System.out.println(response.getStatusInfo().getReasonPhrase());
            return false;
        }

    }
}

