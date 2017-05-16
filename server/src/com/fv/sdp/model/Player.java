package com.fv.sdp.model;

/**
 * Created by filip on 14/05/2017.
 */
public class Player
{
    private String id;
    //p2p token-ring listening server
    private String address;
    private int port;

    public Player() {
    }

    public Player(String id, String address, int port) {
        this.id = id;
        this.address = address;
        this.port = port;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
