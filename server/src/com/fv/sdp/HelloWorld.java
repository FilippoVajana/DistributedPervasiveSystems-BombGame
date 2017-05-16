package com.fv.sdp;

import com.fv.sdp.model.TestModel;
import com.fv.sdp.MatchResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;

@Path("/helloworld")
public class HelloWorld {

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public TestModel getClichedMessage()
    {
        TestModel tm = new TestModel("asd", new ArrayList<>());
        tm.getList().add(2);
        tm.getList().add(45);
        return tm;
    }
}