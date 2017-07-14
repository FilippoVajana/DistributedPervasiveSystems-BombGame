package com.fv.sdp;

import com.fv.sdp.ring.NodeManager;

import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Created by filip on 6/16/2017.
 */
public class Main
{
    public static void main(String[] args)
    {
        Scanner reader = new Scanner(System.in);
        //server url
        System.out.println("Server address: ");
        String restIp;
        while (true)
        {
            restIp = reader.nextLine();
            if (restIp.equals(""))
                System.out.println("Invalid input");
            break;
        }

        System.out.println("Server port: ");
        int restPort;
        while (true)
        {
            try
            {
                restPort = Integer.parseInt(reader.nextLine());
                break;
            }catch (Exception ex)
            {
                System.out.println("Invalid input");
                continue;
            }
        }

        //player nickname
        System.out.println("Player nickname: ");
        String playerNickname;
        while (true)
        {
            playerNickname = reader.nextLine();
            if (playerNickname.equals(""))
                System.out.println("Invalid input");
            break;
        }

        NodeManager node = new NodeManager(restIp, restPort, playerNickname);
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
