package tukano.clients.grpc;

import java.net.URI;
import java.util.List;

import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Users;

public class GrpcUsersClient implements Users {

    public GrpcUsersClient(URI serverURI) {

    }

    @Override
    public Result<String> createUser(User user) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Result<User> getUser(String userId, String pwd) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Result<User> updateUser(String userId, String password, User user) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Result<User> deleteUser(String userId, String password) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Result<Void> createShort(String userId, String password, byte[] bytes) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Result<String> checkBlobId(String blobId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'checkBlobId'");
    }

    @Override
    public Result<Void> deleteUserShorts(String userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteUserShorts'");
    }

    @Override
    public Result<Void> deleteBlob(String blobId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteBlob'");
    }

    
}