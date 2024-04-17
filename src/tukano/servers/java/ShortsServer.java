package tukano.servers.java;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import tukano.api.Follows;
import tukano.api.Likes;
import tukano.api.Short;
import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.api.java.Shorts;
import tukano.api.java.Users;
import tukano.clients.ClientFactory;
import tukano.utils.Discovery;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import tukano.utils.Hibernate;

public class ShortsServer extends RestServer implements Shorts {

    protected static final int READ_TIMEOUT = 10000;
    protected static final int CONNECT_TIMEOUT = 10000;

    final Client client;
    final ClientConfig config;

    private static String queryShortId = "SELECT s FROM Short s WHERE s.shortId = '%s'";
    private static String queryOwnerId = "SELECT s.shortId FROM Short s WHERE s.ownerId = '%s'";
    private static String queryShortsByOwnerId = "SELECT s FROM Short s WHERE s.ownerId = '%s'";
    private static String queryFollows = "SELECT f FROM Follows f WHERE f.userId1 = '%s' AND f.userId2 = '%s'";
    private static String queryFollowers = "SELECT f.userId1 FROM Follows f WHERE f.userId2 = '%s'";
    private static String queryFollowing = "SELECT f.userId2 FROM Follows f WHERE f.userId1 = '%s'";
    private static String queryLike = "SELECT l FROM Likes l WHERE l.userId = '%s' AND l.shortId = '%s'";
    private static String queryLikes = "SELECT l.userId FROM Likes l WHERE l.shortId = '%s'";
    private static String queryLikesShortId = "SELECT l FROM Likes l WHERE l.shortId = '%s'";
    private static String queryLikesUserId = "SELECT l FROM Likes l WHERE l.userId = '%s'";
    private static String queryDeleteFollows = "SELECT f FROM Follows f WHERE f.userId2 = '%s' OR f.userId1 = '%s'";
    private static Discovery discovery;

    private URI[] blobServers;

    private int currentBlob;
    private HashSet<String> verifiers;

    public ShortsServer() {
        this.config = new ClientConfig();

        config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

        this.client = ClientBuilder.newClient(config);

        discovery = Discovery.getInstance();
        blobServers = discovery.knownUrisOf("blobs", 1);

        currentBlob = 0;
        verifiers = new HashSet<>();
    }

    @Override
    public Result<Short> createShort(String userId, String password) {
        Users client = ClientFactory.getClient(Users.NAME);
        Result<User> result = client.getUser(userId, password);

        if (!result.isOK())
            return Result.error(result.error());

        Short s = new Short(userId, getCurrentBlobURI());
        verifiers.add(s.getShortId());

        Hibernate.getInstance().persist(s);
        return Result.ok(s);
    }

    @Override
    public Result<Void> deleteShort(String shortId, String password) {
        var result = hibernateQuery(String.format(queryShortId, shortId), Short.class);

        if (result.value().isEmpty())
            return Result.error(ErrorCode.NOT_FOUND);

        Short s = result.value().get(0);

        Users client = ClientFactory.getClient(Users.NAME);
        Result<User> check = client.getUser(s.getOwnerId(), password);

        if (!check.isOK())
            return Result.error(check.error());

        String URL = s.getBlobUrl().split("/blobs")[0];

        Users client2 = ClientFactory.getClientURI(URI.create(URL));
        Result<Void> delete = client2.deleteBlob(s.getShortId());

        if (!delete.isOK() && !delete.error().equals(Result.ErrorCode.NOT_FOUND))
            return Result.error(delete.error());

        var likes = hibernateQuery(String.format(queryLikesShortId, s.getShortId()), Likes.class);
        for (Likes l : likes.value())
            Hibernate.getInstance().delete(l);

        Hibernate.getInstance().delete(s);
        return Result.ok();
    }

    @Override
    public Result<Short> getShort(String shortId) {
        var result = hibernateQuery(String.format(queryShortId, shortId), Short.class);

        if (!result.isOK() || result.value().isEmpty())
            return Result.error(Result.ErrorCode.NOT_FOUND);

        return Result.ok(result.value().get(0));
    }

    @Override
    public Result<List<String>> getShorts(String userId) {
        Users client = ClientFactory.getClient(Users.NAME);
        Result<User> result = client.getUser(userId, "WrOnGpAsSwOrD");

        if (result.error().equals(Result.ErrorCode.NOT_FOUND))
            return Result.error(Result.ErrorCode.NOT_FOUND);

        var shorts = hibernateQuery(String.format(queryOwnerId, userId), String.class);

        return Result.ok(shorts.value());
    }

    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
        Users client = ClientFactory.getClient(Users.NAME);
        Result<User> check1 = client.getUser(userId1, password);

        if (!check1.isOK())
            return Result.error(check1.error());

        Result<User> check2 = client.getUser(userId1, "WrOnGpAsSwOrD");

        if (check2.error().equals(Result.ErrorCode.NOT_FOUND))
            return Result.error(Result.ErrorCode.NOT_FOUND);

        Result<List<Follows>> follows = hibernateQuery(String.format(queryFollows, userId1, userId2), Follows.class);

