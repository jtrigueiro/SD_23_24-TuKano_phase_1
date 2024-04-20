package tukano.clients.rest;

import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.api.rest.RestBlobs;

import java.net.URI;

import jakarta.ws.rs.client.WebTarget;

public class RestBlobsClient extends RestClient implements Blobs {

    private final WebTarget target;

    public RestBlobsClient(URI serverURI) {
        super();
        target = client.target(serverURI).path(RestBlobs.PATH);
    }

    public Result<Void> clt_delete(String blobId) {
        return super.toJavaResult(
            target.path(blobId)
                    .request()
                    .delete(),
            Void.class);
    }

    @Override
    public Result<Void> delete(String blobId) {
        return super.reTry(() -> clt_delete(blobId));
    }

    // ----------------- Unimplemented methods -----------------

    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'upload'");
    }

    @Override
    public Result<byte[]> download(String blobId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'download'");
    }
    
}
