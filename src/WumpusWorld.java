import aima.core.environment.wumpusworld.WumpusPercept;
import aima.core.environment.wumpusworld.AgentPosition;

public class WumpusWorld {
    public final int STENCH = 1;
    public final int BREEEZE = 2;
    public final int GLITTER = 4;
    public final int WUMPUS = 8;
    public final int PIT = 16;

    private final int[][] rooms;
    private AgentPosition ap = new AgentPosition(0, 0, AgentPosition.Orientation.FACING_EAST);
    private WumpusPercept futurePercept;

    private boolean agentHasArrow = true;
    private boolean agentAlive = true;
    private boolean inGame = true;

    private final int wumpusX;
    private final int wumpusY;

    public WumpusWorld() {
        rooms = new int[4][4];

        this.wumpusX = 2;
        this.wumpusY = 0;

        rooms[0][1] += BREEEZE;
        rooms[0][2] += PIT;
        rooms[0][3] += BREEEZE;
        rooms[1][0] += STENCH;
        rooms[1][2] += BREEEZE;
        rooms[wumpusX][wumpusY] += WUMPUS;
        rooms[2][1] += GLITTER + BREEEZE + STENCH;
        rooms[2][2] += PIT;
        rooms[2][3] += BREEEZE;
        rooms[3][0] += STENCH;
        rooms[3][2] += BREEEZE;
        rooms[3][3] += PIT;

        futurePercept = new WumpusPercept();

        if (checkBreeze(ap.getX(), ap.getY()))
            futurePercept.setBreeze();

        if (checkStench(ap.getX(), ap.getY()))
            futurePercept.setStench();

        if (checkGlitter(ap.getX(), ap.getY()))
            futurePercept.setGlitter();
    }

    public void changeWorld(String action) {
        if (!agentAlive || !inGame)
            return;
        futurePercept = new WumpusPercept();
        AgentPosition.Orientation n = ap.getOrientation();
        switch (action) {
            case "TurnLeft":
                if (n == AgentPosition.Orientation.FACING_EAST)
                    n = AgentPosition.Orientation.FACING_NORTH;
                else if (n == AgentPosition.Orientation.FACING_NORTH)
                    n = AgentPosition.Orientation.FACING_WEST;
                else if (n == AgentPosition.Orientation.FACING_WEST)
                    n = AgentPosition.Orientation.FACING_SOUTH;
                else if (n == AgentPosition.Orientation.FACING_SOUTH)
                    n = AgentPosition.Orientation.FACING_EAST;
                ap = new AgentPosition(ap.getRoom(), n);
                break;
            case "TurnRight":
                if (n == AgentPosition.Orientation.FACING_NORTH)
                    n = AgentPosition.Orientation.FACING_EAST;
                else if (n == AgentPosition.Orientation.FACING_WEST)
                    n = AgentPosition.Orientation.FACING_NORTH;
                else if (n == AgentPosition.Orientation.FACING_SOUTH)
                    n = AgentPosition.Orientation.FACING_WEST;
                else if (n == AgentPosition.Orientation.FACING_EAST)
                    n = AgentPosition.Orientation.FACING_SOUTH;
                ap = new AgentPosition(ap.getRoom(), n);
                break;
            case "Forward":
                if (n == AgentPosition.Orientation.FACING_EAST) {
                    if (ap.getX() < rooms.length)
                        ap = new AgentPosition(ap.getX() + 1, ap.getY(), ap.getOrientation());
                    else
                        futurePercept.setBump();
                } else if (n == AgentPosition.Orientation.FACING_NORTH) {
                    if (ap.getY() < rooms.length)
                        ap = new AgentPosition(ap.getX(), ap.getY() + 1, ap.getOrientation());
                    else
                        futurePercept.setBump();
                } else if (n == AgentPosition.Orientation.FACING_WEST) {
                    if (ap.getX() > 1)
                        ap = new AgentPosition(ap.getX() - 1, ap.getY(), ap.getOrientation());
                    else
                        futurePercept.setBump();
                } else if (n == AgentPosition.Orientation.FACING_SOUTH) {
                    if (ap.getX() > 1)
                        ap = new AgentPosition(ap.getX(), ap.getY() - 1, ap.getOrientation());
                    else
                        futurePercept.setBump();
                }
                if (checkPIT(ap.getX(), ap.getY()) || checkWumpus(ap.getX(), ap.getY())) {
                    agentAlive = false;
                    inGame = false;
                }
                break;
            case "Shoot":
                if (!agentHasArrow)
                    throw new IllegalStateException("Agent don't have an arrow!");
                else {
                    agentHasArrow = false;
                    boolean killed = false;
                    if (n == AgentPosition.Orientation.FACING_NORTH) {
                        if (wumpusY > ap.getY() && wumpusX == ap.getX())
                            killed = true;
                    } else if (n == AgentPosition.Orientation.FACING_WEST) {
                        if (wumpusY == ap.getY() && wumpusX < ap.getX())
                            killed = true;
                    } else if (n == AgentPosition.Orientation.FACING_SOUTH) {
                        if (wumpusY < ap.getY() && wumpusX == ap.getX())
                            killed = true;
                    } else if (n == AgentPosition.Orientation.FACING_EAST) {
                        if (wumpusY == ap.getY() && wumpusX > ap.getX())
                            killed = true;
                    }
                    if (killed) {
                        futurePercept.setScream();
                    }
                }
                break;
            case "Climb":
                inGame = false;
                break;
            case "Grab":
                if (checkGlitter(ap.getX(), ap.getY())) {
                    rooms[ap.getX()][ap.getY()] -= GLITTER;
                } else {
                    throw new IllegalStateException("At this room there isn't any gold!");
                }
                break;
        }

        if (checkBreeze(ap.getX(), ap.getY()))
            futurePercept.setBreeze();

        if (checkStench(ap.getX(), ap.getY()))
            futurePercept.setStench();

        if (checkGlitter(ap.getX(), ap.getY()))
            futurePercept.setGlitter();
    }

    private boolean checkBreeze(int x, int y) {
        return (rooms[x][y] / BREEEZE) % 2 == 1;
    }

    private boolean checkStench(int x, int y) {
        return (rooms[x][y] / STENCH) % 2 == 1;
    }

    private boolean checkGlitter(int x, int y) {
        return (rooms[x][y] / GLITTER) % 2 == 1;
    }

    private boolean checkPIT(int x, int y) {
        return (rooms[x][y] / PIT) % 2 == 1;
    }

    private boolean checkWumpus(int x, int y) {
        return (rooms[x][y] / WUMPUS) % 2 == 1;
    }

    public WumpusPercept getPercept() {
        return futurePercept;
    }
}