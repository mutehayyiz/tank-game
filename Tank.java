import java.awt.*;
import java.awt.event.*;


class Tank {
    int tankX;
    int tankY;
    String id;
    private boolean buttonUP = false;
    private boolean buttonDown = false;
    private boolean buttonLeft = false;
    private boolean buttonRight = false;
    private boolean isLive = true;
    private boolean me;

    private static final int TANK_WIDTH = 35;
    private static final int TANK_HEIGHT = 35;
    private static final int SPEED = 15;
    private static final int UP_LIMIT = 16;
    Direction direction;
    Direction barrelDirection = Direction.D;

    boolean isMe() {
        return me;
    }

    void setMe(boolean me) {
        this.me = me;
    }

    boolean isLive() {
        return isLive;
    }

    void setLive(boolean live) {
        this.isLive = live;
    }

    private static Toolkit tk = Toolkit.getDefaultToolkit();
    private static Image[] images = null;

    static {
        images = new Image[]{
                tk.getImage(Tank.class.getResource("Images/tankU.gif")),
                tk.getImage(Tank.class.getResource("Images/tankRU.gif")),
                tk.getImage(Tank.class.getResource("Images/tankR.gif")),
                tk.getImage(Tank.class.getResource("Images/tankRD.gif")),
                tk.getImage(Tank.class.getResource("Images/tankD.gif")),
                tk.getImage(Tank.class.getResource("Images/tankLD.gif")),
                tk.getImage(Tank.class.getResource("Images/tankL.gif")),
                tk.getImage(Tank.class.getResource("Images/tankLU.gif")),
        };
    }

    int getInt(Direction d) {
        int ret = 0;
        switch (d) {
            case U:
                ret = 0;
                break;
            case RU:
                ret = 1;
                break;
            case R:
                ret = 2;
                break;
            case RD:
                ret = 3;
                break;
            case D:
                ret = 4;
                break;
            case LD:
                ret = 5;
                break;
            case L:
                ret = 6;
                break;
            case LU:
                ret = 7;
                break;
            default:
        }

        return ret;
    }

    private Tank(int x, int y, boolean me) {
        this.tankX = x;
        this.tankY = y;
        this.me = me;
    }

    Tank(int x, int y, boolean me, Direction direction) {
        this(x, y, me);
        this.direction = direction;
    }

    String delimiter = "#d#";

    Tank(String token) {
        String[] fields = token.split(delimiter);
        this.id = fields[0];
        this.tankX = Integer.parseInt(fields[1]);
        this.tankY = Integer.parseInt(fields[2]);
        this.direction = Direction.values()[Integer.parseInt(fields[3])];
        this.barrelDirection = Direction.values()[Integer.parseInt(fields[4])];
    }

    String Token() {
        return this.id + delimiter +
                this.tankX + delimiter +
                this.tankY + delimiter +
                this.direction.ordinal() + delimiter +
                this.barrelDirection.ordinal();
    }

    private boolean checkEdge(int x, int y) {
        return x >= 0 && x <= (WarPanel.GAME_WIDTH - TANK_WIDTH)
                && y >= UP_LIMIT && y <= WarPanel.GAME_HEIGHT - TANK_HEIGHT;
    }

    private void move() {
        int x, y;
        switch (direction) {

            case U:
                y = tankY - SPEED;
                if (checkEdge(tankX, y)) {
                    tankY = y;
                }
                break;
            case RU:
                x = tankX + SPEED;
                y = tankY - SPEED;
                if (checkEdge(x, y)) {
                    tankX = x;
                    tankY = y;
                }
                break;
            case R:
                x = tankX + SPEED;
                if (checkEdge(x, tankY)) {
                    tankX = x;
                }
                break;
            case RD:
                x = tankX + SPEED;
                y = tankY + SPEED;
                if (checkEdge(x, y)) {
                    tankX = x;
                    tankY = y;
                }
                break;
            case D:
                y = tankY + SPEED;
                if (checkEdge(tankX, y)) {
                    tankY = y;
                }
                break;
            case LD:
                x = tankX - SPEED;
                y = tankY + SPEED;
                if (checkEdge(x, y)) {
                    tankX = x;
                    tankY = y;
                }
                break;
            case L:
                x = tankX - SPEED;
                if (checkEdge(x, tankY)) {
                    tankX = x;
                }
                break;
            case LU:
                x = tankX - SPEED;
                y = tankY - SPEED;
                if (checkEdge(x, y)) {
                    tankX = x;
                    tankY = y;
                }
                break;
            case STOP:
                break;
        }
        if (this.direction != Direction.STOP) {
            this.barrelDirection = this.direction;
        }
    }

