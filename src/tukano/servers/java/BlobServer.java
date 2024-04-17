package tukano.servers.java;

import java.util.Map;
import java.util.HashMap;

import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.api.java.Users;
import tukano.clients.ClientFactory;
import tukano.clients.rest.RestClient;

public class BlobServer extends RestClient implements Blobs {

    private final Map<String, byte[]> blobs;

    public BlobServer() {
        this.blobs = new HashMap<>();
    }

    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        Result<Void> bCheck = checkBlobId(blobId);

        // Check if the blobId is verified
        if (!bCheck.isOK())
            return Result.error(Result.ErrorCode.FORBIDDEN);

        // Check if the blobId is already in use with a different value
        if (blobs.containsKey(blobId) && !blobs.values().contains(bytes))
            return Result.error(Result.ErrorCode.CONFLICT);

        // Check if the blobId is not in use
        if (!blobs.containsKey(blobId))
            blobs.put(blobId, bytes);

        return Result.ok();
    }

    @Override
    public Result<byte[]> download(String blobId) {

        // Check if the blobId exists
        if (!blobs.containsKey(blobId))
            return Result.error(Result.ErrorCode.NOT_FOUND);

        return Result.ok(blobs.get(blobId));
    }

    @Override
    public Result<Void> checkBlobId(String blobId) {
        Users client = ClientFactory.getClient(Shorts.NAME);
        Result<String> bCheck = client.checkBlobId(blobId);

        // Check if the blobId is verified
        if (!bCheck.isOK())
            return Result.error(bCheck.error());

        return Result.ok();
    }

    @Override
    public Result<Void> delete(String blobId) {

        // Check if the blobId exists
        if (!blobs.containsKey(blobId))
            return Result.error(Result.ErrorCode.NOT_FOUND);

        blobs.remove(blobId);
        return Result.ok();
    }

}
