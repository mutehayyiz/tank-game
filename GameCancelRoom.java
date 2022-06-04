public class GameCancel {
    String gameOwner;
    GameCancel(String gameOwner) {
        this.gameOwner = gameOwner;
    }

    String Token() {
        return gameOwner;
    }
}
