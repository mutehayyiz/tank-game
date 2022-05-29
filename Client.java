import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Client {
    int UDP_SERVER_PORT = 4243;
    int TCP_PORT = 4242;
    private int udpPort;
    private String IP;
    int connectionID;

    void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }

    private DatagramSocket datagramSocket;
    private Socket socket;

    TankGame tankGame;

    Client(TankGame tankGame) {
        this.tankGame = tankGame;
    }

    boolean connect(String IP) {
        this.IP = IP;

        setUdpPort((int) (Math.random() * 10000));

        try {
            //create udp socket
            datagramSocket = new DatagramSocket(udpPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        socket = null;
        try {
            //create tcp socket
            socket = new Socket(IP, TCP_PORT);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

            // send your udp socket port
            dataOutputStream.writeInt(this.udpPort);
            // get id from server
            this.connectionID = dataInputStream.readInt();

            dataOutputStream.flush();
            dataOutputStream.close();
            dataInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Connected with id: " + this.connectionID);

        new Thread(new UDPReceiveThread()).start();

        return true;
    }

    public void send(int type, String token) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            dataOutputStream.writeInt(type);
            sendText(dataOutputStream, token);

            dataOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        byte[] byteArray = byteArrayOutputStream.toByteArray();
        try {
            datagramSocket.send(new DatagramPacket(byteArray, byteArray.length, new InetSocketAddress(IP, UDP_SERVER_PORT)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendText(DataOutputStream out, String txt) throws IOException {
        byte[] by = txt.getBytes(StandardCharsets.UTF_8);
        out.writeInt(by.length);
        out.write(by);
    }

    String readToken(DataInputStream in) throws IOException {
        int length = in.readInt();
        byte[] array = new byte[length];
        in.read(array);
        return new String(array, StandardCharsets.UTF_8);
    }

    public void close() {
        try {
            datagramSocket.close();
            datagramSocket = null;
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class UDPReceiveThread implements Runnable {
        byte[] buffer = new byte[1024];

        public void run() {
            while (datagramSocket != null) {
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                try {
                    datagramSocket.receive(datagramPacket);
                    parse(datagramPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void parse(DatagramPacket datagramPacket) {
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(buffer, 0, datagramPacket.getLength()));
            try {
                int msgType = dataInputStream.readInt();
                switch (msgType) {
                    case MsgType.LOGIN_RESPONSE:
                        LoginResponse response = new LoginResponse(readToken(dataInputStream));
                        tankGame.handleLoginResponse(response.loggedIn);
                        break;
                    case MsgType.USER_LIST:
                        String token = readToken(dataInputStream);
                        tankGame.homePanel.handleUserList(token);
                        break;
                    case MsgType.GAME_LIST:
                        token = readToken(dataInputStream);
                        tankGame.homePanel.handleGameList(token);
                        break;
                    case MsgType.NEW_USER:
                        System.out.println("new user from server");
                        User u = new User(readToken(dataInputStream));
                        tankGame.homePanel.handleNewUser(u);
                        break;
                    case MsgType.CHAT_NEW_MESSAGE:
                        ChatMessage ch = new ChatMessage(readToken(dataInputStream));
                        tankGame.homePanel.handleChatMessage(ch);
                        break;
                    case MsgType.NEW_GAME:
                        Game game = new Game(readToken(dataInputStream));
                        tankGame.homePanel.handleNewGame(game);
                        break;
                    case MsgType.WAIT_USERS_JOIN:
                        System.out.println("join game");
                        WaitUsersJoin jg = new WaitUsersJoin(readToken(dataInputStream));
                        tankGame.homePanel.handleJoinGame(jg.username, jg.gameOwner);
                        break;
                    case MsgType.WAIT_USERS_CANCEL:
                        WaitUsersCancel cg = new WaitUsersCancel(readToken(dataInputStream));
                        System.out.println("cancel");

                        System.out.println("wait_users_cancel " + cg.gameOwner);
                        tankGame.homePanel.handleWaitUsersCancel(cg.gameOwner);
                        break;
                    case MsgType.WAIT_USERS_LEAVE:
                        WaitUsersLeave lg = new WaitUsersLeave(readToken(dataInputStream));
                        System.out.println("leave");
                        System.out.println("wait_users_leave " + lg.username + " " + lg.gameOwner);
                        tankGame.homePanel.handleWaitUsersLeave(lg.username, lg.gameOwner);
                        break;
                    case MsgType.START_GAME:
                        GameStart sg = new GameStart(readToken(dataInputStream));
                        tankGame.homePanel.handleStartGame(sg.gameOwner);
                        break;
                    case MsgType.TANK_NEW:
                        Tank tank = new Tank(readToken(dataInputStream));
                        tankGame.warPanel.handleNewTank(tank);
                        break;

                    case MsgType.TANK_MOVE:
                        Tank tankMove = new Tank(readToken(dataInputStream));
                        tankGame.warPanel.handleTankMove(tankMove);
                        break;
                    case MsgType.TANK_DEAD:
                        TankDead td = new TankDead(readToken(dataInputStream));
                        tankGame.warPanel.handleTankDead(td);
                        break;
                    case MsgType.MISSILE_DEAD:
                        MissileDead md = new MissileDead(readToken(dataInputStream));
                        tankGame.warPanel.handleMissileDead(md);
                        break;
                    case MsgType.MISSILE_NEW:
                        Missile missile = new Missile(readToken(dataInputStream));
                        tankGame.warPanel.handleNewMissile(missile);
                        break;
                    case MsgType.GAME_LEAVE:
                        GameLeave gl = new GameLeave(readToken(dataInputStream));
                        tankGame.homePanel.handleGameLeave(gl);
                        break;
                    case MsgType.GAME_END:
                        GameEnd ge = new GameEnd(readToken(dataInputStream));
                        tankGame.homePanel.handleGameEnd(ge);
                        break;

                    case MsgType.EXIT:
                        Exit e = new Exit(readToken(dataInputStream));
                        System.out.println(e.username + " exits");
                        tankGame.homePanel.handleExit(e);
                        break;
                }
            } catch (
                    IOException e) {
                e.printStackTrace();
            }
        }
    }
}