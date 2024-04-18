package tukano.servers.grpc;

import static tukano.impl.grpc.common.DataModelAdaptor.*;
import javax.naming.directory.SearchResult;

import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.impl.grpc.generated_java.ShortsGrpc;
import tukano.impl.grpc.generated_java.UsersProtoBuf.*;
import tukano.servers.java.ShortsServer;

public class GrpcShortsServerStub implements ShortsGrpc.AsyncService, BindableService {

    Shorts impl = new ShortsServer();

    @Override
    public ServerServiceDefinition bindService() {
        return ShortsGrpc.bindService(this);
    }

}
