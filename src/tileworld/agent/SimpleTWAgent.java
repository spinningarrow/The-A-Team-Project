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

    public SimpleTWAgent(int xpos, int ypos, TWEnvironment env, double fuelLevel) {
        super(xpos,ypos,env,fuelLevel);
    }

    List<TWEntity> getEntitiesInRange() {
        int sensorRange = Parameters.defaultSensorRange;
        List<TWEntity> entityList = new ArrayList<TWEntity>();

        ObjectGrid2D objectGrid = this.getEnvironment().getObjectGrid();

        int topLeftX = (this.getX() - sensorRange) < 0 ? 0 : this.getX() - sensorRange;
        int topLeftY = (this.getY() - sensorRange) < 0 ? 0 : this.getY() - sensorRange;
        int bottomRightX = (this.getX() + sensorRange) > Parameters.xDimension ? Parameters.xDimension : this.getX() + sensorRange;
        int bottomRightY = (this.getY() + sensorRange) > Parameters.yDimension ? Parameters.yDimension : this.getY() + sensorRange;

        for (int i = topLeftX; i < bottomRightX; i++) {
            for (int j = topLeftY; j < bottomRightY; j++) {
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

    protected TWThought think() {
        List<TWEntity> entityList = this.getEntitiesInRange();

        if (this.fuelLevel < 400) {
            // Refuel!
            AstarPathGenerator astar = new AstarPathGenerator(getEnvironment(), this, Parameters.xDimension * Parameters.yDimension);

            TWPath path = astar.findPath(this.x, this.y, 0, 0);

            System.out.println("Trackingback->Simple Score: " + this.score);

            if (path != null) {
                return new TWThought(TWAction.MOVE,path.getStep(0).getDirection());
            }
        }

        if (entityList.size() > 0) {

            // Find astar path to closest hole if we have carried tiles
            if (this.hasTile()) {
                double minDistance = Double.MAX_VALUE;
                TWHole nearestHole = null;

                for (TWEntity entity : entityList) {
                    if (entity instanceof TWHole) {
                        double distance = entity.getDistanceTo(this);
                        if (distance < minDistance) {
                            minDistance = distance;
                            nearestHole = (TWHole) entity;
                        }
                    }
                }

                if (nearestHole != null) {
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

        // Otherwise move randomly till you see something interesting
        return new TWThought(TWAction.MOVE, getRandomDirection());
    }

    @Override
    protected void act(TWThought thought) {

        //You can do:
        //move(thought.getDirection())
        //pickUpTile(Tile)
        //putTileInHole(Hole)
        //refuel()

        if (this.x == this.y && this.x == 0) {
            super.refuel();
        }

        try {
            this.move(thought.getDirection());
            ObjectGrid2D objectGrid2D = this.getEnvironment().getObjectGrid();
            TWEntity e = (TWEntity) objectGrid2D.get(x, y);
            if(e != null && (e instanceof TWTile))
            {
               pickUpTile((TWTile)e);
               System.out.println("Tiles: "+this.carriedTiles.size());
            }
            if(e != null && (e instanceof TWHole) && this.carriedTiles.size() > 0)
            {
                putTileInHole((TWHole)e);
                System.out.println("Remaining Tiles: "+this.carriedTiles.size());
            }

        }  catch (CellBlockedException ex) {

           // Cell is blocked, replan?
        }
    }


    private TWDirection getRandomDirection(){

        TWDirection randomDir = TWDirection.values()[this.getEnvironment().random.nextInt(5)];

        if(this.getX()>=this.getEnvironment().getxDimension() ){
            randomDir = TWDirection.W;
        }else if(this.getX()<=1 ){
            randomDir = TWDirection.E;
        }else if(this.getY()<=1 ){
            randomDir = TWDirection.S;
        }else if(this.getY()>=this.getEnvironment().getxDimension() ){
            randomDir = TWDirection.N;
        }

       return randomDir;

    }

    @Override
    public String getName() {
        return "Dumb Agent";
    }
}
