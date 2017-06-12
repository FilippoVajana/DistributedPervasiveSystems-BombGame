package main.java;

import com.fv.sdp.model.Match;
import main.java.rest.RESTConnector;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by filip on 6/12/2017.
 */
public class GUIManager
{
    Scanner inputReader;

    public GUIManager()
    {
        inputReader = new Scanner(System.in);

        //testing
        System.out.println("Requesting server test data model . . .\n\n");
        new RESTConnector().requestSetTestModel();
        //welcome
        welcome();
    }

    //welcome message
    public void welcome()
    {
        System.out.println("### Welcome to TokenBombRing Land ###");
    }

    //todo menu

    //todo set nickname
    public void setNickname()
    {
        System.out.print("Enter your nickname: ");
        String nickname = inputReader.nextLine();
        System.out.println();

        SessionConfig.getInstance().PLAYER_NICKNAME = nickname;
        System.out.println("Nickname set to " + SessionConfig.getInstance().PLAYER_NICKNAME);
    }

    //todo get match list
    public void getMatchList
    {
        System.out.println("Retrieving available matches . . .");
        //get list
        ArrayList<Match> matchList = new RESTConnector().getServerMatchList();

        System.out.println("Available matches: ");
        for (Match m : matchList)
        {
            System.out.println(String.format("[#] %s [players: %d]", m.getId(), m.getPlayers().getList().size()));
        }
    }

    //todo create match

    //todo enter match

    //todo play


}
