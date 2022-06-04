import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class GameRoomPanel extends Dialog {
    int userCount = 0;
    Button button = new Button("Start TankGame");
    JLabel status = new JLabel(String.valueOf(userCount));
    JLabel message = new JLabel("wait host to start tankGame");
    TankGame tankGame;

    GameRoomPanel(TankGame tankGame) {

        super(tankGame, false);
        this.tankGame = tankGame;

        this.setLayout(new FlowLayout());

        button.addActionListener(e -> tankGame.client.send(MsgType.GAME_START, new GameStart(tankGame.homePanel.currentGame).Token()));

        this.add(status);

        button.setVisible(false);
        message.setVisible(false);

        this.add(message);
        this.add(button);


        this.setPreferredSize(new Dimension(200, 150));
        this.pack();

        this.setLocationRelativeTo(null);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (tankGame.homePanel.currentGame.equals(tankGame.user.username)) {
                    tankGame.client.send(MsgType.GAME_CANCEL_ROOM, new GameCancelRoom(tankGame.user.username).Token());
                } else {
                    tankGame.client.send(MsgType.GAME_LEAVE_ROOM, new GameLeaveRoom(tankGame.user.username, tankGame.homePanel.currentGame).Token());
                }

                Close();
            }
        });


    }

    void setUser(int count) {
        userCount = count;
        status.setText(String.valueOf(count));
        button.setEnabled(userCount > 1);
    }

    public void Open(boolean owner) {

        if (owner) {
            button.setVisible(true);
            message.setVisible(false);
        } else {
            button.setVisible(false);
            message.setVisible(true);
        }

        this.setVisible(true);
    }

    void Close() {
        this.setVisible(false);
    }
}
