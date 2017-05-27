package main.java.rest;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by filip on 5/27/2017.
 */
public class RESTConnector
{
    final String serverBaseUrl = "http://localhost:8080/server_war_exploded";
    public boolean requestSetTestModel()
    {
        //rest client
        Client client = ClientBuilder.newClient();

        //match web target
        WebTarget matchBaseTarget = client.target(serverBaseUrl).path("match");
        //test model web target
        WebTarget testModelTarget = matchBaseTarget.path("setTest");

        //invocation
        Invocation.Builder invocation = testModelTarget.request();

        //make request
        Response response =testModelTarget.request(MediaType.TEXT_PLAIN).get();
        return true;
    }


    public static void main(String[] args)
    {
        new RESTConnector().requestSetTestModel();
    }
}
