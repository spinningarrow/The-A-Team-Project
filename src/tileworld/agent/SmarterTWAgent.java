package tileworld.agent;

import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.portrayal.Portrayal;
import tileworld.Parameters;
import tileworld.environment.TWDirection;
import tileworld.environment.TWEnvironment;

import java.awt.*;

/**
 * Created by macbookpro on 8/4/14.
 */
public class SmarterTWAgent extends SimpleTWAgent
{
    public SmarterTWAgent(int xpos, int ypos, TWEnvironment env, double fuelLevel) {
        super(xpos,ypos,env,fuelLevel);
    }

    @Override
    TWDirection getBoundedDirection() {
        TWDirection randomDirection;
        int newX =x , newY = y;
        if(newX <= newY)
            return TWDirection.E;
        do
        {
            randomDirection = super.getRandomDirection();
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
        }while (newX < newY);
        return randomDirection;
    }

    protected TWThought think() {
        TWThought thought = super.thinkHelper();

        if (thought == null) {
            return new TWThought(TWAction.MOVE, getBoundedDirection());
        }

        return thought;
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
