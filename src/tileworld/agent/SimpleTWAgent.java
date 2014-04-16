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
import tileworld.planners.DefaultTWPlanner;
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

    public DefaultTWPlanner planner;

    public SimpleTWAgent(int xpos, int ypos, TWEnvironment env, double fuelLevel) {
        super(xpos,ypos,env,fuelLevel);
        planner = new DefaultTWPlanner();
    }

    protected TWThought think() {
        TWThought thought = planner.execute(this, "BottomLeft");
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
        return "Simple Agent 1";
    }
}