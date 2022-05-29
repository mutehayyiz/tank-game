public class WaitUsersLeave {
    String username;
    String gameOwner;

    String delimiter = "#d#";

    WaitUsersLeave(String username, String gameOwner) {
        this.username = username;
        this.gameOwner = gameOwner;
    }

    WaitUsersLeave(String token) {
        String[] fields = token.split(delimiter);
        this.username = fields[0];
        this.gameOwner = fields[1];
    }

    String Token() {
        return username + delimiter + gameOwner;
    }
}
