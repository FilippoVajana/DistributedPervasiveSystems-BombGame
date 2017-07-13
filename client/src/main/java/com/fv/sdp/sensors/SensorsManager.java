package com.fv.sdp.sensors;


import com.fv.sdp.ApplicationContext;
import com.fv.sdp.ring.GridBomb;
import com.fv.sdp.util.ConcurrentList;
import com.fv.sdp.util.PrettyPrinter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by filip on 12/07/2017.
 */
public class SensorsManager
{
    private ApplicationContext appContext;
    private AccelerometerSimulator accelerometerSimulator;
    private DataBuffer dataBuffer;
    private List<Thread> workThreadsList;

    public SensorsManager(ApplicationContext appContext)
    {
        //log
        PrettyPrinter.printClassInit(this);

        //save app context
        this.appContext = appContext;

        //init buffer
        dataBuffer = new DataBuffer();
    }

    //start sim
    public void startSensorsSimulator()
    {
        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Starting sensors simulator", appContext.getPlayerInfo().getId()));

        //init workers list
        workThreadsList = new ArrayList<>();

        //add simulator thread
        accelerometerSimulator = new AccelerometerSimulator(dataBuffer);
        workThreadsList.add(new Thread(accelerometerSimulator));

        //add sensor data monitor thread
        workThreadsList.add(new Thread(() -> monitorSensorBuffer(this.dataBuffer)));

        //start workers
        for (Thread t : workThreadsList)
            t.start();
    }

    //stop sim
    public void stopSensorsSimulator()
    {
        //stop sensor sim
        accelerometerSimulator.stopMeGently();
    }

    //sensor data monitor
    private void monitorSensorBuffer(DataBuffer buffer)
    {
        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Started sensor data monitor", appContext.getPlayerInfo().getId()));

        //monitor loop
        while (true)
        {
            try
            {
                Thread.sleep(1000);

                //log
                //PrettyPrinter.printTimestampError(String.format("[%s] polling sensor data", appContext.getPlayerInfo().getId()));

                //read data
                List<Measurement> data = dataBuffer.readAllAndClean();

                //compress data
                double[] dataArray = new double[data.size()];
                for (int i = 0; i < dataArray.length; i++)
                {
                    dataArray[i] = data.get(i).getValue();
                    //System.out.print(dataArray[i] + ", "); //TODO: remove
                }
                //System.out.println(); //TODO: remove

                //analyze data
                analyzeSensorsData(dataArray);

            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    //analyze data
    private final double ALFA = .75;
    private final double TH = 55;
    private double EMA_prev = 0;
    public void analyzeSensorsData(double[] data) //TODO: check input type
    {
        //compute average
        double M = 0;
        for (double d : data)
            M+= d;
        M = M/data.length;

        //compute EMA
        double EMA = EMA_prev + ALFA * (M - EMA_prev);

        //debug
        //System.err.println(String.format("Avg. : %f, EMA : %f", M, EMA));

        //check for outliers
        if (EMA - EMA_prev > TH) //found outlier
        {
            //build new bomb
            GridBomb bomb = new GridBomb(EMA);

            //push new bomb
            appContext.GAME_MANAGER.addBomb(bomb);
        }
    }
}

class DataBuffer implements Buffer
{
    private ConcurrentList<Measurement> buffer;

    public DataBuffer()
    {
        //log
        PrettyPrinter.printClassInit(this);

        //init buffer
        this.buffer = new ConcurrentList();
    }

    @Override
    public void addNewMeasurement(Measurement measurement)
    {
        //log
        //PrettyPrinter.printTimestampError(String.format("add new measurement %f", measurement.getValue()));
        buffer.add(measurement);
    }

    @Override
    public List readAllAndClean()
    {
        //log
        //PrettyPrinter.printTimestampError(String.format("read data buffer (%s)", buffer.size()));

        //get data copy
        ArrayList<Measurement> data = buffer.getList();

        //flush buffer
        buffer.setList(new ArrayList<>());

        return data;
    }
}
