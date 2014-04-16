/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tileworld.agent;
import sim.field.grid.ObjectGrid2D;
import tileworld.Parameters;
import tileworld.environment.*;
import tileworld.exceptions.CellBlockedException;
import tileworld.planners.AstarPathGenerator;
import tileworld.planners.TWPath;
import java.util.ArrayList;
import java.util.List;
import sim.util.Int2D;
import static tileworld.environment.TWDirection.*;
/**
 * TWContextBuilder
 *
 * @author michaellees
 * Created: Feb 6, 2011
 *
 * Copyright michaellees Expression year is undefined on line 16, column 24 in Templates/Classes/Class.java.
 *
 *
 * Description:
 *
 */
public class SimpleTWAgent extends TWAgent {

    private boolean trackbackFlag = false;
    public static final int REFUEL_THRESHOLD_BUFFER = 20;
    public static final int ASTAR_MAX_SEARCH_DISTANCE = Parameters.xDimension * Parameters.yDimension;

    //private String randomFlag = "";

    public SimpleTWAgent(int xpos, int ypos, TWEnvironment env, double fuelLevel) {
        super(xpos,ypos,env,fuelLevel);
    }

    // Get the tiles or holes that can be seen by the agent
    protected List<TWEntity> getEntitiesInRange() {
        int sensorRange = Parameters.defaultSensorRange;
        List<TWEntity> entityList = new ArrayList<TWEntity>();
        ObjectGrid2D objectGrid = this.getEnvironment().getObjectGrid();
        int topLeftX = (this.getX() - sensorRange) <= 0 ? 0 : this.getX() - sensorRange;
        int topLeftY = (this.getY() - sensorRange) <= 0 ? 0 : this.getY() - sensorRange;
        int bottomRightX = (this.getX() + sensorRange) >= Parameters.xDimension ? Parameters.xDimension-1 : this.getX() + sensorRange;
        int bottomRightY = (this.getY() + sensorRange) >= Parameters.yDimension ? Parameters.yDimension-1 : this.getY() + sensorRange;
        for (int i = topLeftX; i <= bottomRightX; i++) {
            for (int j = topLeftY; j <= bottomRightY; j++) {
                TWEntity e = (TWEntity) objectGrid.get(i, j);

                if (e instanceof TWTile || e instanceof TWHole) {
                    entityList.add(e);
                }
            }
        }
        return entityList;
    }
    protected int getTrackbackThreshold ()
    {
        AstarPathGenerator astar = new AstarPathGenerator(getEnvironment(), this, ASTAR_MAX_SEARCH_DISTANCE);
        TWPath path = astar.findPath(this.getX(), this.getY(), 0, 0);
        return path != null && (path.getpath().size() <= fuelLevel) ? path.getpath().size() + 1 : -1;
    }

//    protected TWPath getNotSoRandomPath() {
//
//        int sensorRange = Parameters.defaultSensorRange;
//        if(this.getX() > this.getY()) {
//            AstarPathGenerator astar = new AstarPathGenerator(getEnvironment(), this, sensorRange * sensorRange);
//            return astar.findPath(this.getX(), this.getY(), this.getX(), Parameters.yDimension - 1);
//        }
//        //if(randomFlag.equalsIgnoreCase("")) {
//
//            int topLeftX = (this.getX() - sensorRange - 1) <= 0 ? -1 : this.getX() - sensorRange - 1;
//            int topLeftY = (this.getY() - sensorRange - 1) <= 0 ? -1 : this.getY() - sensorRange - 1;
//            int bottomRightX = (this.getX() + sensorRange + 1) >= Parameters.xDimension ? -1 : this.getX() + sensorRange + 1;
//            int bottomRightY = (this.getY() + sensorRange + 1) >= Parameters.yDimension ? -1 : this.getY() + sensorRange + 1;
//
//            AstarPathGenerator astar = new AstarPathGenerator(getEnvironment(), this, sensorRange * sensorRange);
//            TWPath topLeftPath = null, topRightPath = null, bottomLeftPath = null, bottomRightPath = null;
//
//            if (topLeftX != -1 && topLeftY != -1)//&& topLeftX < topLeftY)
//                topLeftPath = astar.findPath(this.getX(), this.getY(), topLeftX, topLeftY);
//            if (bottomRightX != -1 && topLeftY != -1)//&& bottomRightX < topLeftY)
//                topRightPath = astar.findPath(this.getX(), this.getY(), bottomRightX, topLeftY);
//            if (topLeftX != -1 && bottomRightY != -1)//&& topLeftX < bottomRightY)
//                topRightPath = astar.findPath(this.getX(), this.getY(), topLeftX, bottomRightY);
//            if (bottomRightX != -1 && bottomRightY != -1)// && bottomRightX < bottomRightY)
//                bottomRightPath = astar.findPath(this.getX(), this.getY(), bottomRightX, bottomRightY);
//
//            ArrayList<TWPath> paths = new ArrayList<TWPath>();
//            if (topLeftPath != null)
//                paths.add(topLeftPath);
//            if (topRightPath != null)
//                paths.add(topRightPath);
//            if (bottomLeftPath != null)
//                paths.add(bottomLeftPath);
//            if (bottomRightPath != null)
//                paths.add(bottomRightPath);
//
//            TWPath path = comparePathDistances(paths);
//            return path;
//        //}
//        //}
////        if(randomFlag.equalsIgnoreCase("TL"))
////        if(randomFlag.equalsIgnoreCase("TR"))
////        if(randomFlag.equalsIgnoreCase("BL"))
////        if(randomFlag.equalsIgnoreCase("BR"))
//    }

