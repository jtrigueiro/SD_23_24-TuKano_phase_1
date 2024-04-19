package tukano.clients.grpc;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import tukano.api.java.Result;
import io.grpc.ManagedChannelBuilder;
import tukano.api.User;
import tukano.api.java.Users;
import tukano.impl.grpc.generated_java.UsersGrpc;
import tukano.impl.grpc.generated_java.UsersProtoBuf.*;
import static tukano.impl.grpc.common.DataModelAdaptor.User_to_GrpcUser;
import static tukano.impl.grpc.common.DataModelAdaptor.GrpcUser_to_User;


public class GrpcUsersClient extends GrpcClient implements Users {

    final UsersGrpc.UsersBlockingStub stub;

    public GrpcUsersClient(URI serverURI) {
        var channel = ManagedChannelBuilder.forAddress(serverURI.getHost(), serverURI.getPort()).usePlaintext().build();
        stub = UsersGrpc.newBlockingStub(channel);
    }

    public Result<String> clt_createUser(User user) {
        return toJavaResult(() -> {
            var res = stub.createUser(CreateUserArgs.newBuilder()
                    .setUser(User_to_GrpcUser(user))
                    .build());
            return res.getUserId();
        });
    }

    public Result<User> clt_getUser(String userId, String pwd) {
        return toJavaResult(() -> {
            var res = stub.getUser(GetUserArgs.newBuilder()
                    .setUserId(userId)
                    .setPassword(pwd)
                    .build());
            return GrpcUser_to_User(res.getUser());
        });
    }

    public Result<User> clt_updateUser(String userId, String pwd, User user) {
        return toJavaResult(() -> {
            var res = stub.updateUser(UpdateUserArgs.newBuilder()
                    .setUserId(userId)
                    .setPassword(pwd)
                    .setUser(User_to_GrpcUser(user))
                    .build());
            return GrpcUser_to_User(res.getUser());
        });
    }

    public Result<User> clt_deleteUser(String userId, String pwd) {
        return toJavaResult(() -> {
            var res = stub.deleteUser(DeleteUserArgs.newBuilder()
                    .setUserId(userId)
                    .setPassword(pwd)
                    .build());
            return GrpcUser_to_User(res.getUser());
        });
    }

    public Result<List<User>> clt_searchUsers(String pattern) {
        return toJavaResult(() -> {
            var res = stub.searchUsers(SearchUserArgs.newBuilder()
                    .setPattern(pattern)
                    .build());

            List<User> users = new ArrayList<>();
            while (res.hasNext()) {
                users.add(GrpcUser_to_User(res.next()));
            }
            return users;
        });
    }

    public Result<String> clt_checkBlobId(String blobId) {
        return toJavaResult(() -> {
            var res = stub.checkBlobId(CheckBlobIdArgs.newBuilder()
                    .setBlobId(blobId)
                    .build());
            return res.getBlobId();
        });
    }

    public Result<Void> clt_deleteUserShorts(String userId) {
        return toJavaResult(() -> {
            stub.deleteUserShorts(DeleteUserShortsArgs.newBuilder()
                    .setUserId(userId)
                    .build());
        });
    }

    public Result<Void> clt_deleteBlob(String blobId) {
        return toJavaResult(() -> {
            stub.deleteBlob(DeleteBlobArgs.newBuilder()
                    .setBlobId(blobId)
                    .build());
        });
    }

    @Override
    public Result<String> createUser(User user) {
        return reTry(() -> clt_createUser(user));
    }

    @Override
    public Result<User> getUser(String userId, String pwd) {
        return reTry(() -> clt_getUser(userId, pwd));
    }

    @Override
    public Result<User> updateUser(String userId, String pwd, User user) {
        return reTry(() -> clt_updateUser(userId, pwd, user));
    }

    @Override
    public Result<User> deleteUser(String userId, String pwd) {
        return reTry(() -> clt_deleteUser(userId, pwd));
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
        return reTry(() -> clt_searchUsers(pattern));
    }

    @Override
    public Result<String> checkBlobId(String blobId) {
        return reTry(() -> clt_checkBlobId(blobId));
    }

    @Override
    public Result<Void> deleteUserShorts(String userId) {
        return reTry(() -> clt_deleteUserShorts(userId));
    }

    @Override
    public Result<Void> deleteBlob(String blobId) {
        return reTry(() -> clt_deleteBlob(blobId));
    }

    @Override
    public Result<Void> createShort(String userId, String password, byte[] bytes) {
        // TODO Auto-generated method stub
        return null;
    }

}