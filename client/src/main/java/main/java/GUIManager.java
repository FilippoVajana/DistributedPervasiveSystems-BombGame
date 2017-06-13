package main.java;

import com.fv.sdp.model.Match;
import com.fv.sdp.model.Player;
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
    public boolean enterMatch()
    {
        System.out.println("Retrieving available matches . . .");
        //get list
        ArrayList<Match> matchList = new RESTConnector().getServerMatchList();

        System.out.println("Available matches: ");
        for (int i=0; i<matchList.size(); i++)
        {
            System.out.println(String.format("[#] %s [players: %d]", matchList.get(i).getId(), matchList.get(i).getPlayers().getList().size()));
        }

        //choose match
        System.out.println("Select match index: ");
        int index = inputReader.nextInt();

        //enter match
        Player player = SessionConfig.getInstance().getPlayerInfo();
        boolean joinResult = new RESTConnector().joinServerMatch(matchList.get(index), player );

        //set match
        if (joinResult)
        {
            System.out.println(String.format("Successfully joined match %s", matchList.get(index).getId()));
            return true;
        }
        else
            System.out.println(String.format("Error joining match %s", matchList.get(index).getId()));
        return false;
    }

    //todo create match


    //todo play


}