    protected TWPath getNotSoRandomPath() {//(TWAgent agent, TWEnvironment environment) {
        //Temp: Chooses a random location and moves towards it
        AstarPathGenerator astar = new AstarPathGenerator(getEnvironment(), this, ASTAR_MAX_SEARCH_DISTANCE);
        while (true) {
            //Generate a random location
            Int2D target = this.getEnvironment().generateFarRandomLocation(this.getX(), this.getY(), Parameters.defaultSensorRange * 2);
            while(target.getX() > target.getY())
                target = this.getEnvironment().generateFarRandomLocation(this.getX(), this.getY(), Parameters.defaultSensorRange+1);
            TWPath pathFound = astar.findPath(this.getX(), this.getY(), target.getX(), target.getY());
            if (pathFound != null) {
                return pathFound;
            }
        }
    }

//    protected TWPath comparePathDistances(List<TWPath> paths)
//    {
//        int minPathLength = Integer.MAX_VALUE, size = 0;
//        TWPath bestPath = null;
//
//        for(TWPath path: paths)
//        {
//            size = path.getpath().size();
//            if(size < minPathLength)
//            {
//                minPathLength = size;
//                bestPath = path;
//            }
//        }
//        return bestPath;
//    }

    // Decide what to do if there are tiles or holes within range
    protected TWThought getThoughtForEntitiesRange(List<TWEntity> entityList) {
        AstarPathGenerator astar = new AstarPathGenerator(getEnvironment(), this, ASTAR_MAX_SEARCH_DISTANCE);

        if (entityList.size() > 0) {
            System.out.println("I FUCKING FOUND A TILE OR A HOLE!");

            // Get the distance to the closest tile and hole within range (if any)
            double minHoleDistance = Double.MAX_VALUE;
            double minTileDistance = Double.MAX_VALUE;
            TWHole nearestHole = null;
            TWTile nearestTile = null;

            for (TWEntity entity : entityList) {

                double distance = entity.getDistanceTo(this);

                if (entity instanceof TWHole) {
                    if (distance < minHoleDistance) {
                        minHoleDistance = distance;
                        nearestHole = (TWHole) entity;
                    }
                }
                else if (entity instanceof TWTile) {
                    if (distance < minTileDistance) {
                        minTileDistance = distance;
                        nearestTile = (TWTile) entity;
                    }
                }
            }

            // If can't pick up any more tiles or some tiles are being carried and there is a hole closer than a tile
            if (nearestHole != null && (this.carriedTiles.size() == 3 || this.hasTile() && minHoleDistance < minTileDistance)) {
                TWPath pathToNearestHole = astar.findPath(this.getX(), this.getY(), nearestHole.getX(), nearestHole.getY());

                if (pathToNearestHole != null) {

                    // If on the way to the fuel station, check if there's enough fuel to deviate from the path
                    if(trackbackFlag) {
                        TWPath pathNearestHoleToFuel = astar.findPath(nearestHole.getX(), nearestHole.getY(), 0, 0);
                        TWPath pathToFuelStation = astar.findPath(getX(), getY(), 0, 0);

                        // If the path to the nearest entity + path from entity to fuel station is too costly, go
                        // directly to the fuel station
                        if(pathNearestHoleToFuel != null && pathToNearestHole.getpath().size() + pathNearestHoleToFuel.getpath().size() > this.fuelLevel) {
                            return new TWThought(TWAction.MOVE, pathToFuelStation.getStep(0).getDirection());
                        }
                    }
                    return new TWThought(TWAction.MOVE, pathToNearestHole.getStep(0).getDirection());
                }
            }

            // Otherwise if there is a tile nearby, pick it up
            else if (this.carriedTiles.size() < 3 && nearestTile != null) {
                TWPath pathToNearestTile = astar.findPath(this.getX(), this.getY(), nearestTile.getX(), nearestTile.getY());

                if (pathToNearestTile != null) {

                    // If on the way to the fuel station, check if there's enough fuel to deviate from the path
                    if(trackbackFlag) {

                        TWPath pathNearestTileToFuel = astar.findPath(nearestTile.getX(), nearestTile.getY(), 0, 0);
                        TWPath pathToFuelStation = astar.findPath(getX(), getY(), 0, 0);

                        // If the path to the nearest entity + path from entity to fuel station is too costly, go
                        // directly to the fuel station
                        if(pathNearestTileToFuel != null && pathToNearestTile.getpath().size() + pathNearestTileToFuel.getpath().size() > this.fuelLevel) {
                            return new TWThought(TWAction.MOVE, pathToFuelStation.getStep(0).getDirection());
                        }
                    }
                    return new TWThought(TWAction.MOVE, pathToNearestTile.getStep(0).getDirection());
                }
            }
        }
        return null;
    }

