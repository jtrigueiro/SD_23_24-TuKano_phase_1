package tukano.clients.grpc;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import io.grpc.Status;
import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import io.grpc.ManagedChannelBuilder;
import tukano.api.User;
import tukano.api.java.Users;
import tukano.impl.grpc.generated_java.UsersGrpc;
import tukano.impl.grpc.generated_java.UsersProtoBuf.*;
import static tukano.impl.grpc.common.DataModelAdaptor.User_to_GrpcUser;
import static tukano.impl.grpc.common.DataModelAdaptor.GrpcUser_to_User;
import java.util.function.Supplier;
import static tukano.api.java.Result.error;
import static tukano.api.java.Result.ok;

public class GrpcUsersClient implements Users {

    private static final long GRPC_REQUEST_TIMEOUT = 5000;
    final UsersGrpc.UsersBlockingStub stub;

    public GrpcUsersClient(URI serverURI) {
        var channel = ManagedChannelBuilder.forAddress(serverURI.getHost(), serverURI.getPort()).usePlaintext().build();
        stub = UsersGrpc.newBlockingStub(channel).withDeadlineAfter(GRPC_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    @Override
    public Result<String> createUser(User user) {
        return toJavaResult(() -> {
            var res = stub.createUser(CreateUserArgs.newBuilder()
                    .setUser(User_to_GrpcUser(user))
                    .build());
            return res.getUserId();
        });
    }

    @Override
    public Result<User> getUser(String userId, String pwd) {
        return toJavaResult(() -> {
            var res = stub.getUser(GetUserArgs.newBuilder()
                    .setUserId(userId)
                    .setPassword(pwd)
                    .build());
            return GrpcUser_to_User(res.getUser());
        });
    }

    @Override
    public Result<User> updateUser(String userId, String pwd, User user) {
        return toJavaResult(() -> {
            var res = stub.updateUser(UpdateUserArgs.newBuilder()
                    .setUserId(userId)
                    .setPassword(pwd)
                    .setUser(User_to_GrpcUser(user))
                    .build());
            return GrpcUser_to_User(res.getUser());
        });
    }

    @Override
    public Result<User> deleteUser(String userId, String pwd) {
        return toJavaResult(() -> {
            var res = stub.deleteUser(DeleteUserArgs.newBuilder()
                    .setUserId(userId)
                    .setPassword(pwd)
                    .build());
            return GrpcUser_to_User(res.getUser());
        });
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
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

    @Override
    public Result<Void> createShort(String userId, String password, byte[] bytes) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Result<String> checkBlobId(String blobId) {
        return toJavaResult(() -> {
            var res = stub.checkBlobId(CheckBlobIdArgs.newBuilder()
                    .setBlobId(blobId)
                    .build());
            return res.getBlobId();
        });
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
    public Result<Void> deleteBlob(String blobId) {
        return toJavaResult(() -> {
            stub.deleteBlob(DeleteBlobArgs.newBuilder()
                    .setBlobId(blobId)
                    .build());
            return null;
        });
    }

    static <T> Result<T> toJavaResult(Supplier<T> func) {
        try {
            return ok(func.get());
        } catch (StatusRuntimeException sre) {
            var code = sre.getStatus().getCode();
            if (code == Code.UNAVAILABLE || code == Code.DEADLINE_EXCEEDED)
                throw sre;
            return error(statusToErrorCode(sre.getStatus()));
        }
    }

    static ErrorCode statusToErrorCode(Status status) {
        return switch (status.getCode()) {
            case OK -> ErrorCode.OK;
            case NOT_FOUND -> ErrorCode.NOT_FOUND;
            case ALREADY_EXISTS -> ErrorCode.CONFLICT;
            case PERMISSION_DENIED -> ErrorCode.FORBIDDEN;
            case INVALID_ARGUMENT -> ErrorCode.BAD_REQUEST;
            case UNIMPLEMENTED -> ErrorCode.NOT_IMPLEMENTED;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }

}