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
import tileworld.Parameters;
import tileworld.environment.*;
import tileworld.exceptions.CellBlockedException;
import tileworld.planners.DefaultTWPlanner;

import java.awt.*;

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

    @Override
    protected void act(TWThought thought) {
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
                    break;
                case PUTDOWN:
                    putTileInHole((TWHole)e);
                    break;
                case REFUEL:
                    super.refuel();
                    break;
                case WAIT:
                    System.out.println(getName() + ": waiting");
                    break;
                default:
                    System.out.println("Something is wrong...");
            }
        }
        catch (CellBlockedException ex) {
        }
    }

    @Override
    public String getName() {
        return "Faceman";
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