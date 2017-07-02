package com.fv.sdp;

import com.fv.sdp.model.Match;
import com.fv.sdp.model.Player;
import com.fv.sdp.ring.AckHandler;
import com.fv.sdp.ring.GameManager;
import com.fv.sdp.ring.TokenManager;
import com.fv.sdp.socket.SocketConnector;
import com.fv.sdp.util.ConcurrentList;
import java.util.HashMap;

public class ApplicationContext
{
    public ApplicationContext()
    {
        //init REST
        RESTConfig();
    }

    //REST
    public String REST_BASE_URL;
    public HashMap<String, String> REST_ENDPOINTS = new HashMap<>();
    private void RESTConfig()
    {
        //base server url
        REST_BASE_URL = "http://localhost:8080/server_war_exploded/";
        //endpoints
        REST_ENDPOINTS.put("Match", "match");
    }

    //RING
    public ConcurrentList<Player> RING_NETWORK = new ConcurrentList<>();

    //NODE
    public SocketConnector SOCKET_CONNECTOR;
    public AckHandler ACK_HANDLER;
    public GameManager GAME_MANAGER;
    public TokenManager TOKEN_MANAGER;

    //PLAYER
    public String PLAYER_NICKNAME;
    public Match PLAYER_MATCH;
    public String LISTENER_ADDR;
    public int LISTENER_PORT;

    public void setPlayerInfo(String nickname, String address, int port)
    {
        PLAYER_NICKNAME = nickname;
        LISTENER_ADDR = address;
        LISTENER_PORT = port;
    }
    public Player getPlayerInfo()
    {
        return new Player(PLAYER_NICKNAME, LISTENER_ADDR, LISTENER_PORT);
    }




}
