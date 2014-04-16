/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tileworld.planners;

import sim.field.grid.ObjectGrid2D;
import sim.util.Int2D;
import tileworld.Parameters;
import tileworld.agent.TWAction;
import tileworld.agent.TWAgent;
import tileworld.agent.TWAgentPercept;
import tileworld.agent.TWThought;
import tileworld.environment.TWDirection;
import tileworld.environment.TWEntity;
import tileworld.environment.TWHole;
import tileworld.environment.TWTile;

import java.util.ArrayList;
import java.util.List;

import static tileworld.environment.TWDirection.values;

/**
 * DefaultTWPlanner
 *
 * @author michaellees
 * Created: Apr 22, 2010
 *
 * Copyright michaellees 2010
 *
 * Here is the skeleton for your planner. Below are some points you may want to
 * consider.
 *
 * Description: This is a simple implementation of a Tileworld planner. A plan
 * consists of a series of directions for the agent to follow. Plans are made,
 * but then the environment changes, so new plans may be needed
 *
 * As an example, your planner could have 4 distinct behaviors:
 *
 * 1. Generate a random walk to locate a Tile (this is triggered when there is
 * no Tile observed in the agents memory
 *
 * 2. Generate a plan to a specified Tile (one which is nearby preferably,
 * nearby is defined by threshold - @see TWEntity)
 *
 * 3. Generate a random walk to locate a Hole (this is triggered when the agent
 * has (is carrying) a tile but doesn't have a hole in memory)
 *
 * 4. Generate a plan to a specified hole (triggered when agent has a tile,
 * looks for a hole in memory which is nearby)
 *
 * The default path generator might use an implementation of A* for each of the behaviors
 *
 */
public class DefaultTWPlanner implements TWPlanner {

    private boolean trackbackFlag = false;
    public static final int REFUEL_THRESHOLD_BUFFER = 20;
    public static final int ASTAR_MAX_SEARCH_DISTANCE = Parameters.xDimension * Parameters.yDimension;
    public Int2D currentGoal;

