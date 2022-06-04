import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class TankGame extends JFrame {

    static final int GAME_WIDTH = 800;
    static final int GAME_HEIGHT = 600;

    User user;
    Client client;

    ConnectDialog connectDialog = new ConnectDialog(this);
    HomePanel homePanel = new HomePanel(this);

    WarPanel warPanel;


    TankGame() {
        client = new Client(this);
        initComponents();
    }

    void handleLoginResponse(boolean resp) {
        if (!resp) {
            JOptionPane.showMessageDialog(null, "username exists");
            System.out.println("username exists");
            connectDialog.setVisible(true);
        } else {
            System.out.println("connected!");
            setContentPane(homePanel);
            client.send(MsgType.NEW_USER, user.Token());
        }
    }

    private void initComponents() {
        int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        this.setLocation((screenWidth - GAME_WIDTH) / 2, (screenHeight - GAME_HEIGHT) / 2);
        this.setSize(GAME_WIDTH, GAME_HEIGHT);
        this.setResizable(false);
        this.setTitle("Tank TankGame");

        this.setVisible(true);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("closing event from home panel ");
                client.send(MsgType.CLOSE_APP, new CloseApp(client.connectionID, user.username).Token());
                System.exit(0);
            }
        });

        this.connectDialog.setVisible(true);
    }

    public static void main(String[] args) {

        /* TODO
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TankGame();
            }
        });

         */
        new TankGame();
    }

}
