public class TokenResponse implements Serializable {
    private int statusCode;
    private String body;

    public TokenResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public int getStatusCode() { return statusCode; }
    public String getBody() { return body; }
}