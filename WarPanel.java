import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.*;

public class WarPanel extends JFrame {
    static final int GAME_WIDTH = 800;
    static final int GAME_HEIGHT = 600;

    String gameID;
    TankGame tankGame;

    int tourCount;
    int deadCount;

    WarPanel(TankGame tankGame) {
        this.tankGame = tankGame;
        this.gameID = tankGame.homePanel.currentGame;
        deadCount = 0;
    }

    List<Missile> missiles = new CopyOnWriteArrayList<>();
    List<Explode> explodes = new CopyOnWriteArrayList<>();
    List<Tank> enemyTanks = new CopyOnWriteArrayList<>();
    private Image offScreenImage = null;
    Tank tank;

    public void start() {
        int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        this.setLocation((screenWidth - GAME_WIDTH) / 2, (screenHeight - GAME_HEIGHT) / 2);
        this.setSize(GAME_WIDTH, GAME_HEIGHT);
        this.setResizable(false);
        this.setTitle("TankWar");
        this.setVisible(true);
        this.addKeyListener(new KeyMonitor());
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                tankGame.client.send(MsgType.GAME_QUIT, new GameQuit(gameID, tankGame.user.username).Token());
                WarPanel.this.dispose();
                tankGame.homePanel.setVisible(true);
            }
        });

        this.tank = new Tank(tankGame.user.username, getRandomX(), getRandomY(), Direction.STOP, gameID);
        this.tank.setMe(true);

        tankGame.client.send(MsgType.TANK_NEW, tank.Token());

        new Thread(new PaintThread()).start();
    }

    public void paint(Graphics g) {
        if (offScreenImage == null) {
            offScreenImage = this.createImage(GAME_WIDTH, GAME_HEIGHT);
        }

        Graphics gOffScreen = offScreenImage.getGraphics();
        Color color = gOffScreen.getColor();
        gOffScreen.setColor(Color.white);
        gOffScreen.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        super.paint(gOffScreen);

        gOffScreen.setColor(Color.black);

        String status;
        if (deadCount < tourCount) {
            status = "Live";
        } else {
            status = "Loser";
        }

        gOffScreen.drawString("Status: " + status, 10, 50);
        gOffScreen.drawString("Count: " + deadCount + "/" + tourCount, 10, 90);
        gOffScreen.drawString("Enemies: " + (enemyTanks.size()), 10, 70);

        if (tank.isLive()) {
            tank.draw(gOffScreen);
        }

        for (Tank t : enemyTanks) {
            if (t.isLive()) {
                t.draw(gOffScreen);
            } else {
                enemyTanks.remove(t);
            }
        }

        for (Missile missile : missiles) {
            if (missile.hitTank(tank)) {
                missile.live = false;
                explodes.add(new Explode(tank.tankX, tank.tankY));
                tankGame.client.send(MsgType.TANK_DEAD, new TankDead(tank.id, gameID).Token());
                tankGame.client.send(MsgType.MISSILE_DEAD, new MissileDead(missile.tankID, missile.id, gameID).Token());

                deadCount++;

                if (deadCount < tourCount) {
                    this.tank = new Tank(tankGame.user.username, getRandomX(), getRandomY(), Direction.STOP, gameID);
                    this.tank.setMe(true);
                    tankGame.client.send(MsgType.TANK_NEW, this.tank.Token());
                } else {
                    tankGame.client.send(MsgType.GAME_LOSER, new GameLoser(gameID, tankGame.user.username).Token());
                }
            }

            if (missile.live) {
                missile.draw(gOffScreen);
            } else {
                missiles.remove(missile);
            }
        }


        for (Explode e : explodes) {
            if (e.getLive()) {
                e.draw(gOffScreen);
            } else {
                explodes.remove(e);
            }
        }

        gOffScreen.setColor(color);
        g.drawImage(offScreenImage, 0, 0, null);
    }

    void handleTankMove(Tank tankMove) {
        for (Tank tank : enemyTanks) {
            if (tank.id.equals(tankMove.id)) {
                tank.tankX = tankMove.tankX;
                tank.tankY = tankMove.tankY;
                tank.direction = tankMove.direction;
                tank.barrelDirection = tankMove.barrelDirection;

                break;
            }
        }
    }

    void handleNewTank(Tank newTank) {
        if (!tankGame.user.username.equals(newTank.id)) {
            enemyTanks.add(newTank);
        }
    }

    public void handleNewMissile(Missile missile) {
        missile.setMe(missile.tankID.equals(tank.id));
        missiles.add(missile);
    }

    public void handleTankDead(TankDead td) {
        for (Tank t : enemyTanks) {
            if (t.id.equals(td.tankID)) {
                t.setLive(false);
                explodes.add(new Explode(t.tankX, t.tankY));
                break;
            }
        }
    }

    int getRandomX() {
        return (int) ((Math.random() * (765 - 10)) + 10);
    }

    int getRandomY() {
        return (int) ((Math.random() * (570 - 10)) + 10);
    }

    public void handleMissileDead(MissileDead md) {
        for (Missile missile : missiles) {
            if (missile.tankID.equals(md.tankID) && missile.id == md.missileID) {
                missile.live = false;
                explodes.add(new Explode(missile.x, missile.y));
                break;
            }
        }
    }

    private class PaintThread implements Runnable {
        public void run() {
            while (true) {
                repaint();
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    private class KeyMonitor extends KeyAdapter {
        public void keyReleased(KeyEvent keyEvent) {
            if (keyEvent.getKeyCode() == KeyEvent.VK_J) {
                Missile m = tank.fire();
                if (m != null) {
                    tankGame.client.send(MsgType.MISSILE_NEW, m.Token());
                }
            } else {
                String moveToken = tank.keyReleased(keyEvent);
                if (!moveToken.equals("")) {
                    tankGame.client.send(MsgType.TANK_MOVE, moveToken);
                }
            }
        }

        public void keyPressed(KeyEvent keyEvent) {
            String moveToken = tank.keyPressed(keyEvent);
            if (!moveToken.equals("")) {
                tankGame.client.send(MsgType.TANK_MOVE, moveToken);
            }
        }
    }
}