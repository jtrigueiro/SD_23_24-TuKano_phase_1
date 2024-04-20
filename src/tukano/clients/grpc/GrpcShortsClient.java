package tukano.clients.grpc;

import java.net.URI;
import java.util.List;

import io.grpc.ManagedChannelBuilder;

import tukano.api.Short;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.impl.grpc.generated_java.ShortsGrpc;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.GetShortArgs;
import tukano.impl.grpc.generated_java.ShortsGrpc.ShortsBlockingStub;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.DeleteUserShortsArgs;

import static tukano.impl.grpc.common.DataModelAdaptor.GrpcShort_to_Short;

public class GrpcShortsClient extends GrpcClient implements Shorts {

    final ShortsBlockingStub stub;

    public GrpcShortsClient(URI serverURI) {
        var channel = ManagedChannelBuilder.forAddress(serverURI.getHost(), serverURI.getPort()).usePlaintext().build();
        stub = ShortsGrpc.newBlockingStub(channel);
    }

    @Override
    public Result<Void> deleteUserShorts(String userId) {
        return toJavaResult(() -> {
            stub.deleteUserShorts(DeleteUserShortsArgs.newBuilder()
                    .setUserId(userId)
                    .build());
                return null;
        });
    }

    @Override
    public Result<Short> getShort(String shortId) {
        return toJavaResult(() -> {
            var res = stub.getShort(GetShortArgs.newBuilder()
                    .setShortId(shortId)
                    .build());
            return GrpcShort_to_Short(res.getValue());
        });
    }

    // ----------------- Unimplemented methods -----------------

    @Override
    public Result<Short> createShort(String userId, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createShort'");
    }

    @Override
    public Result<Void> deleteShort(String shortId, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteShort'");
    }

    @Override
    public Result<List<String>> getShorts(String userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getShorts'");
    }

    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'follow'");
    }

    @Override
    public Result<List<String>> followers(String userId, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'followers'");
    }

    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'like'");
    }

    @Override
    public Result<List<String>> likes(String shortId, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'likes'");
    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFeed'");
    }

    
}
