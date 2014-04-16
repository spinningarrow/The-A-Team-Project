/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tileworld.agent;

import sim.display.GUIState;
import sim.field.grid.ObjectGrid2D;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.portrayal.Portrayal;
import sim.util.Int2D;
import tileworld.Parameters;
import tileworld.environment.*;
import tileworld.exceptions.CellBlockedException;
import tileworld.planners.AstarPathGenerator;
import tileworld.planners.DefaultTWPlanner;
import tileworld.planners.TWPath;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static tileworld.environment.TWDirection.W;
import static tileworld.environment.TWDirection.values;

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
public class FacemanAgent extends TWAgent {

    public DefaultTWPlanner planner;

    public FacemanAgent(int xpos, int ypos, TWEnvironment env, double fuelLevel) {
        super(xpos,ypos,env,fuelLevel);
        planner = new DefaultTWPlanner();
    }
    protected TWThought think() {
        TWThought thought = planner.execute(this, "TopRight");
        return thought;
    }

    public List<TWAgentPercept> getMessage(){
        List<TWAgentPercept> list = new ArrayList<TWAgentPercept>();
        TWAgentWorkingMemory memory = this.getMemory();

        list.add(new TWAgentPercept(memory.getNearbyHole(this.x,this.y,5),500));
        list.add(new TWAgentPercept(memory.getNearbyTile(this.x,this.y,5),500));
        list.add(new TWAgentPercept(memory.getClosestObjectInSensorRange(TWTile.class),500));

        return list;
    }

    @Override
    protected void act(TWThought thought) {
        System.out.println("Simple2 Current Position: (" + x + ", " + y + ")");
        try {
            ObjectGrid2D objectGrid2D = this.getEnvironment().getObjectGrid();
            TWEntity e = (TWEntity) objectGrid2D.get(this.getX(),this.getY());
            switch (thought.getAction())
            {
                case MOVE:
                    move(thought.getDirection());
                    break;
                case PICKUP:
                    pickUpTile((TWTile)e);
                    System.out.println("Simple2 Tiles: "+this.carriedTiles.size());
                    break;
                case PUTDOWN:
                    putTileInHole((TWHole)e);
                    System.out.println("Simple2 Remaining Tiles: "+this.carriedTiles.size());
                    break;
                case REFUEL:
                    super.refuel();
                    break;
                case WAIT:
                    System.out.println("Simple2 jus 'angin 'round them obstacles people!");
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
        return "Templeton \"Face(man)\" Peck";
    }

    public static Portrayal getPortrayal() {
        //red filled box.
        return new TWAgentPortrayal(Color.RED, Parameters.defaultSensorRange) {

            @Override
            public Inspector getInspector(LocationWrapper wrapper, GUIState state) {
                // make the inspector
                return new AgentInspector(super.getInspector(wrapper, state), wrapper, state);
            }
        };
    }
}