public class GameCancelRoom {
    String gameOwner;

    GameCancelRoom(String gameOwner) {
        this.gameOwner = gameOwner;
    }

    String Token() {
        return gameOwner;
    }
}
