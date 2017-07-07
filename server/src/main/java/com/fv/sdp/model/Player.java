package com.fv.sdp.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by filip on 14/05/2017.
 */
@XmlRootElement
public class Player
{
    private String id;
    //p2p token-ring listening server
    private String address;
    private int port;

    public Player()
    {
        id = "";
        address = "";
        port = 0;
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

    public String getCompleteAddress()
    {
        return String.format("%s:%d", this.address, this.port);
    }
    @Override
    //equality over id
    public boolean equals(Object player)
    {
        if (player == null)
            return false;

        Player p = (Player) player;
        if (this.getId().equals(p.getId()))
            return true;
        return false;
    }
}
