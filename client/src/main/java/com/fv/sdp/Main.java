package com.fv.sdp;

import com.fv.sdp.ring.NodeManager;

/**
 * Created by filip on 6/16/2017.
 */
public class Main
{
    public static void main(String[] args)
    {
        NodeManager node = new NodeManager();
        node.startupNode();

        //wait exit
        synchronized (node.appContext.APP_EXIT_SIGNAL)
        {
            try
            {
                //System.out.println("WAIT EXIT SIGNAL");
                node.appContext.APP_EXIT_SIGNAL.wait();
                System.exit(0);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
