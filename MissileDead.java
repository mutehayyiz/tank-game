public class MissileDead {
    String tankID;
    int missileID;

    String delimiter = "#d#";

    MissileDead(String tankID, int missileID) {
        this.tankID = tankID;
        this.missileID = missileID;
    }

    MissileDead(String token) {
        String[] fields = token.split(delimiter);
        this.tankID = fields[0];
        this.missileID = Integer.parseInt(fields[1]);
    }

    String Token() {
        return tankID + delimiter + missileID;
    }

}
