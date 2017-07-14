package com.fv.sdp.ring;

import javax.validation.constraints.NotNull;

public class Grid
{
    //grid params
    private final int gridEdge; //TODO: check value

    private GridPosition playerPosition;
    private GridSector playerSector;

    public Grid(int edge)
    {
        gridEdge = edge;
    }

    //player position get/set
    public void setPlayerPosition(GridPosition position)
    {
        //log
        //PrettyPrinter.printTimestampLog(String.format("[%s] Player located at (%d,%d)", this.getClass().getSimpleName(), position.x, position.y));

        playerPosition = position;
        playerSector = setPlayerSector(position);
    }
    public GridPosition getPlayerPosition()
    {
        return playerPosition;
    }

    //grid edge get
    public int getGridEdge() {
        return gridEdge;
    }

    private GridSector setPlayerSector(@NotNull GridPosition position)
    {
        int x = position.x;
        int y = position.y;

        if (x < gridEdge/2 && y < gridEdge/2)
        {
            return GridSector.Blue;
        }
        if (x < gridEdge/2 && y >= gridEdge/2)
        {
            return GridSector.Green;
        }
        if (x >= gridEdge/2 && y < gridEdge/2)
        {
            return GridSector.Yellow;
        }
        if (x >= gridEdge/2 && y >= gridEdge/2)
        {
            return GridSector.Red;
        }

        //return default
        return GridSector.Blue;
    }
    public GridSector getPlayerSector()
    {
        return playerSector;
    }
}
