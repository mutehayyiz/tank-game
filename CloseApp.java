public class CloseApp {
    int connectionID;
    String username;
    String delimiter = "#d#";

    CloseApp(int connectionID, String username) {
        this.connectionID = connectionID;
        this.username = username;
    }

    CloseApp(String token) {
        String[] fielsds = token.split(delimiter);
        this.connectionID = Integer.parseInt(fielsds[0]);
        this.username = fielsds[1];
    }

    String Token() {
        return this.connectionID + delimiter + this.username;
    }
}

