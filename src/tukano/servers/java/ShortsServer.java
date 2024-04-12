package tukano.servers.java;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import tukano.api.Short;
import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.api.java.Shorts;
import tukano.api.rest.RestUsers;
import tukano.api.Discovery;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import tukano.persistence.Hibernate;

public class ShortsServer extends RestServer implements Shorts {

    protected static final int READ_TIMEOUT = 5000;
    protected static final int CONNECT_TIMEOUT = 5000;

    final Client client;
    final ClientConfig config;   

    private URI[] usersServer;
    private URI[] blobServers;

    private WebTarget usTarget;
    private WebTarget[] bTargets;

    private static String queryShortId = "SELECT s FROM Short s WHERE s.shortId = '%s'";
    private static String queryOwnerId = "SELECT s FROM Short s WHERE s.ownerId = '%s'";
    private static Discovery discovery;


    public ShortsServer(URI serverURI) {
        this.config = new ClientConfig();

        config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

        this.client = ClientBuilder.newClient(config);

        discovery = Discovery.getInstance();
        discovery.announce("shorts", serverURI.toString());

        usersServer = discovery.knownUrisOf("users", 1);
        blobServers = discovery.knownUrisOf("blobs", 3);

        usTarget = client.target(usersServer[0]).path(RestUsers.PATH);
        for(int i = 0 ; i < blobServers.length; i++)
            bTargets[i] = client.target(blobServers[i]).path(RestUsers.PATH);
    }

    @Override
    public Result<Short> createShort(String userId, String password) {
        Result<User> result = checkUserIdAndPassword(userId, password);

        if(!result.isOK())
            return Result.error(result.error());
        
        Short s = new Short(userId, "blobUrl");
        Hibernate.getInstance().persist(s);
        return Result.ok(s);
    }

    @Override
    public Result<Void> deleteShort(String shortId, String password) {
        var result = hibernateQuery(String.format(queryShortId, shortId), Short.class);

        if (result.error() != ErrorCode.OK)
            return Result.error(result.error());

        Short s = result.value().get(0);
        Result<User> check = checkUserIdAndPassword(s.getOwnerId(), password);

        if(check.error() != ErrorCode.OK )
            return Result.error(check.error());

        Hibernate.getInstance().delete(s);
        return Result.ok();
    }

    @Override
    public Result<Short> getShort(String shortId) {
        var result = hibernateQuery(String.format(queryShortId, shortId), Short.class);

        return result.error() == ErrorCode.OK ? Result.ok(result.value().get(0)) : Result.error(result.error());
    }

    @Override
    public Result<List<String>> getShorts(String userId) {
        Result<User> check = hasUser(userId);

        if(hasUser(userId).error() != ErrorCode.OK)
            return Result.error(check.error());

        var result = hibernateQuery(String.format(queryOwnerId, userId), String.class);
        return result.error() == ErrorCode.OK ? Result.ok(result.value()) : Result.error(result.error());
    }

    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
        var check1 = checkUserIdAndPassword(userId1, password);

        if(check1.error() != ErrorCode.OK)
            return Result.error(check1.error());

        var check2 = hasUser(userId2);

        if(check2.error() != ErrorCode.OK)
            return Result.error(check2.error());

        User u1 = check1.value();
        User u2 = check2.value();
        
        if(isFollowing) {
            u1.follow(userId2);
            u2.addFollower(userId1);
        } else {
            u1.unfollow(userId2);
            u2.removeFollower(userId1);
        }

        Result<Void> result = updateUser(userId1, password, u1);
        if(result.isOK())
            return updateUser(userId2, password, u2);
        else
            return Result.error(result.error());
    }


    @Override
    public Result<List<String>> followers(String userId, String password) {
        var check = checkUserIdAndPassword(userId, password);

        if(check.error() != ErrorCode.OK)
            return Result.error(check.error());

        return Result.ok(check.value().followers());
    }


    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
        var check = checkUserIdAndPassword(userId, password);

        if(check.error() != ErrorCode.OK)
            return Result.error(check.error());

        var shortResult = hibernateQuery(String.format(queryShortId, shortId), Short.class);
        
        if(shortResult.error() != ErrorCode.OK)
            return Result.error(shortResult.error());

        User u = check.value();
        Short s = shortResult.value().get(0);

        if(isLiked) {
            u.like(shortId);
            s.addLike(userId);
        } else {
            u.unlike(shortId);
            s.removeLike(userId);
        }

        Hibernate.getInstance().update(s);
        return updateUser(userId, password, u);
    }

    @Override
    public Result<List<String>> likes(String shortId, String password) {
        var shortResult = hibernateQuery(String.format(queryShortId, shortId), Short.class);

        if(shortResult.error() != ErrorCode.OK)
            return Result.error(shortResult.error());

        Short s = shortResult.value().get(0);
        return Result.ok(s.getLikes());
    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {
        var check = checkUserIdAndPassword(userId, password);

        if(check.error() != ErrorCode.OK)
            return Result.error(check.error());

        User u = check.value();
        List<String> following = u.following();
        var feed = new ArrayList<String>();

        for(String f : following) {
            var shorts = hibernateQuery(String.format(queryOwnerId, f), Short.class);

            if(shorts.error() != ErrorCode.OK)
                return Result.error(shorts.error());

            for(Short s : shorts.value())
                feed.add(s.getShortId());
        }

        return Result.ok(feed);
    }

    @Override
    public Result<User> checkUserIdAndPassword(String userId, String pwd) {
        return super.reTry(() -> svr_checkUserIdAndPassword(userId, pwd));
    }

    @Override
    public Result<User> hasUser(String userId) {
        return super.reTry(() -> svr_hasUser(userId));
    }

    @Override
    public Result<Void> updateUser(String userId, String pwd, User user) {
        return super.reTry(() -> svr_updateUser(userId, pwd, user));
    }

    private Result<Void> svr_updateUser(String userId, String pwd, User user) {
        return (userId != null || pwd != null || user != null) ? Result.error(ErrorCode.BAD_REQUEST) :
        super.toJavaResult(
                usTarget.path( userId )
                .queryParam(RestUsers.PWD, pwd).request()
                .accept(MediaType.APPLICATION_JSON)
                .put(Entity.entity(user, MediaType.APPLICATION_JSON)), Void.class);
    }

    private Result<User> svr_checkUserIdAndPassword(String userId, String pwd) {
        return (userId != null || pwd != null) ? Result.error(ErrorCode.BAD_REQUEST) :
        super.toJavaResult(
                usTarget.path( userId )
                .queryParam(RestUsers.PWD, pwd).request()
                .accept(MediaType.APPLICATION_JSON)
                .get(), User.class);
    }

    private Result<User> svr_hasUser(String userId) {
        return (userId != null) ? Result.error(ErrorCode.BAD_REQUEST) :
        super.toJavaResult(
                usTarget.path( userId )
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get(), User.class);
    }
}