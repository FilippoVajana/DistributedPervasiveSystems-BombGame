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
    private WebTarget restBaseUrl = null;
    private Map<String, String> restEndpointsIndex = null;

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

    public ArrayList<Match> getServerMatchList()
    {
        //set web target
        WebTarget matchTarget = restBaseUrl.path(restEndpointsIndex.get("Match"));

        try
        {
            //invocation
            Invocation.Builder invocation = matchTarget.request();

            //make request
            Response response = invocation.get();

            //read response payload
            String entity = response.readEntity(String.class);

            //unpack json data
            Type listType = new TypeToken<ArrayList<Match>>(){}.getType(); //sembra aver risolto il problema del riconoscimento tipi generics
            ArrayList<Match> matches = new Gson().fromJson(entity, listType);

            return matches;
        }catch (Exception ex)
        {
            //log
            PrettyPrinter.printTimestampError(String.format("[%s] Error getting server list", appContext.getPlayerInfo().getId()));
            return new ArrayList<>();
        }
    }

    public boolean joinServerMatch(Match match, Player player)
    {
        //set web target
        WebTarget matchTarget = restBaseUrl.path(restEndpointsIndex.get("Match"));
        WebTarget joinTarget = matchTarget.path(String.format("%s/join", match.getId()));

        try
        {
            //invocation
            Invocation.Builder invocation = joinTarget.request();

            //make request
            String playerJson = new Gson().toJson(player, Player.class);
            Response response = invocation.post(Entity.entity(playerJson, MediaType.APPLICATION_JSON));

            //read response
            if (response.getStatus() == 200)
            {
                //read joined match
                String joinedMatchJson = response.readEntity(String.class);
                Match joinedMatch = new Gson().fromJson(joinedMatchJson, Match.class);

                //notify ring
                appContext.GAME_MANAGER.joinMatchGrid(player, joinedMatch);

                return true;
            }
            else
                throw new Exception(response.getStatusInfo().getReasonPhrase());
        }catch (Exception ex)
        {
            //log
            PrettyPrinter.printTimestampError(String.format("[%s] Error joining match %s: %s", appContext.getPlayerInfo().getId(), match.getId(), ex.getMessage()));
            return false;
        }
    }

    public boolean createServerMatch(Match match)
    {
        //set web target
        WebTarget matchTarget = restBaseUrl.path(restEndpointsIndex.get("Match"));

        try
        {
            //invocation
            Invocation.Builder invocation = matchTarget.request();

            //make request
            Response response = invocation.post(Entity.entity(match, MediaType.APPLICATION_JSON));

            //read response
            if (response.getStatus() == 201)
            {
                //log
                PrettyPrinter.printTimestampLog(String.format("[%s] Created REST match", appContext.getPlayerInfo().getId(), match.getId()));
                return true;
            }
            else
                throw new Exception(response.getStatusInfo().getReasonPhrase());
        }catch (Exception ex)
        {
            //log
            PrettyPrinter.printTimestampError(String.format("[%s] Error creating match %s: %s", appContext.getPlayerInfo().getId(), match.getId(), ex.getMessage()));
            return false;
        }
    }

    public boolean leaveServerMatch(Match match, Player player)
    {
        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Leaving REST match", appContext.getPlayerInfo().getId()));

       try
       {
           //set web target
           WebTarget matchTarget = restBaseUrl.path(restEndpointsIndex.get("Match"));
           WebTarget leaveTarget = matchTarget.path(String.format("%s/leave", match.getId()));

           //invocation
           Invocation.Builder invocation = leaveTarget.request();

           //make request
           String playerJson = new Gson().toJson(player, Player.class);
           Response response = invocation.post(Entity.entity(playerJson, MediaType.APPLICATION_JSON));

           //read response
           if (response.getStatus() == 200)
           {
               //log
               PrettyPrinter.printTimestampLog(String.format("[%s] Left REST match", appContext.getPlayerInfo().getId()));

               return true;
           }
           else
               return false;
       }catch (Exception ex)
       {
           //log
           PrettyPrinter.printTimestampError(String.format("[%s] Error leaving match %s: %s", appContext.getPlayerInfo().getId(), match.getId(), ex.getMessage()));
           //TODO: fault strategy
           return false;
       }
    }

    public boolean deleteMatch(Match match)
    {
        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Deleting REST match: %s", appContext.getPlayerInfo().getId(), match.getId()));

        try
        {
            //set web target
            WebTarget matchTarget = restBaseUrl.path(restEndpointsIndex.get("Match"));
            WebTarget deleteTarget = matchTarget.path(String.format("%s/delete", match.getId()));

            //invocation
            Invocation.Builder invocation = deleteTarget.request();

            //make request
            Response response = invocation.delete();

            //read response
            if (response.getStatus() == 200)
            {
                //log
                PrettyPrinter.printTimestampLog(String.format("[%s] REST match %s deleted", appContext.getPlayerInfo().getId(), match.getId()));

                return true;
            }
            else
                return false;
        }catch (Exception ex)
        {
            //log
            PrettyPrinter.printTimestampError(String.format("[%s] ERROR deleting match %s: %s", appContext.getPlayerInfo().getId(), match.getId(), ex.getMessage()));
            //TODO: fault strategy
            return false;
        }
    }

    public void resetServer()
    {
        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Resetting REST server", appContext.getPlayerInfo().getId()));

        try
        {
            //set web target
            WebTarget matchTarget = restBaseUrl.path(restEndpointsIndex.get("Match"));

            //invocation
            Invocation.Builder invocation = matchTarget.request();

            //make request
            Response response = invocation.delete();

            //read response
            if (response.getStatus() == 200)
            {
                //log
                PrettyPrinter.printTimestampLog(String.format("[%s] REST server reset", appContext.getPlayerInfo().getId()));
            }

        }catch (Exception ex)
        {
            //log
            PrettyPrinter.printTimestampError(String.format("[%s] Error resetting server", appContext.getPlayerInfo().getId()));
        }
    }
}

