public class ChatMessage {
    String username;
    String message;
    ChatMessage(String username, String message) {
        this.username = username;
        this.message = message;
    }
    ChatMessage(String token) {
        String delimiter = "#.delimiter.#";
        String[] fields = token.split(delimiter);
        this.username = fields[0];
        this.message = fields[1];
    }
    String Token() {
        return this.username + "#.delimiter.#" + message;
    }

    public static void main(String []args){
        ChatMessage ch = new ChatMessage("ahemt", "naber nasilsiniz");
        String token = ch.Token();
        System.out.println(token);
        ChatMessage ch2 = new ChatMessage(token);
        System.out.println(ch2.username +": " + ch2.message);
    }
}

