/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tileworld.agent;

import sim.field.grid.ObjectGrid2D;
import sim.util.Int2D;
import tileworld.environment.TWEntity;
import tileworld.environment.TWEnvironment;
import tileworld.environment.TWHole;
import tileworld.environment.TWTile;
import tileworld.exceptions.CellBlockedException;
import tileworld.planners.DefaultTWPlanner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
public class HannibalAgent extends TWAgent {

    public DefaultTWPlanner planner;

    public HannibalAgent(int xpos, int ypos, TWEnvironment env, double fuelLevel) {
        super(xpos,ypos,env,fuelLevel);
        planner = new DefaultTWPlanner();
    }

    protected TWThought think() {
        TWThought thought = planner.execute(this, "BottomLeft");
        getMessage(new Int2D(0, 0), OBJECT_HOLE);
        return thought;
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
                    super.refuel();
                    break;
                case WAIT:
                    System.out.println("jus 'angin 'round them obstacles people!");
                    break;
                default:
                    System.out.println("jus 'angin 'round them obstacles nigga!");
            }
        }
        catch (CellBlockedException ex) {
        }
    }

    @Override
    public String getName() {
        return "John \"Hannibal\" Smith";
    }
}