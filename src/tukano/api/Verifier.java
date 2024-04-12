package tukano.api;

public class Verifier {
    private String shortId, verifier, blobId;

    public Verifier() {
    }

    public Verifier(String shortId, String verifier, String blobId) {
        this.shortId = shortId;
        this.verifier = verifier;
        this.blobId = blobId;
    }

    public String getShortId() {
        return shortId;
    }

    // blobs/thttp://localhost:8080/blobs/1/
    public String getVerifier() {
        return verifier;
    }

    public String getBlobId() {
        return blobId;
    }
}
