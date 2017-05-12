package com.fv.sdp;

import com.fv.sdp.model.TestModel;
import com.fv.sdp.resource.MatchResource;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;


@ApplicationPath("/")
/*
public class MyApplication extends Application
{
    @Override
    public Set<Class<?>> getClasses() {
        HashSet h = new HashSet<Class<?>>();
        //h.add( HelloWorld.class );
        h.add(MatchResource.class);
        //h.add(TestModel.class);
        return h;
    }
}
*/
public class MyApplication extends ResourceConfig
{
    public MyApplication()
    {
        packages("com.fv.sdp");
    }
}