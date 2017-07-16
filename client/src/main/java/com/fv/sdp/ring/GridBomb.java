package com.fv.sdp.ring;

public class GridBomb
{
    private GridSector SOE; //Sector Of Explosion

    public GridBomb(double value)
    {
        //compute sector id
        int sectId = (int) (value % 4);

        //set bomb sector
        switch (sectId)
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
