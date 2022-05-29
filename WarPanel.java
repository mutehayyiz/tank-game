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

    WarPanel(TankGame tankGame) {
        this.tankGame = tankGame;
        this.gameID = tankGame.homePanel.currentGame;
    }

    List<Missile> missiles = new CopyOnWriteArrayList<>();
    List<Explode> explodes = new CopyOnWriteArrayList<>();
    List<Tank> enemyTanks = new CopyOnWriteArrayList<>();

    private Image offScreenImage = null;

    Tank tank;

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
        gOffScreen.drawString("Kill: " + explodes.size(), 10, 50);
        gOffScreen.drawString("Enemies: " + (enemyTanks.size() - 1), 10, 70);
        gOffScreen.drawString("Missiles: " + missiles.size(), 10, 90);

        for (Tank t : enemyTanks) {
            if (!t.isLive()) {
                if (!t.isMe()) {
                    tankGame.warPanel.enemyTanks.remove(t);
                }
            } else {
                t.draw(gOffScreen);

            }


        }

        for (Missile missile : missiles) {
            if (missile.hitTank(tank)) {
                explodes.add(new Explode(tank.tankX, tank.tankY));
                tankGame.client.send(MsgType.TANK_DEAD, new TankDead(tank.id).Token());
                tankGame.client.send(MsgType.MISSILE_DEAD, new MissileDead(missile.tankID, missile.id).Token());
            }

            if (!missile.live) {
                missiles.remove(missile);
            } else {
                missile.draw(gOffScreen);
            }
        }

        for (Explode e : explodes) {
            if (!e.getLive()) {
                explodes.remove(e);
            } else {
                e.draw(gOffScreen);
            }
        }

        gOffScreen.setColor(color);
        g.drawImage(offScreenImage, 0, 0, null);
    }

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
                tankGame.client.send(MsgType.GAME_LEAVE, new GameLeave(tankGame.user.username, gameID).Token());
            }
        });

        this.tank = new Tank(0, 570, true, Direction.STOP);

        tank.id = tankGame.user.username;

        tankGame.client.send(MsgType.TANK_NEW, tank.Token());

        new Thread(new PaintThread()).start();
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
        boolean isExisted = false;

        for (Tank enemyTank : enemyTanks) {
            if (newTank.id.equals(enemyTank.id)) {
                isExisted = true;
                break;
            }
        }

        if (!isExisted) {
            //  warPanel.client.send(new TankNewMsg(warPanel.tank));
            if (tank.id.equals(newTank.id)) {
                newTank.setMe(true);
            }else{
                newTank.setMe(false);
            }

            enemyTanks.add(newTank);
        }
    }

    public void handleNewMissile(Missile missile) {
        if (missile.tankID.equals(tank.id)) {
            missile.setMe(true);
        }else{missile.setMe(false);}

        missiles.add(missile);
    }

    public void handleTankDead(TankDead td) {
        System.out.println("dead tank " + td.tankID);
        for (Tank tnk : enemyTanks) {
            if (tnk.id.equals(td.tankID)) {
                tnk.setLive(false);
                break;
            }
        }
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
        int sleep = 4;
        int counter = 0;

        public void run() {
            while (true) {
                repaint();
                try {
                    Thread.sleep(50);
                    /* TODO
                    if(tank.isLive()){
                        if(counter == sleep){
                            tank.setLive(true);
                            tank.tankX = 0;
                            tank.tankY = 570;
                            tank.direction = Direction.STOP;
                            netClient.send(new TankNewMsg(tank));
                            counter=60;
                        }else{
                            counter++;
                        }


                    }

                     */

                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    private class KeyMonitor extends KeyAdapter {
        public void keyReleased(KeyEvent keyEvent) {
            enemyTanks.forEach(t -> {
                if (tankGame.user.username.equals(t.id)) {
                    if (keyEvent.getKeyCode() == KeyEvent.VK_J) {
                        Missile m = t.fire();
                        if (m != null) {
                            tankGame.client.send(MsgType.MISSILE_NEW, m.Token());
                        }
                    } else {
                        String moveToken = t.keyReleased(keyEvent);
                        if (!moveToken.equals("")) {
                            tankGame.client.send(MsgType.TANK_MOVE, moveToken);
                        }
                    }
                }
            });
        }

        public void keyPressed(KeyEvent keyEvent) {
            enemyTanks.forEach(t -> {
                if (tankGame.user.username.equals(t.id)) {
                    String moveToken = t.keyPressed(keyEvent);
                    if (!moveToken.equals("")) {
                        tankGame.client.send(MsgType.TANK_MOVE, moveToken);
                    }
                }
            });
        }
    }
}