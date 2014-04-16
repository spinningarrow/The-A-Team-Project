package tileworld;

/**
 * Parameters
 *
 * @author michaellees
 * Created: Apr 21, 2010
 *
 * Copyright michaellees 
 *
 * Description:
 *
 * Class used to store global simulation parameters.
 * Environment related parameters are still in the TWEnvironment class.
 *
 */
public class Parameters {

    //Simulation Parameters
    public final static int seed = 4162012; //no effect with gui
    public static final long endTime = 5000; //no effect with gui

    //Agent Parameters
    public static final int defaultFuelLevel = 1000;
    public static final int defaultSensorRange = 2;

    //Environment Parameters I
//    public static final int xDimension = 100; //size in cells
//    public static final int yDimension = 100;
//
//    //
//    //Object Parameters
//    public static final double tileMean = 0.2;
//    public static final double holeMean = 0.2;
//    public static final double obstacleMean = 0.2;
//    public static final double tileDev = 0.05f;
//    public static final double holeDev = 0.05f;
//    public static final double obstacleDev = 0.05f;
//    public static final int lifeTime = 100;

//****************************************************************
    //Environment Parameters II
    public static final int xDimension = 50; //size in cells
    public static final int yDimension = 50;

    //Object Parameters
    public static final double tileMean = 2;
    public static final double holeMean = 2;
    public static final double obstacleMean = 2;
    public static final double tileDev = 0.5f;
    public static final double holeDev = 0.5f;
    public static final double obstacleDev = 0.5f;
    public static final int lifeTime = 30;

//****************************************************************

//    //Environment Parameters III
//    public final static int seed = 9042014;
//    public static final int xDimension = 150; //size in cells
//    public static final int yDimension = 40;
//
//    //Object Parameters
//    public static final double tileMean = 0.02;
//    public static final double holeMean = 0.2;
//    public static final double obstacleMean = 0.5;
//    public static final double tileDev = 0.001f;
//    public static final double holeDev = 0.01f;
//    public static final double obstacleDev = 0.1f;
//    public static final int lifeTime = 120;
}
