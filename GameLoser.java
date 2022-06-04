public class GameLoser {
    String gameID;
    String username;
    String delimiter = "#d#";

    GameLoser(String gameID, String username) {
        this.gameID = gameID;
        this.username = username;
    }

    GameLoser(String token) {
        String[] fields = token.split(delimiter);
        this.gameID = fields[0];
        this.username = fields[1];
    }

    String Token() {
        return this.gameID + this.delimiter + this.username;
    }
}

