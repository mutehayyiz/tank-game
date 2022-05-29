
public class LoginRequest {
    int connectionID;
    String username;

    LoginRequest(int connectionID, String username){
        this.connectionID = connectionID;
        this.username = username;
    }

    LoginRequest(String token){
        String delimiter = " ";
        String[] fields = token.split(delimiter);
        this.connectionID = Integer.parseInt(fields[0]);
        this.username = fields[1];
    }

    public String Token() {
        return this.connectionID+ " " + this.username;
    }
}
