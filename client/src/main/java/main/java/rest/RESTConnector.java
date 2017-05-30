package main.java.rest;

import com.fv.sdp.model.Match;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by filip on 5/27/2017.
 */
public class RESTConnector
{
    private Client restClient = null;
    private WebTarget restBaseUrl = null;
    private Map<String, String> restEndpointsIndex = null;

    public RESTConnector(RESTConfig configuration)
    {
        //client
        restClient = ClientBuilder.newClient();
        //base url
        restBaseUrl = restClient.target(configuration.BASE_URL);
        //endpoints
        restEndpointsIndex = configuration.SERVICE_ENDPOINTS;
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

    public ArrayList<Match> requestMatchesList()
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
}

