package com.fv.sdp.rest;

import com.fv.sdp.ApplicationContext;
import com.fv.sdp.model.Match;
import com.fv.sdp.model.Player;
import com.fv.sdp.util.PrettyPrinter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.validation.constraints.NotNull;
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
    //app context
    private ApplicationContext appContext;

    private Client restClient = null;
    private WebTarget restBaseUrl = null; //TODO: remove, use ApplicationContext
    private Map<String, String> restEndpointsIndex = null; //TODO: remove

    public RESTConnector(@NotNull ApplicationContext appContext)
    {
        //log
        PrettyPrinter.printClassInit(this);

        //save app context
        this.appContext = appContext;

        //client
        restClient = ClientBuilder.newClient();

        //base url
        restBaseUrl = restClient.target(this.appContext.REST_BASE_URL);

        //endpoints
        restEndpointsIndex = this.appContext.REST_ENDPOINTS;
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
            //appContext.PLAYER_MATCH = joinedMatch; //TODO: rivedere pesantemente andando a demandare le azioni a GameManager

            //set ring node
            //appContext.RING_NETWORK = joinedMatch.getPlayers(); //TODO: remove current player

            //notify ring
            appContext.GAME_MANAGER.joinMatchGrid(player, joinedMatch);

            return true;
        }
        else
        {
            //log
            PrettyPrinter.printTimestampLog(String.format("[%s] ERROR joining match %s: %s", this.getClass().getSimpleName(), match.getId(), response.getStatusInfo().getReasonPhrase()));
            //TODO: fault strategy
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
            //log
            PrettyPrinter.printTimestampLog(String.format("[%s] ERROR creating match %s: %s", this.getClass().getSimpleName(), match.getId(), response.getStatusInfo().getReasonPhrase()));
            //TODO: fault strategy
            return false;
        }

    }

    public boolean leaveServerMatch(Match match, Player player)
    {
        //set web target
        WebTarget matchTarget = restBaseUrl.path(restEndpointsIndex.get("Match"));
        WebTarget leaveTarget = matchTarget.path(String.format("%s/leave", match.getId()));

        //invocation
        Invocation.Builder invocation = leaveTarget.request();

        //make request
        Response response = invocation.post(Entity.entity(player, MediaType.APPLICATION_JSON));

        //read response
        if (response.getStatus() == 200)
        {
            //notify ring
            appContext.GAME_MANAGER.notifyLeave(player); //TODO: rivedere pesantemente andando a demandare le azioni a GameManager

            //clear match
            appContext.PLAYER_MATCH = null;

            //clear ring nodes
            appContext.RING_NETWORK = null;

            return true;
        }
        else
        {
            //log
            PrettyPrinter.printTimestampLog(String.format("[%s] ERROR leaving match %s: %s", this.getClass().getSimpleName(), match.getId(), response.getStatusInfo().getReasonPhrase()));
            //TODO: fault strategy
            return false;
        }
    }
}

