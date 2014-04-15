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
    //private String randomFlag = "";

    public SimpleTWAgent(int xpos, int ypos, TWEnvironment env, double fuelLevel) {
        super(xpos,ypos,env,fuelLevel);
    }

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
                if (e == null) {
                    continue;
                }
                else if (e instanceof TWTile || e instanceof TWHole) {
                    entityList.add(e);
                }
            }
        }
        return entityList;
    }
    protected int getTrackbackThreshold ()
    {
        AstarPathGenerator astar = new AstarPathGenerator(getEnvironment(), this, Parameters.xDimension * Parameters.yDimension);
        TWPath path = astar.findPath(this.getX(), this.getY(), 0, 0);
        return path != null && (path.getpath().size() <= fuelLevel) ? path.getpath().size() + 1 : -1;
    }

    protected TWPath getNotSoRandomPath() {

        int sensorRange = Parameters.defaultSensorRange;
        if(this.getX() > this.getY()) {
            AstarPathGenerator astar = new AstarPathGenerator(getEnvironment(), this, sensorRange * sensorRange);
            return astar.findPath(this.getX(), this.getY(), this.getX(), Parameters.yDimension - 1);
        }
        //if(randomFlag.equalsIgnoreCase("")) {

            int topLeftX = (this.getX() - sensorRange - 1) <= 0 ? -1 : this.getX() - sensorRange - 1;
            int topLeftY = (this.getY() - sensorRange - 1) <= 0 ? -1 : this.getY() - sensorRange - 1;
            int bottomRightX = (this.getX() + sensorRange + 1) >= Parameters.xDimension ? -1 : this.getX() + sensorRange + 1;
            int bottomRightY = (this.getY() + sensorRange + 1) >= Parameters.yDimension ? -1 : this.getY() + sensorRange + 1;

            AstarPathGenerator astar = new AstarPathGenerator(getEnvironment(), this, sensorRange * sensorRange);
            TWPath topLeftPath = null, topRightPath = null, bottomLeftPath = null, bottomRightPath = null;

            if (topLeftX != -1 && topLeftY != -1)//&& topLeftX < topLeftY)
                topLeftPath = astar.findPath(this.getX(), this.getY(), topLeftX, topLeftY);
            if (bottomRightX != -1 && topLeftY != -1)//&& bottomRightX < topLeftY)
                topRightPath = astar.findPath(this.getX(), this.getY(), bottomRightX, topLeftY);
            if (topLeftX != -1 && bottomRightY != -1)//&& topLeftX < bottomRightY)
                topRightPath = astar.findPath(this.getX(), this.getY(), topLeftX, bottomRightY);
            if (bottomRightX != -1 && bottomRightY != -1)// && bottomRightX < bottomRightY)
                bottomRightPath = astar.findPath(this.getX(), this.getY(), bottomRightX, bottomRightY);

            ArrayList<TWPath> paths = new ArrayList<TWPath>();
            if (topLeftPath != null)
                paths.add(topLeftPath);
            if (topRightPath != null)
                paths.add(topRightPath);
            if (bottomLeftPath != null)
                paths.add(bottomLeftPath);
            if (bottomRightPath != null)
                paths.add(bottomRightPath);

            TWPath path = comparePathDistances(paths);
            return path;
        //}
        //}
//        if(randomFlag.equalsIgnoreCase("TL"))
//        if(randomFlag.equalsIgnoreCase("TR"))
//        if(randomFlag.equalsIgnoreCase("BL"))
//        if(randomFlag.equalsIgnoreCase("BR"))
    }

    protected TWPath comparePathDistances(List<TWPath> paths)
    {
        int minPathLength = Integer.MAX_VALUE, size = 0;
        TWPath bestPath = null;

        for(TWPath path: paths)
        {
            size = path.getpath().size();
            if(size < minPathLength)
            {
                minPathLength = size;
                bestPath = path;
            }
        }
        return bestPath;
    }

    protected TWThought getThoughtForEntitiesRange(List<TWEntity> entityList)
    {
        if (entityList.size() > 0) {
            System.out.println("I FUCKING FOUND A TILE OR A HOLE!");
            // Find astar path to closest hole if we have carried tiles
            // if there is no other tile closer than the nearest hole
//            if (this.hasTile()) {
            double minHoleDistance = Double.MAX_VALUE;
            double minTileDistance = Double.MAX_VALUE;
            TWHole nearestHole = null;
            TWTile nearestTile = null;
            for (TWEntity entity : entityList) {
                if (entity instanceof TWHole) {
                    double distance = entity.getDistanceTo(this);
                    if (distance < minHoleDistance) {
                        minHoleDistance = distance;
                        nearestHole = (TWHole) entity;
                    }
                }
                else if (entity instanceof TWTile) {
                    double distance = entity.getDistanceTo(this);
                    if (distance < minTileDistance) {
                        minTileDistance = distance;
                        nearestTile = (TWTile) entity;
                    }
                }
            }
            if ((this.carriedTiles.size() == 3 && nearestHole != null) || this.hasTile() && minHoleDistance < minTileDistance) {
                AstarPathGenerator astar = new AstarPathGenerator(getEnvironment(), this, Parameters.defaultSensorRange * Parameters.defaultSensorRange);
                TWPath path = astar.findPath(this.getX(), this.getY(), nearestHole.getX(), nearestHole.getY());
                if (path != null) {
                    if(trackbackFlag)
                    {
                        AstarPathGenerator astar1 = new AstarPathGenerator(getEnvironment(), this, Parameters.defaultSensorRange * Parameters.defaultSensorRange);

                        TWPath path1 = astar1.findPath(this.getX(), this.getY(), 0, 0);
                        if(path1 != null && !(path.getpath().size() + path1.getpath().size() + 1 < this.fuelLevel))
                            new TWThought(TWAction.MOVE, path1.getStep(0).getDirection());
                    }
                    return new TWThought(TWAction.MOVE, path.getStep(0).getDirection());
                }
            }
//            }
            // Otherwise find astar path to closest tile
            else if (this.carriedTiles.size() < 3 && nearestTile != null) {
//                double minDistance = Double.MAX_VALUE;
//                TWTile nearestTile = null;
//                for (TWEntity entity : entityList) {
//                    if (entity instanceof TWTile) {
//                        double distance = entity.getDistanceTo(this);
//                        if (distance < minDistance) {
//                            minDistance = distance;
//                            nearestTile = (TWTile) entity;
//                        }
//                    }
//                }
//                if (nearestTile != null) {
                AstarPathGenerator astar = new AstarPathGenerator(getEnvironment(), this, Parameters.defaultSensorRange * Parameters.defaultSensorRange);
                TWPath path = astar.findPath(this.getX(), this.getY(), nearestTile.getX(), nearestTile.getY());
                if (path != null) {
                    if(trackbackFlag)
                    {
                        AstarPathGenerator astar1 = new AstarPathGenerator(getEnvironment(), this, Parameters.defaultSensorRange * Parameters.defaultSensorRange);

                        TWPath path1 = astar1.findPath(this.getX(), this.getY(), 0, 0);
                        if(path1 != null && !(path.getpath().size() + path1.getpath().size() + 1 < this.fuelLevel))
                            return new TWThought(TWAction.MOVE, path.getStep(0).getDirection());
                    }
                    return new TWThought(TWAction.MOVE, path.getStep(0).getDirection());
                }
//                }
            }
        }
        return null;
    }

    protected TWThought thinkHelper() {

        List<TWEntity> entityList = this.getEntitiesInRange();
        //refuel
        if (this.fuelLevel < 1000 && this.x == this.y && this.x == 0) {
            return new TWThought(TWAction.REFUEL, TWDirection.Z);
        }

        //pick up a TILE
        if(this.carriedTiles.size() < 3 && this.getEnvironment().getObjectGrid().get(this.getX(), this.getY()) instanceof TWTile){
            return new TWThought(TWAction.PICKUP, TWDirection.Z);
        }

        //put down a TILE
        if(this.carriedTiles.size() > 0 && this.getEnvironment().getObjectGrid().get(this.getX(), this.getY()) instanceof TWHole){
            return new TWThought(TWAction.PUTDOWN, TWDirection.Z);
        }

        int threshold = getTrackbackThreshold();
        if(threshold == -1 && x != 0 && y != 0) {
            return new TWThought(TWAction.WAIT, TWDirection.Z);
        }

        //System.out.println("THIS IS THE THRESHOLD MOFOs "+threshold);
        if (this.fuelLevel <= threshold + 20 || trackbackFlag) {
            trackbackFlag = true;
            AstarPathGenerator astar = new AstarPathGenerator(getEnvironment(), this, Parameters.xDimension * Parameters.yDimension);
            TWPath path = astar.findPath(this.getX(), this.getY(), 0, 0);
            System.out.println("Tracking back->Simple Score: " + this.score);
            if (path != null) {
                TWThought currentBestThought = getThoughtForEntitiesRange(entityList);
                if(currentBestThought != null)
                    return currentBestThought;
//                if(this.carriedTiles.size()<3 && this.getEnvironment().getObjectGrid().get(x,y) instanceof TWTile){
//                    System.out.println("Tracking back pickup");
//                    System.exit(8);
//                    return new TWThought(TWAction.PICKUP,null);
//                }
//                //put down a TILE
//                if(this.carriedTiles.size()>0 && this.getEnvironment().getObjectGrid().get(x,y) instanceof TWHole){
//                    System.out.println("Tracking back putdown");
//                    System.exit(2);
//                    return new TWThought(TWAction.PUTDOWN,null);
//                }
                return new TWThought(TWAction.MOVE,path.getStep(0).getDirection());
            }
        }

        TWThought currentBestThought = getThoughtForEntitiesRange(entityList);
        if(currentBestThought != null)
            return currentBestThought;
        /* Idea here is that if we are carrying no tiles and no tiles are sensed in our vision field
         * then we retrieve the last tile from our memory and generate a path towards it. Eventually, if we
         * come across a closer tile then the agent will automatically move to it (execute one of the blocks above)
         * or it will come to this point and continue to move to the same tile we identified. */
        TWTile tile = this.getMemory().getNearbyTile(this.getX(), this.getY(), 15);
        TWHole hole = this.getMemory().getNearbyHole(this.getX(), this.getY(), 15);
        AstarPathGenerator astarTilePath = null, astarHolePath = null;
        TWPath tilePath = null, holePath = null;
        if(tile != null && !isObjectInSensorRange(tile) && this.carriedTiles.size()<3) {
            astarTilePath = new AstarPathGenerator(getEnvironment(), this, Parameters.xDimension * Parameters.yDimension);
            tilePath = astarTilePath.findPath(this.getX(), this.getY(), tile.getX(), tile.getY());
        }
        if(hole != null && !isObjectInSensorRange(hole) && this.carriedTiles.size()>0) {
            astarHolePath = new AstarPathGenerator(getEnvironment(), this, Parameters.xDimension * Parameters.yDimension);
            holePath = astarHolePath.findPath(this.getX(), this.getY(), hole.getX(), hole.getY());
        }
        if(tilePath != null && holePath != null)
        {
            if(tilePath.getpath().size()<=holePath.getpath().size())
                return new TWThought(TWAction.MOVE, tilePath.getStep(0).getDirection());
            return new TWThought(TWAction.MOVE, holePath.getStep(0).getDirection());
        }
        if(tilePath != null)
            return new TWThought(TWAction.MOVE, tilePath.getStep(0).getDirection());
        if(holePath != null)
            return new TWThought(TWAction.MOVE, holePath.getStep(0).getDirection());

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
        System.out.println("Current Position: (" + x + ", " + y + ")");
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