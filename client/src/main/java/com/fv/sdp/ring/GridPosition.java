package com.fv.sdp.ring;

public class GridPosition
{
    public int x, y;

    public GridPosition(int playerX, int playerY)
    {
        x = playerX;
        y = playerY;
    }

    public static boolean equals(GridPosition pos1, GridPosition pos2)
    {
        if (pos1.x == pos2.x && pos1.y == pos2.y)
            return true;
        return false;
    }
}
