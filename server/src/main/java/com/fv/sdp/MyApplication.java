package com.fv.sdp;

import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;


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