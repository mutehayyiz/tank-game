public class GameEnd {
    String gameID;
    String winner;
    String delimiter = "#d#";

    GameEnd(String gameID, String winner) {
        this.gameID = gameID;
        this.winner = winner;
    }

    GameEnd(String token) {
        String[] fields = token.split(delimiter);
        this.gameID = fields[0];
        this.winner = fields[1];
    }

    String Token() {
        return this.gameID + this.delimiter + this.winner;
    }
}

