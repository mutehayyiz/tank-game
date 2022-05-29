public class GameEnd {
    String gameID;
    GameEnd(String gameID) {
        this.gameID = gameID;
    }

    String Token(){
        return this.gameID;
    }
}

