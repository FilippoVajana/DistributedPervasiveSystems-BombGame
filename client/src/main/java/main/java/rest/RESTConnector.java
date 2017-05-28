package main.java.rest;

import com.fv.sdp.model.Match;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
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

    public RESTConnector(String serviceBaseUrl, HashMap<String, String> serviceEndpoints)
    {
        //client
        restClient = ClientBuilder.newClient();
        //base url
        restBaseUrl = restClient.target(serviceBaseUrl);
        //endpoints
        restEndpointsIndex = serviceEndpoints;
    }

    public boolean requestSetTestModel()
    {
        WebTarget matchTarget = restBaseUrl.path(restEndpointsIndex.get("Match"));
        //test model web target
        WebTarget testModelTarget = matchTarget.path("setTest");

        //invocation
        Invocation.Builder invocation = testModelTarget.request();

        //make request
        Response response = invocation.get();
        return true;
    }

    public ArrayList<Match> requestMatchesList()
    {
        return null;
    }

    public static void main(String[] args)
    {
        //base url
        String serverBaseUrl = "http://localhost:8080/server_war_exploded";
        //service endpoints
        HashMap<String, String> serverEndpoints = new HashMap<>();
        serverEndpoints.put("Match", "match");

        RESTConnector rest = new RESTConnector(serverBaseUrl, serverEndpoints );

        //set test model
        rest.requestSetTestModel();
    }
}
