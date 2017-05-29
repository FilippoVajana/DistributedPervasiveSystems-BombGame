package main.java.rest;

import com.fv.sdp.model.Match;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
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
        ArrayList<Match> response = invocation.get(new GenericType<ArrayList<Match>>(){});

        //read response payload
        //ArrayList<Match> matches = response.readEntity(ArrayList.class);
        return response;
    }
}

