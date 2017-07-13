package com.fv.sdp.gui;

import com.fv.sdp.ApplicationContext;
import com.fv.sdp.model.Match;
import com.fv.sdp.model.Player;
import com.fv.sdp.rest.RESTConnector;
import com.fv.sdp.ring.GridBomb;
import com.fv.sdp.ring.GridPosition;
import com.fv.sdp.util.ConcurrentList;
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
            try
            {
                Thread.sleep(500);
            }catch (Exception ex)
            {
                continue;
            }

            System.out.println("\n\n### Choose an action ###\n");
            System.out.println("[1] - Set nickname");
            System.out.println("[2] - Show matches");
            System.out.println("[3] - Create new match");
            System.out.println("[4] - Enter existing match");
            System.out.println("[5] - Exit application");
            System.out.println("[6] - Reset server");

            System.out.print("Enter option num: ");
            int option = inputReader.nextInt();
            switch (option)
            {
                case 1:
                    setNickname();
                    break;
                case 2:
                    showMatch();
                    break;
                case 3:
                    createMatch();
                    break;
                case 4:
                    if (joinMatch())
                        play();
                    break;
                case 5:
                    exitApplication();
                    return;
                case 6:
                    appContext.REST_CONNECTOR.resetServer();
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
    //show available match
    public void showMatch()//TODO: test
    {
        System.out.println("Retrieving available matches . . .");
        //get list
        ArrayList<Match> matchList = new ArrayList<>();
        try
        {
            matchList = new RESTConnector(appContext).getServerMatchList();
        }catch (Exception ex)
        {
            PrettyPrinter.printTimestampError("Error retrieving match list");
            return;
        }


        System.out.println("Available matches: ");
        for (int i = 0; i < matchList.size(); i++)
        {
            try
            {
                //System.out.println(String.format("[%d] %s [players: %d]", i, matchList.get(i).getId(), matchList.get(i).getPlayers().getList().size()));
                System.out.print(i + ")");;
                PrettyPrinter.printMatchDetails(matchList.get(i));
            }catch (NullPointerException ex)
            {
                System.out.println(String.format("[%d] %s [players: 0]", i, matchList.get(i).getId()));
            }
        }
    }
    //create match
    public void createMatch()
    {
        Match match = new Match();
        match.setPlayers(new ConcurrentList<>());  //set empty player
        Scanner inputReader = new Scanner(System.in);

        System.out.println("### Match creation menu ###\n");

        //match id
        while (true)
        {
            System.out.print("Enter match id: ");

            String mID = inputReader.nextLine();
            //check input
            if (mID.equals(""))
                System.out.println("Invalid ID");
            else
            {
                match.setId(mID);
                break;
            }
        }

        //edge
        while (true)
        {
            System.out.print("Enter match edge length: ");

            try
            {
                int mEdge = Integer.parseInt(inputReader.nextLine());

                //check value
                if (mEdge <= 0)
                {
                    System.out.println("Invalid Edge Value");
                    continue;
                }

                //set value
                match.setEdgeLength(mEdge);
                break;
            }catch (Exception ex)
            {
                System.out.println("Invalid Edge Value");
                continue;
            }
        }

        //victory
        while (true)
        {
            System.out.print("Enter match victory points: ");

            try
            {
                int mVictory = Integer.parseInt(inputReader.nextLine());

                //check value
                if (mVictory <= 0)
                {
                    System.out.println("Invalid Points Value");
                    continue;
                }

                //set value
                match.setVictoryPoints(mVictory);
                break;
            }catch (Exception ex)
            {
                System.out.println("Invalid Points Value");
                continue;
            }
        }

        try
        {
            new RESTConnector(appContext).createServerMatch(match);
        }catch (Exception ex)
        {
            PrettyPrinter.printTimestampError("Error creating new match");
            return;
        }
    }
    //enter match
    public boolean joinMatch()
    {
        Scanner inputReader = new Scanner(System.in);

        System.out.println("Retrieving available matches . . .");
        //get list
        ArrayList<Match> matchList = new ArrayList<>();
        try
        {
            matchList = new RESTConnector(appContext).getServerMatchList();
        }catch (Exception ex)
        {
            PrettyPrinter.printTimestampError("Error retrieving match list");
            return false;
        }

        System.out.println("Available matches: ");
        for (int i = 0; i < matchList.size(); i++)
        {
            try
            {
                //System.out.println(String.format("[%d] %s [players: %d]", i, matchList.get(i).getId(), matchList.get(i).getPlayers().getList().size()));
                System.out.print(i + ")");
                PrettyPrinter.printMatchDetails(matchList.get(i));
            }catch (NullPointerException ex)
            {
                System.out.println(String.format("[%d] %s [players: 0]", i, matchList.get(i).getId()));
            }
        }

        //choose match
        int index;
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
        boolean joinResult;
        try
        {
            joinResult = new RESTConnector(appContext).joinServerMatch(matchList.get(index), player);
        }catch (Exception ex)
        {
            PrettyPrinter.printTimestampError("Error joining match");
            return false;
        }

        //set match
        if (joinResult)
        {
            System.out.println(String.format("Successfully joined match %s", matchList.get(index).getId()));
            return true;
        }
        else
            //System.out.println(String.format("Error joining match %s", matchList.get(index).getId()));
        return false;
    }

    private boolean inputLock = false;
    public void play()
    {
        Scanner inputReader = new Scanner(System.in);
        while(true)
        {
            //check token
            try
            {
                while (appContext.TOKEN_MANAGER.isHasToken() == false) //no token
                {
                    //token signal
                    Object tokenSignal = appContext.TOKEN_MANAGER.getTokenStoreSignal();
                    synchronized (tokenSignal)
                    {
                        tokenSignal.wait();
                    }
                }
            }catch (Exception ex)
            {
                PrettyPrinter.printTimestampError(String.format("[%s] ERROR play() wait token", appContext.getPlayerInfo().getId()));
            }

            if (inputLock)
                return;

            //visual feedback
            System.out.println(String.format("\n\n### %s, CHOOSE ACTION ###", appContext.getPlayerInfo().getId()));
            System.out.println(String.format("### POSITION: (%d,%d) ###", appContext.GAME_MANAGER.getPlayerPosition().x, appContext.GAME_MANAGER.getPlayerPosition().y));
            System.out.println(String.format("### SCORE: %d ###", appContext.GAME_MANAGER.getPlayerScore()));
            System.out.println(String.format("### BOMBS: %d ###", appContext.GAME_MANAGER.getBombQueue().size()));

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
                    boolean release = appContext.GAME_MANAGER.releaseBomb();
                    if (release == false)
                        System.out.println("### INVALID ACTION ###");
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
                "### PLAYER SCORE: %d ###", appContext.getPlayerInfo().getId(), appContext.GAME_MANAGER.getPlayerScore()));

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
    public void notifyMove(GridPosition playerPosition)
    {
        System.out.println(String.format("### %s, MOVED TO (%d,%d) ###", appContext.getPlayerInfo().getId(), playerPosition.x, playerPosition.y));
    }
}
