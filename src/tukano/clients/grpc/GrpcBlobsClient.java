package tukano.clients.grpc;

import java.net.URI;

import io.grpc.ManagedChannelBuilder;

import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.impl.grpc.generated_java.BlobsGrpc;
import tukano.impl.grpc.generated_java.BlobsProtoBuf.DeleteArgs;
import tukano.impl.grpc.generated_java.BlobsGrpc.BlobsBlockingStub;

public class GrpcBlobsClient extends GrpcClient implements Blobs {

    private final BlobsBlockingStub stub;

    public GrpcBlobsClient(URI serverURI) {
        var channel = ManagedChannelBuilder.forAddress(serverURI.getHost(), serverURI.getPort()).usePlaintext().build();
        stub = BlobsGrpc.newBlockingStub(channel);
    }

    @Override
    public Result<Void> delete(String blobId) {
        return toJavaResult(() -> {
            stub.delete(DeleteArgs.newBuilder()
                    .setBlobId(blobId)
                    .build());
            return null;
        });
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
