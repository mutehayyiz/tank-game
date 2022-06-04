import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class HomePanel extends Panel {
    TankGame tankGame;
    String currentGame = "";
    public final java.util.List<User> users = new CopyOnWriteArrayList<>();
    public final List<Game> games = new CopyOnWriteArrayList<>();
    NewGamePanel newGamePanel;
    GameRoomPanel gameRoomPanel;

    public HomePanel(TankGame tankGame) {
        this.tankGame = tankGame;
        gameRoomPanel = new GameRoomPanel(tankGame);
        newGamePanel = new NewGamePanel(tankGame);

        initComponents();
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

        buttonCreateGame.addActionListener(e ->
                newGamePanel.Open());

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

    public void handleNewUser(User u) {
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

    public void handleGameNew(Game game) {
        addGame(game, true);
        if (game.owner.equals(tankGame.user.username)) {
            tankGame.homePanel.newGamePanel.Close();
            tankGame.client.send(MsgType.GAME_JOIN_ROOM, new GameJoinRoom(tankGame.user.username, game.owner).Token());
        }
    }

    public void handleGameJoinRoom(String username, String gameID) {
        int count = 0;
        for (Game g : games) {
            if (g.owner.equals(gameID)) {
                g.userCount++;
                count = g.userCount;
            }
        }

        updateGameList();

        if (username.equals(tankGame.user.username)) {
            gameRoomPanel.setUser(count);
            currentGame = gameID;
            gameRoomPanel.Open(gameID.equals(tankGame.user.username));
        } else {
            gameRoomPanel.setUser(count);
        }
    }

    public void handleGameLeaveRoom(String username, String gameID) {
        printNewMessage("server", username + " leaved game with id: " + gameID);
        int count = 0;
        for (Game g : games) {
            if (g.owner.equals(gameID)) {
                g.userCount--;
                count = g.userCount;
            }
        }
        updateGameList();

        if (username.equals(tankGame.user.username)) {
            gameRoomPanel.Close();
            currentGame = "";
        } else if (currentGame.equals(gameID)) {
            gameRoomPanel.setUser(count);
        }
    }

    public void handleGameCancelRoom(String gameID) {
        games.removeIf(game -> game.owner.equals(gameID));
        updateGameList();
        printNewMessage("Server: ", gameID + " cancelled game!");

        if (tankGame.user.username.equals(gameID) || tankGame.homePanel.currentGame.equals(gameID)) {
            gameRoomPanel.Close();
            currentGame = "";
        }
    }

    public void handleGameEnd(GameEnd end) {
        games.removeIf(game -> game.owner.equals(end.gameID));
        printNewMessage("Server", end.winner + " wins the game " + end.gameID);
        updateGameList();

        if (currentGame.equals(end.gameID)) {
            JOptionPane.showMessageDialog(tankGame.warPanel, end.winner + " wins the game");
            tankGame.warPanel.dispose();
            tankGame.setVisible(true);
            currentGame = "";
        }
    }

    public void handleGameStart(String gameID) {
        int tourCount = 0;
        for (Game g : games) {
            if (gameID.equals(g.owner)) {
                g.started = true;
                tourCount = g.tourCount;
                break;
            }
        }

        updateGameList();

        printNewMessage("Server: ", "game " + gameID + " is started! ");

        if (currentGame.equals(gameID)) {
            gameRoomPanel.Close();
            tankGame.warPanel = new WarPanel(tankGame);
            tankGame.warPanel.tourCount = tourCount;
            tankGame.warPanel.start();
            tankGame.setVisible(false);
        }
    }

    public void handleCloseApp(CloseApp ca) {
        if (!ca.username.equals(tankGame.user.username)) {
            removeUser(ca.username);
        }
    }

    void handleSendButton(ActionEvent e) {
        String txt = chatMessage.getText();
        if (!Objects.equals(txt, "")) {
            tankGame.client.send(MsgType.CHAT_NEW_MESSAGE, new ChatMessage(tankGame.user.username, txt).Token());
            chatMessage.setText("");
        }
    }

    public void handleGameQuit(GameQuit gq) {
        for (Game g : games) {
            if (g.owner.equals(gq.gameID)) {
                g.userCount--;
                break;
            }
        }

        updateGameList();

        if (currentGame.equals(gq.gameID)) {
            if (tankGame.user.username.equals(gq.username)) {
                tankGame.warPanel.dispose();
                tankGame.setVisible(true);
                currentGame = "";
            } else {
                tankGame.warPanel.enemyTanks.removeIf(t -> t.id.equals(gq.username));
            }
        }
    }

    void updateGameList() {
        gameListPanel.removeAll();
        for (Game g : games) {
            gameListPanel.add(new RoomListElement(g.owner, g.userCount, g.started));
        }

        gameListPanel.updateUI();
    }

    public void addGame(Game r, boolean printChat) {
        games.add(r);
        updateGameList();
        if (printChat) {
            printNewMessage("Server: ", r.owner + " created a room");
        }
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

    private javax.swing.JTextField chatMessage;
    private javax.swing.JPanel gameListPanel;
    private javax.swing.JTextArea chatLayout;
    private javax.swing.JButton sendButton;
    public DefaultListModel<String> userList = new DefaultListModel<>();

    public class RoomListElement extends JPanel {
        String owner;

        public RoomListElement(String owner, int currentPlayerCount, boolean started) {
            joinGameButton = new JButton();
            gameStatusLabel = new JLabel();

            this.owner = owner;
            joinGameButton.setText("join");

            joinGameButton.addActionListener(e -> tankGame.client.send(MsgType.GAME_JOIN_ROOM, new GameJoinRoom(tankGame.user.username, owner).Token()));

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
    }
}
