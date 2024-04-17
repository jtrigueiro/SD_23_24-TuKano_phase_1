package tukano.servers.java;

//import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import java.util.logging.Logger;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import tukano.api.Short;
import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.api.java.Users;
import tukano.api.java.Result.ErrorCode;
import tukano.clients.ClientFactory;

public class BlobServer extends RestServer implements Blobs {
    protected static final int READ_TIMEOUT = 10000;
    protected static final int CONNECT_TIMEOUT = 10000;

    private final Map<String, byte[]> blobs = new HashMap<>();
    private static Logger Log = Logger.getLogger(BlobServer.class.getName());

    final Client client;
    final ClientConfig config;

    public BlobServer() {
        this.config = new ClientConfig();

        config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

        this.client = ClientBuilder.newClient(config);

    }

    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        Log.info("BlobServer: uploading blob " + blobId);

        Result<Void> check = checkBlobId(blobId);

        if (!check.isOK())
            return Result.error(ErrorCode.FORBIDDEN);

        // If the blob exists but the short is different, i.e. it's not a duplicate,
        // give an error
        if (blobs.containsKey(blobId) && !blobs.values().contains(bytes)) {
            Log.info("BlobServer: blob id exists with a different short.");
            return Result.error(ErrorCode.CONFLICT);
        }

        // If there is no error and it's a new blob add it to the map
        if (!blobs.containsKey(blobId)) {
            blobs.put(blobId, bytes);
            Log.info("BlobServer: uploaded blob ");
        }

        return Result.ok();
    }

    @Override
    public Result<byte[]> download(String blobId) {
        Log.info("BlobServer: downloading blob " + blobId);
        if (!blobs.containsKey(blobId)) {
            Log.info("BlobServer: blob id does not exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }
        return Result.ok(blobs.get(blobId));
    }

    @Override
    public Result<Void> checkBlobId(String blobId) {
        Users client = ClientFactory.getClient(Shorts.NAME);
        Result<String> check = client.checkBlobId(blobId);

        if (!check.isOK())
            return Result.error(check.error());

        return Result.ok();
    }

    @Override
    public Result<Void> delete(String blobId) {
        Log.info("BlobServer: deleting blob " + blobId);

        if (!blobs.containsKey(blobId)) {
            Log.info("BlobServer: blob id does not exist.");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        blobs.remove(blobId);
        return Result.ok();
    }

}
