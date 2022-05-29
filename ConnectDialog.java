import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class ConnectDialog extends Dialog {
    Button connectButton = new Button("Connect");
    Button loginButton = new Button("Login");

    TextField textFieldIP = new TextField("127.0.0.1", 16);
    TextField textFieldUserName = new TextField("", 16);
    Label hostLabel = new Label("Host IP :");
    Label usernameLabel = new Label("Username :");

    TankGame tankGame;

    ConnectDialog(TankGame tankGame) {
        super(tankGame, true);
        this.tankGame = tankGame;
        this.setLayout(new FlowLayout());
        this.add(hostLabel);
        this.add(textFieldIP);
        this.add(usernameLabel);
        this.add(textFieldUserName);
        this.add(connectButton);
        this.add(loginButton);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                System.exit(0);
            }
        });

        connectButton.addActionListener(e -> connect(textFieldIP.getText().trim()));
        loginButton.addActionListener(e -> login(textFieldUserName.getText().trim()));


        this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });
        showConnect();
    }

    void showConnect() {
        hostLabel.setVisible(true);
        textFieldIP.setVisible(true);
        connectButton.setVisible(true);
        usernameLabel.setVisible(false);
        textFieldUserName.setVisible(false);
        loginButton.setVisible(false);
    }

    void showLogin() {
        hostLabel.setVisible(false);
        textFieldIP.setVisible(false);
        connectButton.setVisible(false);

        usernameLabel.setVisible(true);
        textFieldUserName.setVisible(true);
        loginButton.setVisible(true);

    }

    private void connect(String IP) {
        // create and set random udp port
        tankGame.client.setUdpPort((int) (Math.random() * 10000));
        boolean connected = tankGame.client.connect(IP);

        if (!connected) {
            System.out.println("connection error");
            JOptionPane.showMessageDialog(null, "Network error");
            this.setVisible(true);
        } else {
            showLogin();
            this.setVisible(true);
        }
    }

    private void login(String username) {
        if (username.equals("")){
            return;
        }
        System.out.println("login: " + username);
        tankGame.user = new User(username);
        tankGame.client.send(MsgType.LOGIN_REQUEST,  new LoginRequest(tankGame.client.connectionID, username).Token());
        this.setVisible(false);
    }
}