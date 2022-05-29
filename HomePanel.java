import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class HomePanel extends Panel {
    TankGame tankGame;

    public final java.util.List<User> users = new CopyOnWriteArrayList<>();
    public final List<Game> games = new CopyOnWriteArrayList<>();

    NewGamePanel newGamePanel = new NewGamePanel();
    WaitUsersPanel waitUsersPanel = new WaitUsersPanel();

    public HomePanel(TankGame tankGame) {
        this.tankGame = tankGame;
        initComponents();
    }

    String currentGame = "";

    void handleNewUser(User u) {
        System.out.println("new user message: " + u.username);
        addUser(u, true);
    }

    public void handleUserList(String token) {
        String delimiter = "%ddd%";
        String[] tokens = token.split(delimiter);

        for (int i = 1; i < tokens.length; i++) {
            User u = new User(tokens[i]);
            if (!u.username.equals(tankGame.user.username)) {
                addUser(u, false);
            }
        }

    }

    public void handleGameList(String token) {
        String delimiter = "%ddd%";
        String[] tokens = token.split(delimiter);

        for (int i = 1; i < tokens.length; i++) {
            System.out.println(tokens[i]);
            Game u = new Game(tokens[i]);
            addGame(u, false);
        }
    }

    public void handleChatMessage(ChatMessage ch) {
        printNewMessage(ch.username, ch.message);
    }

    public void handleNewGame(Game game) {
        addGame(game, false);
        System.out.println("new tankGame message received");
        if (game.owner.equals(tankGame.user.username)) {
            tankGame.homePanel.newGamePanel.Close();
            tankGame.client.send(MsgType.WAIT_USERS_JOIN, new WaitUsersJoin(tankGame.user.username, game.owner).Token());
        }
    }

    public void handleJoinGame(String username, String gameID) {
        int count = 0;
        for (Game g : games) {
            if (g.owner.equals(gameID)) {
                g.userCount++;
                count = g.userCount;
            }
        }

        updateGameList();

        if (username.equals(tankGame.user.username)) {
            waitUsersPanel.setUser(count);
            currentGame = gameID;
            waitUsersPanel.Open(gameID.equals(tankGame.user.username));
        } else {
            waitUsersPanel.setUser(count);
        }
    }

    public void handleWaitUsersLeave(String username, String gameID) {
        System.out.println("waitusersleave: " + username + " " + gameID);
        int count = 0;
        for (Game g : games) {
            if (g.owner.equals(gameID)) {
                g.userCount--;
                count = g.userCount;
            }
        }
        updateGameList();

        if (username.equals(tankGame.user.username)) {
            waitUsersPanel.Close();
            currentGame = "";
            System.out.println("leave " + username + " " + gameID);
        } else if (currentGame.equals(gameID)) {
            waitUsersPanel.setUser(count);
        }
    }

    public void handleWaitUsersCancel(String gameID) {
        games.removeIf(game -> game.owner.equals(gameID));
        updateGameList();
        printNewMessage("Server: ", gameID + " cancelled game!");

        if (tankGame.user.username.equals(gameID)) {
            waitUsersPanel.Close();
            currentGame = "";
        }

        System.out.println("cancelled: " + gameID);
    }

    public void handleGameLeave(GameLeave gl) {
        System.out.println("end " + gl.username + " " + gl.gameOwner);

        for (Game g : games) {
            if (g.owner.equals(gl.gameOwner)) {
                g.userCount--;
            }
        }

        updateGameList();

        if (gl.username.equals(tankGame.user.username)) {
            tankGame.warPanel.dispose();
            currentGame = "";
            tankGame.setVisible(true);
        }
    }

    public void handleGameEnd(GameEnd end) {
        games.removeIf(game -> game.owner.equals(end.gameID));
        updateGameList();

        System.out.println("end " + end.gameID);

        //TODO UPDATE
        if (tankGame.homePanel.currentGame.equals(end.gameID)) {
            tankGame.warPanel.dispose();
            currentGame = "";
            tankGame.setVisible(true);
        }

        // TODO MAYBE SCORE TABLE
    }

    public void addGame(Game r, boolean printChat) {
        games.add(r);
        updateGameList();
        if (printChat) {
            printNewMessage("Server: ", r.owner + " created a room");
        }
    }

    void updateGameList() {
        gameListPanel.removeAll();
        for (Game g : games) {
            gameListPanel.add(new RoomListElement(g.owner, g.userCount, g.started));
        }
        gameListPanel.updateUI();
    }

    public void addUser(User u, boolean printChat) {
        users.add(u);
        userList.addElement(u.username);
        if (printChat) {
            printNewMessage("Server: ", u.username + " joined to chat!");
        }
    }

    public void removeUser(String username) {
        users.removeIf(u -> u.username.equals(username));
        userList.removeElement(username);
        printNewMessage("Server: ", username + " leaved!");
    }

    public void printNewMessage(String userName, String message) {
        chatLayout.append(userName + ": " + message + "\n");
    }

    private void initComponents() {
        JButton buttonCreateGame = new JButton();
        JScrollPane jScrollPane1 = new JScrollPane();
        chatLayout = new javax.swing.JTextArea();
        JList<String> userListPanel = new JList<>();
        chatMessage = new javax.swing.JTextField();
        sendButton = new javax.swing.JButton();
        JScrollPane gameListScroll = new JScrollPane();
        gameListPanel = new javax.swing.JPanel();

        chatMessage.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendButton.doClick();
                }
            }
        });

        sendButton.addActionListener(this::handleSendButton);

        setMaximumSize(new java.awt.Dimension(748, 528));
        buttonCreateGame.setText("create room");

        buttonCreateGame.addActionListener(e -> newGamePanel.Open());


        chatLayout.setEnabled(false);
        chatLayout.setColumns(20);
        chatLayout.setRows(5);
        jScrollPane1.setViewportView(chatLayout);
        userListPanel.setModel(userList);

        sendButton.setText("send");
        gameListPanel.setLayout(new BoxLayout(gameListPanel, BoxLayout.Y_AXIS));

        gameListScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        gameListScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        gameListScroll.setViewportView(gameListPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addComponent(buttonCreateGame, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))).addComponent(gameListScroll, GroupLayout.DEFAULT_SIZE, 170, GroupLayout.DEFAULT_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 407, javax.swing.GroupLayout.PREFERRED_SIZE).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addComponent(chatMessage).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(sendButton, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(userListPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(16, 16, 16).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(userListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false).addGroup(layout.createSequentialGroup().addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(buttonCreateGame).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(gameListScroll, javax.swing.GroupLayout.PREFERRED_SIZE, 415, javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(layout.createSequentialGroup().addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 460, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(sendButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(chatMessage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))).addGap(0, 0, Short.MAX_VALUE))).addContainerGap()));
    }

    private javax.swing.JTextField chatMessage;
    private javax.swing.JPanel gameListPanel;
    private javax.swing.JTextArea chatLayout;
    private javax.swing.JButton sendButton;
    public DefaultListModel<String> userList = new DefaultListModel<>();

    public void handleStartGame(String gameID) {
        System.out.println("game is started");
        printNewMessage("Server: ", "game " + gameID + " is started! ");
        if (currentGame.equals(gameID)) {
            System.out.println("game is starting");
            waitUsersPanel.Close();
            tankGame.warPanel = new WarPanel(tankGame);
            tankGame.warPanel.start();
            tankGame.setVisible(false);
        }
    }

    public void handleExit(Exit e) {
        if (!e.username.equals(tankGame.user.username)) {
            removeUser(e.username);
        }
        //TODO not necessary

        /*
        else{
            if (tankGame.homePanel.currentGame.equals(tankGame.user.username)){
                tankGame.client.send(MsgType.CANCEL_GAME, new WaitUsersCancel(tankGame.user.username).Token());
            }else{
                tankGame.client.send(MsgType.LEAVE_GAME, new WaitUsersLeave(tankGame.user.username).Token());
            }
        }

         */
    }

    void handleSendButton(ActionEvent e) {
        String txt = chatMessage.getText();
        if (!Objects.equals(txt, "")) {
            tankGame.client.send(MsgType.CHAT_NEW_MESSAGE, new ChatMessage(tankGame.user.username, txt).Token());
            chatMessage.setText("");
        }
    }

    public class RoomListElement extends JPanel {

        String owner;

        public RoomListElement(String owner, int currentPlayerCount, boolean started) {
            joinGameButton = new JButton();
            gameStatusLabel = new JLabel();

            this.owner = owner;
            joinGameButton.setText("join");

            joinGameButton.addActionListener(e -> {
                tankGame.client.send(MsgType.WAIT_USERS_JOIN, new WaitUsersJoin(tankGame.user.username, owner).Token());
            });

            setStatus(currentPlayerCount);
            setStarted(started);


            this.add(gameStatusLabel);
            this.add(joinGameButton);

        }

        void setStarted(boolean b) {
            joinGameButton.setEnabled(!b);
            if (b) {
                joinGameButton.setText("started");
            }
        }

        public void setStatus(int current) {
            gameStatusLabel.setText(String.valueOf(current));
        }

        private final JLabel gameStatusLabel;
        private final JButton joinGameButton;
        public int gameID;
    }

    class NewGamePanel extends Dialog {
        Button start = new Button("Setup TankGame");
        JPanel panel = new JPanel();
        TextField tour = new TextField(5);

        NewGamePanel() {
            super(tankGame, true);

            this.setLayout(new FlowLayout());
            this.add(new Label("Tour Count :"));
            this.add(tour);
            this.add(start);
            start.addActionListener(e -> handleSetupGame(tour.getText().trim()));
            this.setSize(60, 80);

            this.setResizable(false);
            this.add(panel);
            this.pack();
            this.setLocationRelativeTo(null);
            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    NewGamePanel.this.dispose();
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
                tankGame.client.send(MsgType.NEW_GAME, new Game(tankGame.user.username, count, 0, false).Token());
            }
        }

        void Open() {
            //frame.setVisible(true);
            this.setVisible(true);

        }

        void Close() {
            // frame.setVisible(false);
            this.dispose();
        }
    }

    class WaitUsersPanel extends Dialog {
        int userCount = 0;
        JDialog frame = new JDialog(tankGame, "WaitUsers", false);
        Button button = new Button("Start TankGame");
        JLabel status = new JLabel(String.valueOf(userCount));
        JLabel message = new JLabel("wait host to start tankGame");
        JPanel panel = new JPanel();

        WaitUsersPanel() {
            super(tankGame, true);

            this.setLayout(new FlowLayout());

            this.setResizable(false);
            this.add(panel);
            this.setLocationRelativeTo(null);

            button.addActionListener(e -> tankGame.client.send(MsgType.START_GAME, new GameStart(currentGame).Token()));
            this.add(status);
            button.setVisible(false);
            message.setVisible(false);

            this.add(message);
            this.add(button);

            this.setPreferredSize(new Dimension(200, 150));

            this.setResizable(false);
            this.setLocationRelativeTo(null);

            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.out.println("closing");
                    System.out.println(currentGame + " " + tankGame.user.username);
                    if (currentGame.equals(tankGame.user.username)) {
                        tankGame.client.send(MsgType.WAIT_USERS_CANCEL, new WaitUsersCancel(tankGame.user.username).Token());
                    } else {
                        tankGame.client.send(MsgType.WAIT_USERS_LEAVE, new WaitUsersLeave(tankGame.user.username, currentGame).Token());
                    }
                }
            });

            this.pack();

            this.setAlwaysOnTop(true);
        }

        void setUser(int count) {
            userCount = count;
            status.setText(String.valueOf(count));
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
            //this.dispose();

            this.setVisible(false);
        }
    }
}