    private String location() {
        Direction oldDirection = this.direction;

        if (buttonUP && !buttonDown && !buttonLeft && !buttonRight) {
            direction = Direction.U;
        } else if (buttonUP && !buttonDown && !buttonLeft) {
            direction = Direction.RU;
        } else if (!buttonUP && !buttonDown && !buttonLeft && buttonRight) {
            direction = Direction.R;
        } else if (!buttonUP && buttonDown && !buttonLeft && buttonRight) {
            direction = Direction.RD;
        } else if (!buttonUP && buttonDown && !buttonLeft) {
            direction = Direction.D;
        } else if (!buttonUP && buttonDown && !buttonRight) {
            direction = Direction.LD;
        } else if (!buttonUP && !buttonDown && buttonLeft && !buttonRight) {
            direction = Direction.L;
        } else if (buttonUP && !buttonDown && !buttonRight) {
            direction = Direction.LU;
        } else if (!buttonUP && !buttonDown && !buttonLeft) {
            direction = Direction.STOP;
        }
        if (this.direction != oldDirection) {
            return Token();
        }

        return "";
    }

    void draw(Graphics graphics) {

        Color color = graphics.getColor();
        if (this.me) {
            graphics.setColor(Color.green);
        } else {
            graphics.setColor(Color.red);
        }

        graphics.drawImage(images[getInt(barrelDirection)], this.tankX, this.tankY, null);
        // graphics.fillRect(this.tankX, this.tankY, TANK_WIDTH, Tank.TANK_HEIGHT);
        graphics.drawString("ID : " + this.id, this.tankX, this.tankY - 10);
        graphics.setColor(Color.black);
/*
        int x1 = this.tankX + TANK_WIDTH / 2;
        int y1 = this.tankY + TANK_HEIGHT / 2;
        int x2 = countX2();
        int y2 = countY2();

        graphics.drawLine(x1, y1, x2, y2);
        graphics.setColor(color);

 */
        move();
    }

    private int countX2() {
        double x = 0;
        switch (barrelDirection) {
            case U, D -> x = this.tankX + TANK_WIDTH / 2.0;
            case RU, R, RD -> x = this.tankX + TANK_WIDTH * 1.5;
            case LD, L, LU -> x = this.tankX - TANK_WIDTH / 2.0;
            default -> {
            }
        }
        return (int) x;
    }

    private int countY2() {
        double y = 0;
        switch (barrelDirection) {
            case U, RU, LU -> y = this.tankY - TANK_HEIGHT / 2.0;
            case R -> y = this.tankY + TANK_HEIGHT / 2.0;
            case RD, D, LD -> y = this.tankY + TANK_HEIGHT * 1.5;
            case L -> y = this.tankY + TANK_WIDTH / 2.0;
            default -> {
            }
        }
        return (int) y;
    }


    public Missile fire() {
        if (!this.isLive) {
            return null;
        }

        int x = this.tankX + Tank.TANK_WIDTH / 2 - Missile.WIDTH / 2;
        int y = this.tankY + Tank.TANK_HEIGHT / 2 - Missile.HEIGHT / 2;

        return new Missile(id, x, y, this.barrelDirection);
    }

    String keyPressed(KeyEvent keyEvent) {
        int key = keyEvent.getKeyCode();
        switch (key) {
            case KeyEvent.VK_W -> buttonUP = true;
            case KeyEvent.VK_S -> buttonDown = true;
            case KeyEvent.VK_A -> buttonLeft = true;
            case KeyEvent.VK_D -> buttonRight = true;
        }
        return location();
    }

    String keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        switch (key) {
            case KeyEvent.VK_W -> buttonUP = false;
            case KeyEvent.VK_S -> buttonDown = false;
            case KeyEvent.VK_A -> buttonLeft = false;
            case KeyEvent.VK_D -> buttonRight = false;
        }
        return location();
    }

    Rectangle getRect() {
        return new Rectangle(tankX, tankY, TANK_WIDTH, TANK_HEIGHT);
    }
}