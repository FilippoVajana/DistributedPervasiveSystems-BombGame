package com.fv.sdp.ring;

public class GridBomb
{
    private GridSector SOE; //Sector Of Explosion

    public GridBomb(int value)
    {
        //compute sector id
        int sectId = value % 4;

        //set bomb sector
        switch (value)
        {
            case 0:
                SOE = GridSector.Green;
                break;
            case 1:
                SOE = GridSector.Red;
                break;
            case 2:
                SOE = GridSector.Blue;
                break;
            case 3:
                SOE = GridSector.Yellow;
                break;
        }
    }

    public GridSector getBombSOE()
    {
        return SOE;
    }
}
