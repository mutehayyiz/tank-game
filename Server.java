import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private static int connectionID = 1;
    static final int UDP_SERVER_PORT = 4243;
    private static final int TCP_SERVER_PORT = 4242;
    private final List<Client> clients = new CopyOnWriteArrayList<>();
    private final List<Game> games = new CopyOnWriteArrayList<>();

    private void start() {
        new Thread(new UDPThread()).start();
        try {
            ServerSocket serverSocket = new ServerSocket(TCP_SERVER_PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                String socketIP = socket.getInetAddress().getHostAddress();
                int socketPort = socket.getPort();

                DataInputStream input = new DataInputStream(socket.getInputStream());

                int socketUdpPort = input.readInt();

                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeInt(connectionID++);

                clients.add(new Client(socketIP, socketUdpPort, connectionID));

                dataOutputStream.flush();
                dataOutputStream.close();
                socket.close();

                if (serverSocket.isClosed()) {
                    break;
                }
            }
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
            return new String(array, "UTF-8");
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
                while (true) {
                    DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                    datagramSocket.receive(datagramPacket);

                    DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(buffer, 0, datagramPacket.getLength()));

                    int msgType = dataInputStream.readInt();
                    switch (msgType) {
                        case MsgType.LOGIN_REQUEST:
                            System.out.println("new login request");
                            LoginRequest lr = new LoginRequest(readToken(dataInputStream));
                            boolean usernameExists = false;

                            System.out.println(lr.username);

                            for (Client c : clients) {
                                if (c.username.equals(lr.username)) {
                                    usernameExists = true;
                                    break;
                                }
                            }

                            for (Client c : clients) {
                                if (c.connectionID == connectionID) {
                                    if (usernameExists) {
                                        System.out.println("username exists");
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

                        case MsgType.NEW_GAME:
                            Game gm = new Game(readToken(dataInputStream));
                            games.add(gm);
                            notifyAll(datagramPacket, datagramSocket);
                            break;

                        case MsgType.WAIT_USERS_JOIN:
                            WaitUsersJoin jg = new WaitUsersJoin(readToken(dataInputStream));
                            games.forEach(g -> {
                                if (jg.gameOwner.equals(g.owner)) {
                                    g.userCount++;
                                    System.out.println("new join, count: " + g.userCount);
                                }
                            });

                            notifyAll(datagramPacket, datagramSocket);
                            break;
                        case MsgType.WAIT_USERS_LEAVE:
                            WaitUsersLeave wul = new WaitUsersLeave(readToken(dataInputStream));
                            games.forEach(g -> {
                                if (wul.gameOwner.equals(g.owner)) {
                                    g.userCount--;
                                    System.out.println("new leave, count: " + g.userCount);
                                }
                            });

                            notifyAll(datagramPacket, datagramSocket);

                            break;
                        case MsgType.WAIT_USERS_CANCEL:
                            WaitUsersCancel wuc = new WaitUsersCancel(readToken(dataInputStream));
                            games.removeIf(g -> g.owner.equals(wuc.gameOwner));
                            System.out.println("wait users cancel: " + wuc.gameOwner);

                            notifyAll(datagramPacket, datagramSocket);
                            break;

                        case MsgType.GAME_LEAVE:
                            GameLeave gml = new GameLeave(readToken(dataInputStream));
                            games.forEach(g -> {
                                if (g.owner.equals(gml.gameOwner)) {
                                    g.userCount--;
                                }
                            });
                            notifyAll(datagramPacket, datagramSocket);
                            break;

                        case MsgType.GAME_END:
                            GameEnd ge = new GameEnd(readToken(dataInputStream));
                            games.removeIf(g -> ge.gameID.equals(g.owner));
                            notifyAll(datagramPacket, datagramSocket);
                            break;

                        case MsgType.EXIT:
                            Exit e = new Exit(readToken(dataInputStream));
                            //TODO remove connected but not logged in client
                            clients.removeIf(c -> c.username.equals(e.username));
                            notifyAll(datagramPacket, datagramSocket);
                            break;
                        default:
                            notifyAll(datagramPacket, datagramSocket);
                    }

                    if (datagramSocket.isClosed()) {
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String tokenizeUserList() {
            String delimiter = "%ddd%";
            String list = "";
            for (Client c : clients) {
                User u = new User(c.username);
                list += delimiter + u.Token();
            }

            return list;
        }

        String tokenizeGameList() {
            String delimiter = "%ddd%";
            String list = "";
            for (Game g : games) {
                System.out.println("count: " + g.userCount);
                list += delimiter + g.Token();
            }

            return list;
        }

        public void notifyAll(DatagramPacket datagramPacket, DatagramSocket datagramSocket) throws IOException {
            for (Client client : clients) {
                datagramPacket.setSocketAddress(new InetSocketAddress(client.IP, client.udpPort));
                datagramSocket.send(datagramPacket);
            }
        }


    }


    public static class Client {
        String IP;
        int udpPort;
        int userId;
        String username;
        boolean inGame;

        int connectionID;

        Client(String IP, int udpPort, int connectionID) {
            this.IP = IP;
            this.udpPort = udpPort;
            this.connectionID = connectionID;
            this.username = "";
            this.inGame = false;
        }

        Client(String IP, int udpPort, int userId, String username, boolean inGame) {
            this.IP = IP;
            this.udpPort = udpPort;
            this.userId = userId;
            this.username = username;
            this.inGame = inGame;
            System.out.println(this);
        }

        public String toString() {
            return "IP: " + this.IP + " \nPort: " + this.udpPort + "\nUsername: " + username + "\nInGame: " + inGame + "\n";
        }
    }
}