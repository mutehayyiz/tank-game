import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class TankGame extends JFrame {

    static final int GAME_WIDTH = 800;
    static final int GAME_HEIGHT = 600;

    ConnectDialog connectDialog = new ConnectDialog(this);
    User user;
    Client client;
    HomePanel homePanel = new HomePanel(this);

    WarPanel warPanel;


    TankGame() {
        client = new Client(this);
        initComponents();
    }
    void handleLoginResponse(boolean resp){
        if (!resp) {
            JOptionPane.showMessageDialog(null, "username exists");
            System.out.println("username exists");
            connectDialog.setVisible(true);
        } else {
            System.out.println("success!");

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
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setTitle("Tank TankGame");

        this.setVisible(true);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                client.send(MsgType.EXIT, new Exit(user.username).Token());
            }
        });

        this.connectDialog.setVisible(true);
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TankGame();
            }
        });
        /*
        g.client = new Client(g);
        g.initComponents();

        g.client.connect("127.0.0.1", 4242);

        g.client.send(new LoginRequestMsg(g, "ahmet"));

         */
    }

}
