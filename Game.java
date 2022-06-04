public class Game {
    String owner;
    int tourCount;
    int userCount;
    int loserCount;
    boolean started;
    String delimiter = "#d#";

    Game(String owner, int tourCount, int playerSize, boolean started) {
        this.owner = owner;
        this.tourCount = tourCount;
        this.userCount = playerSize;
        this.started = started;
        this.loserCount = 0;
    }

    Game(String token) {
        String[] fields = token.split(delimiter);
        this.owner = fields[0];
        this.tourCount = Integer.parseInt(fields[1]);
        this.userCount = Integer.parseInt(fields[2]);
        this.started = Boolean.parseBoolean(fields[3]);
        for (int i = 0; i < fields.length; i++) {
            System.out.println(i + 1 + " " + fields[i]);
        }
    }

    String Token() {
        return this.owner + this.delimiter +
                this.tourCount + this.delimiter +
                this.userCount + this.delimiter +
                this.started;
    }

    public static void main(String[] args) {
        Game ch = new Game("ahmet", 3, 5, true);
        String token = ch.Token();
        System.out.println(token);
        Game ch2 = new Game(token);
        System.out.println(ch2.owner + " " + ch2.tourCount + " " + ch2.userCount + " " + ch2.started);
    }
}