public class MissileDead {
    String tankID;
    int missileID;
    String gameId;
    String delimiter = "#d#";

    MissileDead(String tankID, int missileID, String gameId) {
        this.tankID = tankID;
        this.missileID = missileID;
        this.gameId = gameId;
    }
    MissileDead(String token) {
        String[] fields = token.split(delimiter);
        this.tankID = fields[0];
        this.missileID = Integer.parseInt(fields[1]);
        this.gameId = fields[2];
    }

    String Token() {
        return tankID + delimiter + missileID + delimiter + missileID;
    }
}
