import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

class Missile {
    int x, y;
    String tankID;
    int id;
    String gameID;
    String delimiter = "#d#";

    static final int WIDTH = 10;
    static final int HEIGHT = 10;
    private static int ID = 1;
    private static final int SPEED = 20;
    private boolean me;
    boolean live;
    Direction direction;

    void setMe(boolean b) {
        this.me = b;
    }

    Missile(String tankID, int x, int y, Direction direction, String gameID) {
        this.tankID = tankID;
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.id = ID++;
        this.gameID = gameID;
        this.live = true;
    }

    Missile(String token) {
        String[] fields = token.split(delimiter);
        this.id = Integer.parseInt(fields[0]);
        this.tankID = fields[1];
        this.x = Integer.parseInt(fields[2]);
        this.y = Integer.parseInt(fields[3]);
        this.direction = Direction.values()[Integer.parseInt(fields[4])];
        this.gameID = fields[5];
        this.live = true;
    }

    String Token() {
        return this.id + delimiter +
                this.tankID + delimiter +
                this.x + delimiter +
                this.y + delimiter +
                this.direction.ordinal() + delimiter +
                this.gameID;
    }

    void draw(Graphics g) {
        Color c = g.getColor();

        if (this.me) {
            g.setColor(Color.green);
        } else {
            g.setColor(Color.red);
        }

        g.fillOval(x, y, WIDTH, HEIGHT);
        g.setColor(c);
        move();
    }

    private void move() {
        switch (direction) {
            case U:
                y -= SPEED;
                break;
            case RU:
                x += SPEED;
                y -= SPEED;
                break;
            case R:
                x += SPEED;
                break;
            case RD:
                x += SPEED;
                y += SPEED;
                break;
            case D:
                y += SPEED;
                break;
            case LD:
                x -= SPEED;
                y += SPEED;
                break;
            case L:
                x -= SPEED;
                break;
            case LU:
                x -= SPEED;
                y -= SPEED;
                break;
            case STOP:
                break;
        }

        if (x <= 0 || y <= 0 || y + (HEIGHT / 2) >= WarPanel.GAME_HEIGHT || x + (WIDTH / 2) >= WarPanel.GAME_WIDTH) {
            this.live = false;
        }
    }

    private Rectangle getRect() {
        return new Rectangle(x, y, WIDTH, HEIGHT);
    }

    boolean hitTank(Tank tank) {
        if (this.live && this.getRect().intersects(tank.getRect()) && tank.isLive() && !this.me) {
            tank.setLive(false);
            return true;
        }

        return false;
    }

}