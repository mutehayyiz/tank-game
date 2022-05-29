
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

    public static void main(String []args){
        LoginResponse ch = new LoginResponse(false);
        String token = ch.Token();
        System.out.println(token);
        LoginResponse ch2 = new LoginResponse(token);
        System.out.println(ch2.loggedIn);
    }

}
