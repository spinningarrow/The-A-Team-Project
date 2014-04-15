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
public class SimpleTWAgent extends TWAgent{
    private boolean trackbackFlag = false;
    public SimpleTWAgent(int xpos, int ypos, TWEnvironment env, double fuelLevel) {
        super(xpos,ypos,env,fuelLevel);
    }
    List<TWEntity> getEntitiesInRange() {
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
    int getTrackbackThreshold ()
    {
        AstarPathGenerator astar = new AstarPathGenerator(getEnvironment(), this, Parameters.xDimension * Parameters.yDimension);
        TWPath path = astar.findPath(this.x, this.y, 0, 0);
        return path!=null && (path.getpath().size()<=fuelLevel) ? path.getpath().size()+1 : -1;
    }

    protected TWPath getNotSoRandomPath() {
        int sensorRange = Parameters.defaultSensorRange;
        //ObjectGrid2D curMemoryGrid = this.getMemory().getMemoryGrid();
        int topLeftX = (this.getX() - sensorRange - 1) <= 0 ? 0 : this.getX() - sensorRange - 1;
        int topLeftY = (this.getY() - sensorRange - 1) <= 0 ? 0 : this.getY() - sensorRange - 1;
        int bottomRightX = (this.getX() + sensorRange + 1) >= Parameters.xDimension ? Parameters.xDimension : this.getX() + sensorRange + 1;
        int bottomRightY = (this.getY() + sensorRange + 1) >= Parameters.yDimension ? Parameters.yDimension : this.getY() + sensorRange + 1;
        AstarPathGenerator astar = new AstarPathGenerator(getEnvironment(), this, Parameters.xDimension * Parameters.yDimension);
        TWPath topPath = null, bottomPath = null;
        if(topLeftX < topLeftY)
            topPath = astar.findPath(this.x, this.y, topLeftX, topLeftY);
        if(bottomRightX < bottomRightY)
            bottomPath = astar.findPath(this.x, this.y, bottomRightX, bottomRightY);
        if(topPath != null && bottomPath !=null)
            return topPath.getpath().size() <= bottomPath.getpath().size() ? topPath: bottomPath;
        if(topPath !=null)
            return topPath;
        return bottomPath;
    }

    TWThought thinkHelper() {

        List<TWEntity> entityList = this.getEntitiesInRange();
        //refuel
        if (this.fuelLevel<1000 && this.x == this.y && this.x == 0) {
            return new TWThought(TWAction.REFUEL,null);
        }

        //pick up a TILE
        if(this.carriedTiles.size()<3 && this.getEnvironment().getObjectGrid().get(x,y) instanceof TWTile){
            return new TWThought(TWAction.PICKUP,null);
        }

        //put down a TILE
        if(this.carriedTiles.size()>0 && this.getEnvironment().getObjectGrid().get(x,y) instanceof TWHole){
            return new TWThought(TWAction.PUTDOWN,null);
        }

        int addedThreshold = 80;
        int threshold = addedThreshold + getTrackbackThreshold();
        if(threshold == -1 && x!=0 && y!=0) {
            return new TWThought(TWAction.WAIT,null);
        }

        //System.out.println("THIS IS THE THRESHOLD MOFOs "+threshold);
        if (this.fuelLevel <= threshold || trackbackFlag) {
            trackbackFlag = true;
            AstarPathGenerator astar = new AstarPathGenerator(getEnvironment(), this, Parameters.xDimension * Parameters.yDimension);
            TWPath path = astar.findPath(this.x, this.y, 0, 0);


            System.out.println("Tracking back->Simple Score: " + this.score);
            if (path != null) {

                if (entityList.size() > 0) {
                    System.out.println("Found something while tracking back.");
                    if (this.hasTile()) {
                        double minHoleDistance = Double.MAX_VALUE;
                        double minTileDistance = Double.MAX_VALUE;
                        TWHole nearestHole = null;
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
                                }
                            }
                        }
                            if ((this.carriedTiles.size() == 3 && nearestHole != null) || minHoleDistance < minTileDistance) {
                                AstarPathGenerator astar2 = new AstarPathGenerator(getEnvironment(), this, Parameters.defaultSensorRange * Parameters.defaultSensorRange);
                            TWPath path2 = astar2.findPath(x, y, nearestHole.getX(), nearestHole.getY());
                            if (path2 != null) {
                                return new TWThought(TWAction.MOVE, path2.getStep(0).getDirection());
                            }
                        }
                    }
                    // Otherwise find astar path to closest tile
                    if (this.carriedTiles.size() < 3) {
                        double minDistance = Double.MAX_VALUE;
                        TWTile nearestTile = null;
                        for (TWEntity entity : entityList) {
                            if (entity instanceof TWTile) {
                                double distance = entity.getDistanceTo(this);
                                if (distance < minDistance) {
                                    minDistance = distance;
                                    nearestTile = (TWTile) entity;
                                }
                            }
                        }
                        if (nearestTile != null) {
                            AstarPathGenerator astar3 = new AstarPathGenerator(getEnvironment(), this, Parameters.defaultSensorRange * Parameters.defaultSensorRange);
                            TWPath path3 = astar3.findPath(x, y, nearestTile.getX(), nearestTile.getY());
                            if (path3 != null) {
                                return new TWThought(TWAction.MOVE, path3.getStep(0).getDirection());
                            }
                        }
                    }
                }



                if(this.carriedTiles.size()<3 && this.getEnvironment().getObjectGrid().get(x,y) instanceof TWTile){
                    System.out.println("Tracking back pickup");
                    System.exit(8);
                    return new TWThought(TWAction.PICKUP,null);
                }
                //put down a TILE
                if(this.carriedTiles.size()>0 && this.getEnvironment().getObjectGrid().get(x,y) instanceof TWHole){
                    System.out.println("Tracking back putdown");
                    System.exit(2);
                    return new TWThought(TWAction.PUTDOWN,null);
                }
                return new TWThought(TWAction.MOVE,path.getStep(0).getDirection());
            }
        }

        if (entityList.size() > 0) {
            System.out.println("I FUCKING FOUND A TILE OR A HOLE!");
            // Find astar path to closest hole if we have carried tiles
            // if there is no other tile closer than the nearest hole
            if (this.hasTile()) {
                double minHoleDistance = Double.MAX_VALUE;
                double minTileDistance = Double.MAX_VALUE;
                TWHole nearestHole = null;
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
                        }
                    }
                }
                if ((this.carriedTiles.size() == 3 && nearestHole != null) || minHoleDistance < minTileDistance) {
                    AstarPathGenerator astar = new AstarPathGenerator(getEnvironment(), this, Parameters.defaultSensorRange * Parameters.defaultSensorRange);
                    TWPath path = astar.findPath(x, y, nearestHole.getX(), nearestHole.getY());
                    if (path != null) {
                        return new TWThought(TWAction.MOVE, path.getStep(0).getDirection());
                    }
                }
            }
            // Otherwise find astar path to closest tile
            if (this.carriedTiles.size() < 3) {
                double minDistance = Double.MAX_VALUE;
                TWTile nearestTile = null;
                for (TWEntity entity : entityList) {
                    if (entity instanceof TWTile) {
                        double distance = entity.getDistanceTo(this);
                        if (distance < minDistance) {
                            minDistance = distance;
                            nearestTile = (TWTile) entity;
                        }
                    }
                }
                if (nearestTile != null) {
                    AstarPathGenerator astar = new AstarPathGenerator(getEnvironment(), this, Parameters.defaultSensorRange * Parameters.defaultSensorRange);
                    TWPath path = astar.findPath(x, y, nearestTile.getX(), nearestTile.getY());
                    if (path != null) {
                        return new TWThought(TWAction.MOVE, path.getStep(0).getDirection());
                    }
                }
            }
        }
        /* Idea here is that if we are carrying no tiles and no tiles are sensed in our vision field
         * then we retrieve the last tile from our memory and generate a path towards it. Eventually, if we
         * come across a closer tile then the agent will automatically move to it (execute one of the blocks above)
         * or it will come to this point and continue to move to the same tile we identified. */
        TWTile tile = this.getMemory().getNearbyTile(this.x, this.y, 15);
        TWHole hole = this.getMemory().getNearbyHole(this.x,this.y,15);
        if(tile != null && hole != null)
        {
            AstarPathGenerator astar = new AstarPathGenerator(getEnvironment(), this, Parameters.xDimension * Parameters.yDimension);
            TWPath path = astar.findPath(x, y, hole.getX(), hole.getY());
            if (path!=null)
            {
                return new TWThought(TWAction.MOVE, path.getStep(0).getDirection());
            }
        }
        return null;
    }
    protected TWThought think() {
        TWThought thought = thinkHelper();
        if (thought == null) {
            TWPath notSoRandomPath = getNotSoRandomPath();
            if(notSoRandomPath != null)
                return new TWThought(TWAction.MOVE, notSoRandomPath.getStep(0).getDirection());
            return new TWThought(TWAction.MOVE, getBoundedDirection());
        }
        return thought;
    }
    TWDirection getBoundedDirection() {
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
        //You can do:
        //move(thought.getDirection())
        //pickUpTile(Tile)
        //putTileInHole(Hole)
        //refuel()
        System.out.println("Current Position: (" + x + ", " + y + ")");
        try {
            ObjectGrid2D objectGrid2D = this.getEnvironment().getObjectGrid();
            TWEntity e = (TWEntity) objectGrid2D.get(x, y);

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
        return "Simple Agent ONE";
    }
}