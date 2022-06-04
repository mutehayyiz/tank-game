import javax.print.attribute.standard.JobName;

public class GameStart {
    String gameOwner;

    GameStart(String gameOwner) {
        this.gameOwner = gameOwner;
    }

    String Token() {
        return gameOwner;
    }
}
