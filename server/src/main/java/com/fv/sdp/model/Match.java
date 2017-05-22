package com.fv.sdp.model;

import com.fv.sdp.util.ConcurrentList;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by filip on 12/05/2017.
 */
@XmlRootElement
public class Match
{
    private String id;
    private int edgeLength;
    private int victoryPoints;
    private ConcurrentList<Player> players;

    public Match() {
    }

    public Match(String id, int edgeLength, int victoryPoints) {
        this.id = id;
        this.edgeLength = edgeLength;
        this.victoryPoints = victoryPoints;
        this.players = new ConcurrentList<>();
    }

    public Match(String id, int edgeLength, int victoryPoints, ConcurrentList<Player> players) {
        this.id = id;
        this.edgeLength = edgeLength;
        this.victoryPoints = victoryPoints;
        this.players = players;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getEdgeLength() {
        return edgeLength;
    }

    public void setEdgeLength(int edgeLength) {
        this.edgeLength = edgeLength;
    }

    public int getVictoryPoints() {
        return victoryPoints;
    }

    public void setVictoryPoints(int victoryPoints) {
        this.victoryPoints = victoryPoints;
    }

    public ConcurrentList<Player> getPlayers() {
        return players;
    }

    public void setPlayers(ConcurrentList<Player> players) {
        this.players = players;
    }

    @Override
    //equality over id
    public boolean equals(Object match)
    {
        if (match == null)
        return false;

        Match m = (Match) match;
        if (this.getId().equals(m.getId()))
            return true;
        return false;
    }
}
