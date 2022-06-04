import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private static int connectionID = 0;
    static final int UDP_SERVER_PORT = 4243;
    private static final int TCP_SERVER_PORT = 4242;
    private final List<Client> clients = new CopyOnWriteArrayList<>();
    private final List<Game> games = new CopyOnWriteArrayList<>();

    private void start() {
        new Thread(new UDPThread()).start();
        try {
            ServerSocket serverSocket = new ServerSocket(TCP_SERVER_PORT);
            do {
                Socket socket = serverSocket.accept();
                String socketIP = socket.getInetAddress().getHostAddress();

                DataInputStream input = new DataInputStream(socket.getInputStream());

                int socketUdpPort = input.readInt();

                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeInt(++connectionID);

                clients.add(new Client(socketIP, socketUdpPort, connectionID));
                System.out.println("new client with connection id " + connectionID);

                dataOutputStream.flush();
                dataOutputStream.close();
                socket.close();

            } while (!serverSocket.isClosed());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Server().start();
    }

    private class UDPThread implements Runnable {
        byte[] buffer = new byte[1024];

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

        public void send(DatagramSocket datagramSocket, String IP, int udpPort, int msgType, String token) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            try {
                dataOutputStream.writeInt(msgType);
                sendText(dataOutputStream, token);

                dataOutputStream.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }

            byte[] byteArray = byteArrayOutputStream.toByteArray();
            try {
                datagramSocket.send(new DatagramPacket(byteArray, byteArray.length, new InetSocketAddress(IP, udpPort)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                DatagramSocket datagramSocket = new DatagramSocket(UDP_SERVER_PORT);
                do {
                    DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                    datagramSocket.receive(datagramPacket);

                    DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(buffer, 0, datagramPacket.getLength()));

                    int msgType = dataInputStream.readInt();
                    switch (msgType) {
                        case MsgType.LOGIN_REQUEST:
                            System.out.println("new login request");
                            LoginRequest lr = new LoginRequest(readToken(dataInputStream));
                            boolean usernameExists = false;

                            for (Client c : clients) {
                                if (c.username.equals(lr.username)) {
                                    usernameExists = true;
                                    break;
                                }
                            }

                            for (Client c : clients) {
                                if (c.connectionID == connectionID) {
                                    if (usernameExists) {
                                        System.out.println("username exists " + lr.username);
                                        LoginResponse resp = new LoginResponse(false);
                                        send(datagramSocket, c.IP, c.udpPort, MsgType.LOGIN_RESPONSE, resp.Token());
                                    } else {
                                        c.username = lr.username;
                                        System.out.println("new login with username: " + lr.username);

                                        LoginResponse resp = new LoginResponse(true);
                                        send(datagramSocket, c.IP, c.udpPort, MsgType.LOGIN_RESPONSE, resp.Token());
                                        send(datagramSocket, c.IP, c.udpPort, MsgType.USER_LIST, tokenizeUserList());
                                        send(datagramSocket, c.IP, c.udpPort, MsgType.GAME_LIST, tokenizeGameList());
                                    }
                                    break;
                                }
                            }
                            break;

                        case MsgType.NEW_USER:
                            User u = new User(readToken(dataInputStream));
                            System.out.println("user " + u.username + "joined server");
                            notifyAll(datagramPacket, datagramSocket);
                            break;

                        case MsgType.GAME_NEW:
                            Game gm = new Game(readToken(dataInputStream));
                            System.out.println("new game request: " + gm.owner);
                            games.add(gm);
                            notifyAll(datagramPacket, datagramSocket);
                            break;

                        case MsgType.GAME_JOIN_ROOM:
                            GameJoinRoom jg = new GameJoinRoom(readToken(dataInputStream));
                            for (Game g : games) {
                                if (jg.gameOwner.equals(g.owner)) {
                                    g.userCount++;
                                    System.out.println("user " + jg.username + " joined game " + jg.gameOwner);
                                    break;
                                }
                            }

                            for (Client c : clients) {
                                if (c.username.equals(jg.username)) {
                                    c.currentGame = jg.gameOwner;
                                    break;
                                }
                            }

                            notifyAll(datagramPacket, datagramSocket);
                            break;


                        case MsgType.GAME_LEAVE_ROOM:
                            GameLeaveRoom wul = new GameLeaveRoom(readToken(dataInputStream));
                            for (Game g : games) {
                                if (wul.gameOwner.equals(g.owner)) {
                                    g.userCount--;
                                    System.out.println("wait users: user " + wul.username + " joined " + wul.gameOwner);
                                    break;
                                }
                            }

                            for (Client c : clients) {
                                if (c.username.equals(wul.username)) {
                                    c.currentGame = "";
                                    break;
                                }
                            }

                            notifyAll(datagramPacket, datagramSocket);
                            break;

                        case MsgType.GAME_CANCEL_ROOM:
                            GameCancelRoom wuc = new GameCancelRoom(readToken(dataInputStream));
                            games.removeIf(g -> g.owner.equals(wuc.gameOwner));
                            System.out.println("wait users cancel: " + wuc.gameOwner);

                            for (Client c : clients) {
                                if (c.currentGame.equals(wuc.gameOwner)) {
                                    c.currentGame = "";
                                }
                            }

                            notifyAll(datagramPacket, datagramSocket);
                            break;

                        case MsgType.GAME_START:
                            GameStart gs = new GameStart(readToken(dataInputStream));
                            for (Game g : games) {
                                if (g.owner.equals(gs.gameOwner)) {
                                    g.started = true;
                                    break;
                                }
                            }
                            notifyAll(datagramPacket, datagramSocket);
                            break;

                        case MsgType.TANK_NEW:

                        case MsgType.TANK_MOVE:
                            Tank t = new Tank(readToken(dataInputStream));
                            notifyPlayers(datagramPacket, datagramSocket, t.gameId);
                            break;
                        case MsgType.TANK_DEAD:
                            TankDead td = new TankDead(readToken(dataInputStream));
                            System.out.println("Dead tank " + td.tankID);
                            notifyPlayers(datagramPacket, datagramSocket, td.gameID);
                            break;

                        case MsgType.MISSILE_NEW:
                            Missile missile = new Missile(readToken(dataInputStream));
                            notifyPlayers(datagramPacket, datagramSocket, missile.gameID);
                            break;

                        case MsgType.MISSILE_DEAD:
                            MissileDead md = new MissileDead(readToken(dataInputStream));
                            System.out.println("Dead missile " + md.missileID);

                            notifyPlayers(datagramPacket, datagramSocket, md.gameId);
                            break;

                        case MsgType.GAME_LOSER:
                            GameLoser gl = new GameLoser(readToken(dataInputStream));
                            for (Game g : games) {
                                if (g.owner.equals(gl.gameID)) {
                                    g.loserCount++;
                                    break;
                                }
                            }

                            for (Client c : clients) {
                                if (c.currentGame.equals(gl.gameID) && c.username.equals(gl.username)) {
                                    c.loser = true;
                                    break;
                                }
                            }

                            if (checkFinish(gl.gameID)) {
                                finishGame(datagramSocket, gl.gameID);
                            }

                            break;

                        case MsgType.GAME_QUIT:
                            GameQuit gq = new GameQuit(readToken(dataInputStream));
                            games.forEach(g -> {
                                if (g.owner.equals(gq.gameID)) {
                                    g.userCount--;
                                    System.out.println("game: user " + gq.username + " leaved " + gq.gameID);

                                }
                            });

                            for (Client c : clients) {
                                if (c.username.equals(gq.username)) {
                                    c.currentGame = "";
                                    break;
                                }
                            }

                            if (checkFinish(gq.gameID)) {
                                finishGame(datagramSocket, gq.gameID);
                            } else {
                                notifyAll(datagramPacket, datagramSocket);
                            }

                            break;

                        case MsgType.CLOSE_APP:
                            CloseApp ca = new CloseApp(readToken(dataInputStream));
                            clients.removeIf(client -> {
                                System.out.println("close " + client.connectionID);
                                return client.connectionID == ca.connectionID;
                            });
                            notifyAll(datagramPacket, datagramSocket);
                            break;

                        default:
                            notifyAll(datagramPacket, datagramSocket);
                    }

                } while (!datagramSocket.isClosed());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String tokenizeUserList() {
            String delimiter = "%ddd%";
            StringBuilder list = new StringBuilder();
            for (Client c : clients) {
                User u = new User(c.username);
                list.append(delimiter).append(u.Token());
            }

            return list.toString();
        }

        String tokenizeGameList() {
            String delimiter = "%ddd%";
            StringBuilder list = new StringBuilder();
            for (Game g : games) {
                System.out.println("count: " + g.userCount);
                list.append(delimiter).append(g.Token());
            }

            return list.toString();
        }

        boolean checkFinish(String gameID) {
            boolean finish = false;
            for (Game g : games) {
                if (g.owner.equals(gameID)) {
                    if (g.loserCount + 1 == g.userCount) {
                        finish = true;
                    }
                    break;
                }
            }
            return finish;
        }

        public void finishGame(DatagramSocket datagramSocket, String gameID) {
            String winner = "";
            for (Client c : clients) {
                if (c.currentGame.equals(gameID)) {
                    c.currentGame = "";
                    if (!c.loser) {
                        winner = c.username;
                    }
                }
            }

            games.removeIf(g -> g.owner.equals(gameID));

            GameEnd ge = new GameEnd(gameID, winner);

            for (Client c : clients) {
                send(datagramSocket, c.IP, c.udpPort, MsgType.GAME_END, ge.Token());
            }
        }

        public void notifyAll(DatagramPacket datagramPacket, DatagramSocket datagramSocket) throws IOException {
            for (Client client : clients) {
                datagramPacket.setSocketAddress(new InetSocketAddress(client.IP, client.udpPort));
                datagramSocket.send(datagramPacket);
            }
        }

        public void notifyPlayers(DatagramPacket datagramPacket, DatagramSocket datagramSocket, String gameID) throws IOException {
            for (Client client : clients) {
                if (client.currentGame.equals(gameID)) {
                    datagramPacket.setSocketAddress(new InetSocketAddress(client.IP, client.udpPort));
                    datagramSocket.send(datagramPacket);
                }
            }
        }
    }

    public static class Client {
        int udpPort;
        int connectionID;

        String IP;
        String currentGame;
        String username;
        boolean loser;

        Client(String IP, int udpPort, int connectionID) {
            this.IP = IP;
            this.udpPort = udpPort;
            this.connectionID = connectionID;
            this.username = "";
            this.currentGame = "";
            loser = false;
        }
    }
}