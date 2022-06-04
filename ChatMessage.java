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
}

