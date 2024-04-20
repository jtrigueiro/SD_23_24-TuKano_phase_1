package tukano.clients.rest;

import java.net.URI;

import java.util.List;

import tukano.api.Short;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.api.rest.RestShorts;

import jakarta.ws.rs.client.WebTarget;


public class RestShortsClient extends RestClient implements Shorts {

    final WebTarget target;

    public RestShortsClient(URI serverURI) {
        super();
        target = client.target(serverURI).path(RestShorts.PATH);
    }

    public Result<Void> clt_deleteUserShorts(String userId) {
        return super.toJavaResult(
				target.path(userId)
						.path(RestShorts.DELETES)
						.request()
						.delete(),
				Void.class);
    }

    public Result<Void> clt_checkBlobId(String blobId) {
        return super.toJavaResult(
                target.path(blobId)
                        .path(RestShorts.CHECK)
                        .request()
                        .get(),
                Void.class);
    }
    

    @Override
	public Result<Void> deleteUserShorts(String userId) {
		return super.reTry(() -> clt_deleteUserShorts(userId));
	}

    @Override
    public Result<Void> checkBlobId(String blobId) {
        return super.reTry(() -> clt_checkBlobId(blobId));
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
    public Result<Short> getShort(String shortId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getShort'");
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
