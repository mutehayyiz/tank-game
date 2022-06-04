public class GameLeaveRoom {
    String username;
    String gameOwner;
    String delimiter = "#d#";

    GameLeaveRoom(String username, String gameOwner) {
        this.username = username;
        this.gameOwner = gameOwner;
    }

    GameLeaveRoom(String token) {
        String[] fields = token.split(delimiter);
        this.username = fields[0];
        this.gameOwner = fields[1];
    }

    String Token() {
        return username + delimiter + gameOwner;
    }
}