        if (isFollowing) {
            if (follows.value().isEmpty()) {
                Follows f = new Follows(userId1, userId2);
                Hibernate.getInstance().persist(f);
            } else
                return Result.error(Result.ErrorCode.CONFLICT);
        } else {
            if (!follows.value().isEmpty())
                Hibernate.getInstance().delete(follows.value().get(0));
        }

        return Result.ok();
    }

    @Override
    public Result<List<String>> followers(String userId, String password) {
        Users client = ClientFactory.getClient(Users.NAME);
        Result<User> result = client.getUser(userId, password);

        if (!result.isOK())
            return Result.error(result.error());

        Result<List<String>> follows = hibernateQuery(String.format(queryFollowers, userId), String.class);
        return Result.ok(follows.value());
    }

    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
        Users client = ClientFactory.getClient(Users.NAME);
        Result<User> check1 = client.getUser(userId, password);

        if (!check1.isOK())
            return Result.error(check1.error());

        Result<List<Short>> check2 = hibernateQuery(String.format(queryShortId, shortId), Short.class);

        if (!check2.isOK())
            return Result.error(check2.error());

        Result<List<Likes>> result = hibernateQuery(String.format(queryLike, userId, shortId), Likes.class);
        Short s = check2.value().get(0);

        if (result.value().isEmpty() == !isLiked)
            return Result.error(Result.ErrorCode.CONFLICT);

        if (isLiked) {
            s.addLike();

            Likes l = new Likes(userId, shortId);
            Hibernate.getInstance().persist(l);

        } else {
            s.removeLike();

            Hibernate.getInstance().delete(result.value().get(0));
        }

        Hibernate.getInstance().update(s);
        return Result.ok();
    }

    @Override
    public Result<List<String>> likes(String shortId, String password) {
        var check1 = hibernateQuery(String.format(queryShortId, shortId), Short.class);

        if (check1.value().isEmpty())
            return Result.error(Result.ErrorCode.NOT_FOUND);

        Short s = check1.value().get(0);

        Users client = ClientFactory.getClient(Users.NAME);
        Result<User> check2 = client.getUser(s.getOwnerId(), password);

        if (!check2.isOK())
            return Result.error(Result.ErrorCode.FORBIDDEN);

        var likes = hibernateQuery(String.format(queryLikes, shortId), String.class);
        return Result.ok(likes.value());
    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {
        Users client = ClientFactory.getClient(Users.NAME);
        Result<User> check = client.getUser(userId, password);

        if (!check.isOK())
            return Result.error(check.error());

        Result<List<String>> following = hibernateQuery(String.format(queryFollowing, userId), String.class);
        List<Short> shorts = new ArrayList<>();

        Result<List<Short>> own = hibernateQuery(String.format(queryShortsByOwnerId, userId), Short.class);
        if (own.isOK() && !own.value().isEmpty())
            shorts.addAll(own.value());

        if (following.isOK() && !following.value().isEmpty()) {
            for (String f : following.value()) {
                Result<List<Short>> result = hibernateQuery(String.format(queryShortsByOwnerId, f), Short.class);

                if (!result.value().isEmpty()) {
                    for (Short s : result.value())
                        shorts.add(s);
                }
            }
        }

        shorts.sort(Comparator.naturalOrder());
        List<String> feed = new ArrayList<>();

        for (Short s : shorts)
            feed.add(s.getShortId());

        return Result.ok(feed);
    }

    private String getCurrentBlobURI() {
        return blobServers[currentBlob++ % blobServers.length].toString();
    }

    @Override
    public Result<Void> deleteUserShorts(String userId) {
        var result = hibernateQuery(String.format(queryShortsByOwnerId, userId), Short.class);

        for (Short s : result.value()) {
            String URL = s.getBlobUrl().split("/blobs")[0];

            Users client = ClientFactory.getClientURI(URI.create(URL));
            Result<Void> delete = client.deleteBlob(s.getShortId());

            if (!delete.isOK())
                return Result.error(delete.error());

            var likes = hibernateQuery(String.format(queryLikesShortId, s.getShortId()), Likes.class);
            for (Likes l : likes.value())
                Hibernate.getInstance().delete(l);

            Hibernate.getInstance().delete(s);
        }

        Result<List<Likes>> userLikes = hibernateQuery(String.format(queryLikesUserId, userId), Likes.class);
        for (Likes l : userLikes.value()) {
            String shortId = l.getShortId();

            Short s = hibernateQuery(String.format(queryShortId, shortId), Short.class).value().get(0);
            s.removeLike();
            Hibernate.getInstance().update(s);
            Hibernate.getInstance().delete(l);
        }

        // hibernateQuery(String.format(queryDeleteFollows, userId, userId),
        // Void.class);

        Result<List<Follows>> userFollows = hibernateQuery(String.format(queryDeleteFollows, userId, userId),
                Follows.class);
        for (Follows f : userFollows.value())
            Hibernate.getInstance().delete(f);

        /*
         * userFollows = hibernateQuery(String.format(queryFollowing, userId),
         * Follows.class);
         * for (Follows f : userFollows.value())
         * Hibernate.getInstance().delete(f);
         */
        return Result.ok();
    }

}