    // A helper method used by the agent's think() method (refactored so that agent can be subclassed)
    protected TWThought thinkHelper() {

        List<TWEntity> entityList = this.getEntitiesInRange();
        // If I'm at the fuel station, refuel
        if (this.fuelLevel < 1000 && this.x == this.y && this.x == 0) {
            return new TWThought(TWAction.REFUEL, TWDirection.Z);
        }

        // When standing on a tile, pick it up
        if(this.carriedTiles.size() < 3 && this.getEnvironment().getObjectGrid().get(this.getX(), this.getY()) instanceof TWTile){
            return new TWThought(TWAction.PICKUP, TWDirection.Z);
        }

        // When standing over a hole, drop a tile
        if(this.hasTile() && this.getEnvironment().getObjectGrid().get(this.getX(), this.getY()) instanceof TWHole){
            return new TWThought(TWAction.PUTDOWN, TWDirection.Z);
        }

        // Decide whether refueling is needed
        int threshold = getTrackbackThreshold();
        if(threshold == -1 && x != 0 && y != 0) {
            return new TWThought(TWAction.WAIT, TWDirection.Z);
        }

        // If fuel is low or agent is already tracking back
        if (this.fuelLevel <= threshold + REFUEL_THRESHOLD_BUFFER || trackbackFlag) {
            trackbackFlag = true;

            // Find a path to the fuel station from the current position
            AstarPathGenerator astar = new AstarPathGenerator(getEnvironment(), this, ASTAR_MAX_SEARCH_DISTANCE);
            TWPath path = astar.findPath(this.getX(), this.getY(), 0, 0);

            System.out.println("Tracking back->Simple Score: " + this.score);

            if (path != null) {
                // Decide whether to deviate or not from the path if a tile or a hole is within range
                TWThought currentBestThought = getThoughtForEntitiesRange(entityList);
                if(currentBestThought != null) {
                    return currentBestThought;
                }
                return new TWThought(TWAction.MOVE,path.getStep(0).getDirection());
            }
        }

        // Fuel level is normal and there are tiles or holes within range
        TWThought currentBestThought = getThoughtForEntitiesRange(entityList);
        if(currentBestThought != null) {
            return currentBestThought;
        }

        // If agent sees nothing in its sensor range, retrieve tiles and holes seen nearby from the memory and generate
        // a path towards them; eventually they will come within range (handled above) or we'll keep moving towards them
        TWTile recentTile = getMemory().getNearbyTile(getX(), getY(), 15);
        TWHole recentHole = getMemory().getNearbyHole(getX(), getY(), 15);

        AstarPathGenerator astar = new AstarPathGenerator(getEnvironment(), this, ASTAR_MAX_SEARCH_DISTANCE);
        TWPath tilePath = null, holePath = null;

        // Find paths to a recently seen tile and hole (if any)
        if(recentTile != null && this.carriedTiles.size() < 3) {
            if (isObjectInSensorRange(recentTile) && !getEntitiesInRange().contains(recentTile)) {
                getMemory().removeObject(recentTile);
            }
            else {
                tilePath = astar.findPath(this.getX(), this.getY(), recentTile.getX(), recentTile.getY());
            }
        }
        if(recentHole != null && this.hasTile()) {
            if (isObjectInSensorRange(recentHole) && !getEntitiesInRange().contains(recentHole)) {
                getMemory().removeObject(recentHole);
            }
            else {
                holePath = astar.findPath(this.getX(), this.getY(), recentHole.getX(), recentHole.getY());
            }
        }

        // If there is a path to a recently seen hole, go to it (score is more important than number of tiles carried)
        if(holePath != null)
            return new TWThought(TWAction.MOVE, holePath.getStep(0).getDirection());

        // Otherwise go to a recently seen tile
        if(tilePath != null)
            return new TWThought(TWAction.MOVE, tilePath.getStep(0).getDirection());

        // We got nothing, defer to the think() method
        return null;
    }