    public TWPath generatePlan() {
        
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean hasPlan() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void voidPlan() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Int2D getCurrentGoal() { return currentGoal; }

    public TWThought execute() { throw new UnsupportedOperationException("Not supported yet."); }

    protected List<TWEntity> getEntitiesInRange(TWAgent agent) {
        int sensorRange = Parameters.defaultSensorRange;
        List<TWEntity> entityList = new ArrayList<TWEntity>();
        ObjectGrid2D objectGrid = agent.getEnvironment().getObjectGrid();
        int topLeftX = (agent.getX() - sensorRange) <= 0 ? 0 : agent.getX() - sensorRange;
        int topLeftY = (agent.getY() - sensorRange) <= 0 ? 0 : agent.getY() - sensorRange;
        int bottomRightX = (agent.getX() + sensorRange) >= Parameters.xDimension ? Parameters.xDimension-1 : agent.getX() + sensorRange;
        int bottomRightY = (agent.getY() + sensorRange) >= Parameters.yDimension ? Parameters.yDimension-1 : agent.getY() + sensorRange;
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

    protected int getTrackbackThreshold (TWAgent agent)
    {
        AstarPathGenerator astar = new AstarPathGenerator(agent.getEnvironment(), agent, ASTAR_MAX_SEARCH_DISTANCE);
        TWPath path = astar.findPath(agent.getX(), agent.getY(), 0, 0);
        if(path == null)
            System.out.println("Path Null Fuel: " + agent.getFuelLevel());
        if(path != null) {
            System.out.println("Path steps to station: " + path.getpath().size() + " Fuel: " + agent.getFuelLevel());
        }
        return path != null && (path.getpath().size() <= agent.getFuelLevel()) ? path.getpath().size() : -1;
    }

    protected TWThought getThoughtForEntitiesRange(TWAgent agent, List<TWEntity> entityList) {
        AstarPathGenerator astar = new AstarPathGenerator(agent.getEnvironment(), agent, ASTAR_MAX_SEARCH_DISTANCE);

        if (entityList.size() > 0) {
            System.out.println("I FOUND A TILE OR A HOLE!");

            // Get the distance to the closest tile and hole within range (if any)
            double minHoleDistance = Double.MAX_VALUE;
            double minTileDistance = Double.MAX_VALUE;
            TWHole nearestHole = null;
            TWTile nearestTile = null;

            for (TWEntity entity : entityList) {

                double distance = entity.getDistanceTo(agent);

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
            if (nearestHole != null && (agent.getNumberOfCarriedTiles() == 3 || agent.hasTile() && minHoleDistance < minTileDistance)) {
                TWPath pathToNearestHole = astar.findPath(agent.getX(), agent.getY(), nearestHole.getX(), nearestHole.getY());

                if (pathToNearestHole != null) {

                    // If on the way to the fuel station, check if there's enough fuel to deviate from the path
                    if(trackbackFlag) {
                        TWPath pathNearestHoleToFuel = astar.findPath(nearestHole.getX(), nearestHole.getY(), 0, 0);
                        TWPath pathToFuelStation = astar.findPath(agent.getX(), agent.getY(), 0, 0);

                        // If the path to the nearest entity + path from entity to fuel station is too costly, go
                        // directly to the fuel station
                        if(pathNearestHoleToFuel != null && pathToNearestHole.getpath().size() + pathNearestHoleToFuel.getpath().size() > agent.getFuelLevel() - REFUEL_THRESHOLD_BUFFER/2) {
                            return new TWThought(TWAction.MOVE, pathToFuelStation.getStep(0).getDirection());
                        }
                    }
                    return new TWThought(TWAction.MOVE, pathToNearestHole.getStep(0).getDirection());
                }
            }

            // Otherwise if there is a tile nearby, pick it up
            else if (agent.getNumberOfCarriedTiles() < 3 && nearestTile != null) {
                TWPath pathToNearestTile = astar.findPath(agent.getX(), agent.getY(), nearestTile.getX(), nearestTile.getY());

                if (pathToNearestTile != null) {

                    // If on the way to the fuel station, check if there's enough fuel to deviate from the path
                    if(trackbackFlag) {

                        TWPath pathNearestTileToFuel = astar.findPath(nearestTile.getX(), nearestTile.getY(), 0, 0);
                        TWPath pathToFuelStation = astar.findPath(agent.getX(), agent.getY(), 0, 0);

                        // If the path to the nearest entity + path from entity to fuel station is too costly, go
                        // directly to the fuel station
                        if(pathNearestTileToFuel != null && pathToNearestTile.getpath().size() + pathNearestTileToFuel.getpath().size() > agent.getFuelLevel() - REFUEL_THRESHOLD_BUFFER/2) {
                            return new TWThought(TWAction.MOVE, pathToFuelStation.getStep(0).getDirection());
                        }
                    }
                    return new TWThought(TWAction.MOVE, pathToNearestTile.getStep(0).getDirection());
                }
            }
        }
        return null;
    }

    public void setCurrentGoal(int x, int y) {
        this.currentGoal = new Int2D(x, y);
    }

    protected TWPath getNotSoRandomPath(TWAgent agent, String section) {//(TWAgent agent, TWEnvironment environment) {
        //Temp: Chooses a random location and moves towards it
        AstarPathGenerator astar = new AstarPathGenerator(agent.getEnvironment(), agent, ASTAR_MAX_SEARCH_DISTANCE);
        if(getCurrentGoal() != null && !isGoalInSensorRange(agent))
        {
            TWPath recalculatedPath = astar.findPath(agent.getX(), agent.getY(), getCurrentGoal().getX(), getCurrentGoal().getY());
            if(recalculatedPath != null)
                return recalculatedPath;
        }
        while (true) {
            //Generate a random location
            Int2D target = agent.getEnvironment().generateFarRandomLocation(agent.getX(), agent.getY(), Parameters.defaultSensorRange * 2);
            if(section.equalsIgnoreCase("BottomLeft")) {
                while (target.getX() > target.getY())
                    target = agent.getEnvironment().generateFarRandomLocation(agent.getX(), agent.getY(), Parameters.defaultSensorRange + 1);
            }
            else {
                while (target.getX() < target.getY())
                    target = agent.getEnvironment().generateFarRandomLocation(agent.getX(), agent.getY(), Parameters.defaultSensorRange + 1);
            }
            TWPath pathFound = astar.findPath(agent.getX(), agent.getY(), target.getX(), target.getY());
            if (pathFound != null) {
                //Gets the last pathstep in TWPath, then return the x, y as a Int2D
                setCurrentGoal(target.getX(), target.getY());
                return pathFound;
            }
        }
    }

    public TWThought execute(TWAgent agent, String section) {
        TWThought thought = executeHelper(agent);
        System.out.println("First Agent Tiles: " + agent.getNumberOfCarriedTiles());
        if (thought == null) {
            TWPath notSoRandomPath = getNotSoRandomPath(agent, section);
            if(notSoRandomPath != null)
                return new TWThought(TWAction.MOVE, notSoRandomPath.getStep(0).getDirection());
            return new TWThought(TWAction.MOVE, getBoundedDirection(agent));
        }
        return thought;
    }

    protected TWDirection getRandomDirection(TWAgent agent){
        TWDirection randomDir = values()[agent.getEnvironment().random.nextInt(5)];
        switch (randomDir) {
            case N:
                if (agent.getY() == 0) return TWDirection.S;
                break;
            case E:
                if (agent.getX() == agent.getEnvironment().getxDimension()) return TWDirection.W;
                break;
            case W:
                if (agent.getX() == 0) return TWDirection.E;
                break;
            case S:
                if (agent.getY() == agent.getEnvironment().getyDimension()) return TWDirection.N;
                break;
        }
        return randomDir;
    }

    protected TWDirection getBoundedDirection(TWAgent agent) {
        TWDirection randomDirection;
        int newX = agent.getX() , newY = agent.getY();
        if(newX > newY)
            return TWDirection.W;
        else do
        {
            randomDirection = getRandomDirection(agent);
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

    public TWThought executeHelper(TWAgent agent) {
        List<TWEntity> entityList = this.getEntitiesInRange(agent);
//        List<TWAgentPercept> percepts = agent.getEnvironment().getAgent(agent).getMessage();

        // If I'm at the fuel station, refuel
        if (agent.getFuelLevel() < 1000 && agent.getX() == agent.getY() && agent.getX() == 0) {
            trackbackFlag = false;
            return new TWThought(TWAction.REFUEL, TWDirection.Z);
        }

        // When standing on a tile, pick it up
        if(agent.getNumberOfCarriedTiles() < 3 && agent.getEnvironment().getObjectGrid().get(agent.getX(), agent.getY()) instanceof TWTile){
            return new TWThought(TWAction.PICKUP, TWDirection.Z);
        }

        // When standing over a hole, drop a tile
        if(agent.hasTile() && agent.getEnvironment().getObjectGrid().get(agent.getX(), agent.getY()) instanceof TWHole){
            return new TWThought(TWAction.PUTDOWN, TWDirection.Z);
        }

        // Decide whether refueling is needed
        int threshold = getTrackbackThreshold(agent);
        if(threshold == -1 && agent.getX() != 0 && agent.getY() != 0) {
            return new TWThought(TWAction.WAIT, TWDirection.Z);
        }

        // If fuel is low or agent is already tracking back
        if (trackbackFlag || agent.getFuelLevel() <= threshold + REFUEL_THRESHOLD_BUFFER) {
            trackbackFlag = true;

            // Find a path to the fuel station from the current position
            AstarPathGenerator astar = new AstarPathGenerator(agent.getEnvironment(), agent, ASTAR_MAX_SEARCH_DISTANCE);
            TWPath path = astar.findPath(agent.getX(), agent.getY(), 0, 0);

            System.out.println("Tracking back->Simple Score: " + agent.getScore());

            if (path != null) {
                // Decide whether to deviate or not from the path if a tile or a hole is within range
                TWThought currentBestThought = getThoughtForEntitiesRange(agent, entityList);
                if(currentBestThought != null) {
                    return currentBestThought;
                }
                return new TWThought(TWAction.MOVE,path.getStep(0).getDirection());
            }
        }

        // Fuel level is normal and there are tiles or holes within range
        TWThought currentBestThought = getThoughtForEntitiesRange(agent, entityList);
        if(currentBestThought != null) {
            return currentBestThought;
        }

        // If agent sees nothing in its sensor range, retrieve tiles and holes seen nearby from the memory and generate
        // a path towards them; eventually they will come within range (handled above) or we'll keep moving towards them
        TWTile recentTile = agent.getMemory().getNearbyTile(agent.getX(), agent.getY(), 15);
        TWHole recentHole = agent.getMemory().getNearbyHole(agent.getX(), agent.getY(), 15);

        AstarPathGenerator astar = new AstarPathGenerator(agent.getEnvironment(), agent, ASTAR_MAX_SEARCH_DISTANCE);
        TWPath tilePath = null, holePath = null;

        // Find paths to a recently seen tile and hole (if any)
        if(recentTile != null && agent.getNumberOfCarriedTiles() < 3) {
            if (isObjectInSensorRange(agent, recentTile) && !getEntitiesInRange(agent).contains(recentTile)) {
                agent.getMemory().removeObject(recentTile);
            }
            else {
                tilePath = astar.findPath(agent.getX(), agent.getY(), recentTile.getX(), recentTile.getY());
            }
        }
        if(recentHole != null && agent.hasTile()) {
            if (isObjectInSensorRange(agent, recentHole) && !getEntitiesInRange(agent).contains(recentHole)) {
                agent.getMemory().removeObject(recentHole);
            }
            else {
                holePath = astar.findPath(agent.getX(), agent.getY(), recentHole.getX(), recentHole.getY());
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

    protected boolean isObjectInSensorRange(TWAgent agent, TWEntity entity)
    {
        return entity.getX()>=agent.getX()- Parameters.defaultSensorRange && entity.getX()<=agent.getX()+Parameters.defaultSensorRange
                && entity.getY()>=agent.getY()-Parameters.defaultSensorRange && entity.getY()<=agent.getY()+Parameters.defaultSensorRange;
    }

    protected boolean isGoalInSensorRange(TWAgent agent)
    {
        return getCurrentGoal().getX()>=agent.getX()- Parameters.defaultSensorRange && getCurrentGoal().getX()<=agent.getX()+Parameters.defaultSensorRange
                && getCurrentGoal().getY()>=agent.getY()-Parameters.defaultSensorRange && getCurrentGoal().getY()<=agent.getY()+Parameters.defaultSensorRange;
    }
}

