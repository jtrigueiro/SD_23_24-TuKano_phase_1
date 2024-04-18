package tukano.servers.grpc;

import static tukano.impl.grpc.common.DataModelAdaptor.*;
import javax.naming.directory.SearchResult;

import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;
import tukano.api.java.Result;
import tukano.api.java.Blobs;
import tukano.impl.grpc.generated_java.BlobsGrpc;
import tukano.impl.grpc.generated_java.UsersProtoBuf.*;
import tukano.servers.java.ShortsServer;
import tukano.servers.java.BlobServer;

public class GrpcBlobsServerStub implements BlobsGrpc.AsyncService, BindableService {

    Blobs impl = new BlobServer();

    @Override
    public ServerServiceDefinition bindService() {
        return BlobsGrpc.bindService(this);
    }

}