    protected boolean isObjectInSensorRange(TWEntity entity)
    {
        return entity.getX()>=this.getX()-Parameters.defaultSensorRange && entity.getX()<=this.getX()+Parameters.defaultSensorRange
                && entity.getY()>=this.getY()-Parameters.defaultSensorRange && entity.getY()<=this.getY()+Parameters.defaultSensorRange;
    }

    protected TWThought think() {
        TWThought thought = thinkHelper();
        System.out.println("First Agent Tiles: " + this.carriedTiles.size());
        if (thought == null) {
            TWPath notSoRandomPath = getNotSoRandomPath();
            if(notSoRandomPath != null)
                return new TWThought(TWAction.MOVE, notSoRandomPath.getStep(0).getDirection());
            return new TWThought(TWAction.MOVE, getBoundedDirection());
        }
        return thought;
    }

    public List<TWAgentPercept> getMessage(){
//        return Double.toString(fuelLevel);
        List<TWAgentPercept> list = new ArrayList<TWAgentPercept>();
        TWAgentWorkingMemory memory = this.getMemory();

        list.add(new TWAgentPercept(memory.getNearbyHole(this.x,this.y,5),500));
        list.add(new TWAgentPercept(memory.getNearbyTile(this.x,this.y,5),500));
        list.add(new TWAgentPercept(memory.getClosestObjectInSensorRange(TWTile.class),500));

        return list;
    }

    protected TWDirection getBoundedDirection() {
        TWDirection randomDirection;
        int newX = x , newY = y;
        if(newX > newY)
            return TWDirection.W;
        else do
        {
            randomDirection = getRandomDirection();
            switch(randomDirection)
            {
                case N:
                    newY -=1;
                    break;
                case E:
                    newX  +=1;
                    break;
                case W:
                    newX -=1;
                    break;
                case S:
                    newY +=1;
                    break;
            }
        } while (newX > newY);
        return randomDirection;
    }

    @Override
    protected void act(TWThought thought) {
        try {
            ObjectGrid2D objectGrid2D = this.getEnvironment().getObjectGrid();
            TWEntity e = (TWEntity) objectGrid2D.get(this.getX(), this.getY());

            switch (thought.getAction())
            {
                case MOVE:
                    move(thought.getDirection());
                    break;
                case PICKUP:
                    pickUpTile((TWTile)e);
                    System.out.println("Tiles: "+this.carriedTiles.size());
                    break;
                case PUTDOWN:
                    putTileInHole((TWHole)e);
                    System.out.println("Remaining Tiles: "+this.carriedTiles.size());
                    break;
                case REFUEL:
                    trackbackFlag=false;
                    super.refuel();
                    break;
                case WAIT:
                    System.out.println("jus 'angin 'round them obstacles people!");
                    break;
                default:
                    System.out.println("jus 'angin 'round them obstacles nigga!");
            }
        }  catch (CellBlockedException ex) {
        }
    }

//    protected TWDirection getRandomDirection(){
//
//        TWDirection randomDir = TWDirection.values()[this.getEnvironment().random.nextInt(5)];
//
//        if(this.getX()>=this.getEnvironment().getxDimension() ){
//            randomDir = TWDirection.W;
//        }else if(this.getX()<=0 ){
//            randomDir = TWDirection.E;
//        }else if(this.getY()<=0 ){
//            randomDir = TWDirection.S;
//        }else if(this.getY()>=this.getEnvironment().getxDimension() ){
//            randomDir = TWDirection.N;
//        }
//
//        return randomDir;
//
//    }

    protected TWDirection getRandomDirection(){
        TWDirection randomDir = values()[this.getEnvironment().random.nextInt(5)];
        switch (randomDir) {
            case N:
                if (getY() == 0) return TWDirection.S;
                break;
            case E:
                if (getX() == getEnvironment().getxDimension()) return TWDirection.W;
                break;
            case W:
                if (getX() == 0) return TWDirection.E;
                break;
            case S:
                if (getY() == getEnvironment().getyDimension()) return TWDirection.N;
                break;
        }
        return randomDir;
    }

    @Override
    public String getName() {
        return "Simple Agent 1";
    }
}