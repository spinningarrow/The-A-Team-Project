/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tileworld.agent;

import sim.field.grid.ObjectGrid2D;
import tileworld.environment.*;
import tileworld.exceptions.CellBlockedException;
import tileworld.planners.AstarPathGenerator;
import tileworld.planners.TWPath;
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

    protected TWThought think() {
//        getMemory().getClosestObjectInSensorRange(Tile.class);
        //DefaultTWPlanner plan = new DefaultTWPlanner();
        //plan.generatePlan();
        AstarPathGenerator astar = new AstarPathGenerator(getEnvironment(),this,30);
        TWPath path = astar.findPath(x, y, 15, 15);
        System.out.println("Simple Score: " + this.score);
        if(path != null)
            return new TWThought(TWAction.MOVE,path.getStep(0).getDirection());
        else return new TWThought(TWAction.MOVE,getRandomDirection());
    }

    @Override
    protected void act(TWThought thought) {

        //You can do:
        //move(thought.getDirection())
        //pickUpTile(Tile)
        //putTileInHole(Hole)
        //refuel()

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
