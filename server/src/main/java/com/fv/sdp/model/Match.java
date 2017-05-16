package com.fv.sdp.model;

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

    public Match() {
    }

    public Match(String id, int edgeLength, int victoryPoints) {
        this.id = id;
        this.edgeLength = edgeLength;
        this.victoryPoints = victoryPoints;
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
}
