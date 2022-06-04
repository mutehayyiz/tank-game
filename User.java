import java.io.DataInputStream;
import java.io.IOException;

public class User {
    String username;

    User(String token) {
        this.username = token;
    }

    public String Token() {
        return username;
    }
}
