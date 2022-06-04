public class TankDead {
    String tankID;
    String gameID;
    String delimiter = "#d#";

    TankDead(String tankID, String gameID) {
        this.tankID = tankID;
        this.gameID = gameID;
    }

    TankDead(String token) {
        String[] fields = token.split(delimiter);
        this.tankID = fields[0];
        this.gameID = fields[1];
    }

    String Token() {
        return this.tankID + this.delimiter + this.gameID;
    }
}
