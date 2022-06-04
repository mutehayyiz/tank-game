public class GameQuit {
    String gameID;
    String username;
    String delimiter = "#d#";

    GameQuit(String gameID, String username) {
        this.username = username;
        this.gameID = gameID;
    }

    GameQuit(String token) {
        String[] fields = token.split(delimiter);
        this.gameID = fields[0];
        this.username = fields[1];
    }

    String Token() {
        return this.gameID + this.delimiter + this.username;
    }
}

