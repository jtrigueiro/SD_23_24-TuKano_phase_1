package tukano.servers.java;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import java.util.logging.Logger;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import jakarta.ws.rs.client.WebTarget;


import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.api.rest.RestShorts;
import tukano.utils.Discovery;

public class BlobServer extends RestServer implements Blobs {
    protected static final int READ_TIMEOUT = 5000;
	protected static final int CONNECT_TIMEOUT = 5000;

    private final Map<String, byte[]> blobs = new HashMap<>();
    private static Logger Log = Logger.getLogger(BlobServer.class.getName());

    final Client client;
	final ClientConfig config;

    final URI serverURI;
    final URI[] shortsServer;
    final WebTarget ssTarget;

    public BlobServer(URI serverURI) {
        this.serverURI = serverURI;
        this.config = new ClientConfig();

        config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
		config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

        this.client = ClientBuilder.newClient(config);

        Discovery discovery = Discovery.getInstance();
        discovery.announce("blobs", serverURI.toString());

        shortsServer = discovery.knownUrisOf("shorts", 1);
        ssTarget = client.target(shortsServer[0]).path(RestShorts.PATH);

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

    private Result<Void> svr_checkBlobId(String blobId) {
        if (blobId == null)
            return Result.error(ErrorCode.BAD_REQUEST);

        return super.toJavaResult(
            ssTarget.path(blobId).path(RestShorts.CHECK).request().get(), Void.class);
    }

    @Override
    public Result<Void> checkBlobId(String blobId) {
        return super.reTry(() -> svr_checkBlobId(blobId));
    }

}
