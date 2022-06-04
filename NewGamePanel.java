import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class NewGamePanel extends Dialog {
    Button start = new Button("Setup TankGame");
    TextField tour = new TextField(5);
    TankGame tankGame;

    NewGamePanel(TankGame tankGame) {
        super(tankGame, true);
        this.tankGame = tankGame;
        this.setLayout(new FlowLayout());
        this.add(new Label("Tour Count :"));
        this.add(tour);
        this.add(start);
        start.addActionListener(e -> handleSetupGame(tour.getText().trim()));
        this.setSize(60, 80);
        this.setResizable(false);
        this.pack();
        this.setLocationRelativeTo(null);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Close();
            }
        });
    }

    void handleSetupGame(String tourCount) {
        int count = 0;
        try {
            count = Integer.parseInt(tourCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (count == 0) {
            Open();
        } else {
            tankGame.client.send(MsgType.GAME_NEW, new Game(tankGame.user.username, count, 0, false).Token());
        }
    }

    void Open() {
        this.setVisible(true);
    }

    void Close() {
        // frame.setVisible(false);
        this.setVisible(false);
    }
}
