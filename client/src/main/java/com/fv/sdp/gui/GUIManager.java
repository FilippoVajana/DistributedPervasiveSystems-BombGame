package com.fv.sdp.gui;

import com.fv.sdp.ApplicationContext;
import com.fv.sdp.model.Match;
import com.fv.sdp.model.Player;
import com.fv.sdp.rest.RESTConnector;
import com.fv.sdp.ring.GridBomb;
import com.fv.sdp.util.PrettyPrinter;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Created by filip on 6/12/2017.
 */

public class GUIManager
{
    //app context
    public ApplicationContext appContext;

    public GUIManager(@NotNull ApplicationContext appContext)
    {
        //log
        PrettyPrinter.printClassInit(this);

        //save context
        this.appContext = appContext;

        //welcome message
        welcome();

        //show menu
        new Thread(() -> showMenu()).start();
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

        while (true)
        {
            System.out.println("### Choose an action ###\n");
            System.out.println("[1] - Set nickname");
            System.out.println("[2] - Create new match");
            System.out.println("[3] - Enter existing match");
            System.out.println("[4] - Exit application");

            System.out.print("Enter option num: ");
            int option = inputReader.nextInt();
            switch (option)
            {
                case 1:
                    setNickname();
                    break;
                case 2:
                    createMatch();
                    break;
                case 3:
                    if (joinMatch())
                        play();
                    break;
                case 4:
                    exitApplication();
                    return;
            }
        }
    }

    private void exitApplication()
    {
        //node shutdown
        appContext.NODE_MANAGER.shutdownNode();
    }

    //set nickname
    public void setNickname()
    {
        Scanner inputReader = new Scanner(System.in);

        //read input
        String nickname;
        while (true)
        {
            System.out.print("Enter your nickname: "); //TODO: nickname not empty
            nickname = inputReader.nextLine();
            //check nickname
            if (nickname.equals(""))
                System.out.println("Invalid Nickname");
            else
            {
                System.out.println();
                break;
            }
        }

        //set nickname
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
            try
            {
                System.out.println(String.format("[%d] %s [players: %d]", i, matchList.get(i).getId(), matchList.get(i).getPlayers().getList().size()));
            }catch (NullPointerException ex)
            {
                System.out.println(String.format("[%d] %s [players: 0]", i, matchList.get(i).getId()));
            }
        }

        //choose match
        int index = 0;
        while (true)
        {
            System.out.println("Select match index: ");
            try
            {
                index = Integer.parseInt(inputReader.nextLine());
            }catch (Exception ex)
            {
                System.out.println("Invalid Input");
                continue;
            }
            //check input
            if (index >= matchList.size())
                System.out.println("Invalid Index Value");
            else
            {
                break;
            }
        }

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

        System.out.println("### Match creation menu ###\n");
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
    private boolean inputLock = false;
    public void play()
    {
        Scanner inputReader = new Scanner(System.in);
        while(inputLock == false)
        {
            //read input
           String input = inputReader.nextLine().toUpperCase();

           //check input
            switch (input)
            {
                case "W":
                    appContext.GAME_MANAGER.movePlayer(input);
                    break;
                case "S":
                    appContext.GAME_MANAGER.movePlayer(input);
                    break;
                case "A":
                    appContext.GAME_MANAGER.movePlayer(input);
                    break;
                case "D":
                    appContext.GAME_MANAGER.movePlayer(input);
                    break;
                case "B":
                    appContext.GAME_MANAGER.releaseBomb();
                    break;
                default:
                    System.out.println("Invalid Input");
            }
        }
    }

    public void notifyPlayerLost(Player killer)
    {
        //console output
        System.out.println(String.format("\n### %s, YOU LOST ###\n" +
                "### %s KILLED YOU ###", appContext.getPlayerInfo().getId(),killer.getId()));

        //lock input
        inputLock = true;
    }
    public void notifyPlayerLost()
    {
        //console output
        System.out.println(String.format("\n### %s, YOU LOST ###\n", appContext.getPlayerInfo().getId()));

        //lock input
        inputLock = true;
    }
    public void notifyPlayerWin()
    {
        //console output
        System.out.println(String.format("\n### %s, YOU WIN ###\n" +
                "### PLAYER SCORE: %d", appContext.getPlayerInfo().getId(), appContext.GAME_MANAGER.getPlayerScore()));

        //lock input
        inputLock = true;
    }
    public void notifyKill(Player killedPlayer)
    {
        //console output
        System.out.println(String.format("\n### %s, YOU KILLED %s ###\n" +
                "### SCORE: %d ###", appContext.getPlayerInfo().getId(), killedPlayer.getId(), appContext.GAME_MANAGER.getPlayerScore()));
    }
    public void notifyBombRelease(GridBomb bomb)
    {
        System.out.println(String.format("\n### BOMB RELEASED IN %s SECTOR - Minus 5 Seconds To Detonation  - ###", bomb.getBombSOE()));
    }
    public void notifyBombKills(int killedPlayers)
    {
        System.out.println(String.format("\n### %s, BOMB DETONATED - You Killed %d Players  - ###\n" +
                "### SCORE: %d ###", appContext.getPlayerInfo().getId(), killedPlayers, appContext.GAME_MANAGER.getPlayerScore()));
    }
    public void notifyGameEnd()
    {
        //console output
        System.out.println(String.format("\n### %s, MATCH END ###\n" +
                "### PLAYER SCORE: %d", appContext.getPlayerInfo().getId(), appContext.GAME_MANAGER.getPlayerScore()));

        //lock input
        inputLock = true;
    }
}
