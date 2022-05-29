import javax.print.attribute.standard.JobName;

public class GameStart {
    String gameOwner;

    String delimiter = "#d#";

    GameStart(String gameOwner) {
        this.gameOwner = gameOwner;
    }

    String Token() {
        return gameOwner;
    }
}
