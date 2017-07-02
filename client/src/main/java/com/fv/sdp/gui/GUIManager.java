package com.fv.sdp.gui;

import com.fv.sdp.ApplicationContext;
import com.fv.sdp.model.Match;
import com.fv.sdp.model.Player;
import com.fv.sdp.rest.RESTConnector;
import com.fv.sdp.util.PrettyPrinter;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by filip on 6/12/2017.
 */

public class GUIManager
{
    //app context
    ApplicationContext appContext;

    public GUIManager(@NotNull ApplicationContext appContext)
    {
        //log
        PrettyPrinter.printClassInit(this);

        //save context
        this.appContext = appContext;
        //testing
        //System.out.println("Requesting server test data model . . .\n\n");
        //new RESTConnector().requestSetTestModel();
        //welcome
        welcome();
        //TODO: show menu
    }

    public ApplicationContext getAppContext()
    {
        return appContext;
    }

    //welcome message
    public void welcome()
    {
        System.out.println("### Welcome to TokenBombRing Land ###");
    }

    //main menu
    public void showMenu()
    {
        Scanner inputReader = new Scanner(System.in);

        System.out.println("### Choose an action ###\n");
        System.out.println("[1] - Set nickname");
        System.out.println("[2] - Create new match");
        System.out.println("[3] - Enter existing match");
        System.out.println("[4] - Exit application");

        System.out.print("Enter option num: ");
        int option = inputReader.nextInt();
        switch (option) {
            case 1:
                setNickname();
                break;
            case 2:
                createMatch();
                break;
            case 3:
                joinMatch();
                break;
            case 4:
                return;
        }
    }

    //set nickname
    public void setNickname()
    {
        Scanner inputReader = new Scanner(System.in);

        System.out.print("Enter your nickname: ");
        String nickname = inputReader.next();
        System.out.println();

        appContext.PLAYER_NICKNAME = nickname;
        System.out.println("Nickname set to " + appContext.PLAYER_NICKNAME);
    }

    //enter match
    public boolean joinMatch()
    {
        Scanner inputReader = new Scanner(System.in);

        System.out.println("Retrieving available matches . . .");
        //get list
        ArrayList<Match> matchList = new RESTConnector(appContext).getServerMatchList();

        System.out.println("Available matches: ");
        for (int i = 0; i < matchList.size(); i++)
        {
            System.out.println(String.format("[%d] %s [players: %d]", i, matchList.get(i).getId(), matchList.get(i).getPlayers().getList().size()));
        }

        //choose match
        System.out.println("Select match index: ");
        int index = inputReader.nextInt();

        //enter match
        Player player = appContext.getPlayerInfo();
        boolean joinResult = new RESTConnector(appContext).joinServerMatch(matchList.get(index), player );
        //TODO: token management + signal new node

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

    //create match
    public boolean createMatch()
    {
        Match match = new Match();
        Scanner inputReader = new Scanner(System.in);

        System.out.println("### Match creation menù ###\n");
        //match id
        System.out.print("Enter match id: ");
        match.setId( inputReader.nextLine());
        //edge
        System.out.print("Enter match edge length: ");
        match.setEdgeLength(inputReader.nextInt());
        //victory
        System.out.print("Enter match victory points: ");
        match.setVictoryPoints(inputReader.nextInt());

        boolean creationResult = new RESTConnector(appContext).createServerMatch(match);

        return creationResult;
    }

    //todo play


}
