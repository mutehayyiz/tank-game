
public class LoginResponse {
    boolean loggedIn;

    LoginResponse(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    LoginResponse(String token) {
        this.loggedIn = Boolean.parseBoolean(token);
    }

    public String Token() {
        return String.valueOf(loggedIn);
    }
}
