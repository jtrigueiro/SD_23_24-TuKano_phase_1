package tukano.servers.java;

import java.net.URI;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

import tukano.api.User;
import tukano.api.Likes;
import tukano.api.Short;
import tukano.api.Follows;
import tukano.api.java.Users;
import tukano.api.rest.RestBlobs;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.utils.Discovery;
import tukano.utils.Hibernate;
import tukano.clients.ClientFactory;

public class ShortsServer implements Shorts {

    // Queries
    private static String shortByShortId = "SELECT s FROM Short s WHERE s.shortId = '%s'";
    private static String shortIdByOwnerId = "SELECT s.shortId FROM Short s WHERE s.ownerId = '%s'";
    private static String shortsByOwnerId = "SELECT s FROM Short s WHERE s.ownerId = '%s'";
    private static String followsByUserIds = "SELECT f FROM Follows f WHERE f.userId1 = '%s' AND f.userId2 = '%s'";
    private static String followersByUserId = "SELECT f.userId1 FROM Follows f WHERE f.userId2 = '%s'";
    private static String followingIdByUserId = "SELECT f.userId2 FROM Follows f WHERE f.userId1 = '%s'";
    private static String likeByUserIdAndShortId = "SELECT l FROM Likes l WHERE l.userId = '%s' AND l.shortId = '%s'";
    private static String likesIdByShortId = "SELECT l.userId FROM Likes l WHERE l.shortId = '%s'";
    private static String likesByShortId = "SELECT l FROM Likes l WHERE l.shortId = '%s'";
    private static String likesByUserId = "SELECT l FROM Likes l WHERE l.userId = '%s'";
    private static String followsByUserId = "SELECT f FROM Follows f WHERE f.userId2 = '%s' OR f.userId1 = '%s'";

    private URI[] blobServers;
    // To distribute the shorts among the blobs servers
    private int[] blobsShortsCounterMem;

    public ShortsServer() {
        blobServers = Discovery.getInstance().knownUrisOf("blobs", 1);
        blobsShortsCounterMem = new int[blobServers.length];
    }

    @Override
    public Result<Short> createShort(String userId, String password) {
        Users client = ClientFactory.getClient(Users.NAME);
        Result<User> uCheck = client.getUser(userId, password);

        // Check if the user exists and if the password is correct
        if (!uCheck.isOK())
            return Result.error(uCheck.error());

        Short s = new Short(userId, incrementBlobCounterAndGetURI());

        Hibernate.getInstance().persist(s);
        return Result.ok(s);
    }

    @Override
    public Result<Void> deleteShort(String shortId, String password) {
        List<Short> shorts = Hibernate.getInstance().jpql(String.format(shortByShortId, shortId), Short.class);

        // Check if the short exists
        if (shorts.isEmpty())
            return Result.error(Result.ErrorCode.NOT_FOUND);

        Short s = shorts.get(0);

        Users client = ClientFactory.getClient(Users.NAME);
        Result<User> uCheck = client.getUser(s.getOwnerId(), password);

        // Check if the user exists and if the password is correct
        if (!uCheck.isOK())
            return Result.error(uCheck.error());

        String shortenURL = s.getBlobUrl().split(RestBlobs.PATH)[0];

        Users client2 = ClientFactory.getClientURI(URI.create(shortenURL));
        Result<Void> deleteBlob = client2.deleteBlob(s.getShortId());

        // Check if the blob was deleted
        if (!deleteBlob.isOK() && !deleteBlob.error().equals(Result.ErrorCode.NOT_FOUND))
            return Result.error(deleteBlob.error());

        List<Likes> likes = Hibernate.getInstance().jpql(String.format(likesByShortId, s.getShortId()), Likes.class);

        for (Likes l : likes)
            Hibernate.getInstance().delete(l);

        decrementBlobCounter(shortenURL);

        Hibernate.getInstance().delete(s);
        return Result.ok();
    }

    @Override
    public Result<Short> getShort(String shortId) {
        List<Short> shorts = Hibernate.getInstance().jpql(String.format(shortByShortId, shortId), Short.class);

        // Check if the short exists
        if (shorts.isEmpty())
            return Result.error(Result.ErrorCode.NOT_FOUND);

        return Result.ok(shorts.get(0));
    }

    @Override
    public Result<List<String>> getShorts(String userId) {
        Users client = ClientFactory.getClient(Users.NAME);
        Result<User> uCheck = client.getUser(userId, "WrOnGpAsSwOrD");

        // Check if the user exists
        if (uCheck.error().equals(Result.ErrorCode.NOT_FOUND))
            return Result.error(Result.ErrorCode.NOT_FOUND);

        List<String> shorts = Hibernate.getInstance().jpql(String.format(shortIdByOwnerId, userId), String.class);

        return Result.ok(shorts);
    }

    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
        Users client = ClientFactory.getClient(Users.NAME);
        Result<User> u1Check = client.getUser(userId1, password);

        // Check if the first user exists and if the password is correct
        if (!u1Check.isOK())
            return Result.error(u1Check.error());

        Result<User> u2Check = client.getUser(userId1, "WrOnGpAsSwOrD");

        // Check if the second user exists
        if (u2Check.error().equals(Result.ErrorCode.NOT_FOUND))
            return Result.error(Result.ErrorCode.NOT_FOUND);

        List<Follows> follows = Hibernate.getInstance().jpql(String.format(followsByUserIds, userId1, userId2),
                Follows.class);

        if (isFollowing) {
            if (follows.isEmpty()) {
                Follows f = new Follows(userId1, userId2);
                Hibernate.getInstance().persist(f);

            } else
                return Result.error(Result.ErrorCode.CONFLICT);
        } else {
            if (!follows.isEmpty())
                Hibernate.getInstance().delete(follows.get(0));
        }

        return Result.ok();
    }

    @Override
    public Result<List<String>> followers(String userId, String password) {
        Users client = ClientFactory.getClient(Users.NAME);
        Result<User> uCheck = client.getUser(userId, password);

        // Check if the user exists and if the password is correct
        if (!uCheck.isOK())
            return Result.error(uCheck.error());

        List<String> followers = Hibernate.getInstance().jpql(String.format(followersByUserId, userId), String.class);
        return Result.ok(followers);
    }

    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
        Users client = ClientFactory.getClient(Users.NAME);
        Result<User> uCheck = client.getUser(userId, password);

        // Check if the user exists and if the password is correct
        if (!uCheck.isOK())
            return Result.error(uCheck.error());

        List<Short> shorts = Hibernate.getInstance().jpql(String.format(shortByShortId, shortId), Short.class);
        Short s = shorts.get(0);

        List<Likes> likes = Hibernate.getInstance().jpql(String.format(likeByUserIdAndShortId, userId, shortId),
                Likes.class);

        // Check if the user has already liked the short and if the user is trying to,
        // or vice versa
        if (likes.isEmpty() == !isLiked)
            return Result.error(Result.ErrorCode.CONFLICT);

        if (isLiked) {
            s.addLike();

            Likes l = new Likes(userId, shortId);
            Hibernate.getInstance().persist(l);

        } else {
            s.removeLike();

            Hibernate.getInstance().delete(likes.get(0));
        }

        Hibernate.getInstance().update(s);
        return Result.ok();
    }

    @Override
    public Result<List<String>> likes(String shortId, String password) {
        List<Short> shorts = Hibernate.getInstance().jpql(String.format(shortByShortId, shortId), Short.class);

        // Check if the short exists
        if (shorts.isEmpty())
            return Result.error(Result.ErrorCode.NOT_FOUND);

        Short s = shorts.get(0);

        Users client = ClientFactory.getClient(Users.NAME);
        Result<User> uCheck = client.getUser(s.getOwnerId(), password);

        // Check if the password is correct
        if (!uCheck.isOK())
            return Result.error(Result.ErrorCode.FORBIDDEN);

        List<String> likes = Hibernate.getInstance().jpql(String.format(likesIdByShortId, shortId), String.class);
        return Result.ok(likes);
    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {
        Users client = ClientFactory.getClient(Users.NAME);
        Result<User> uCheck = client.getUser(userId, password);

        // Check if the user exists and if the password is correct
        if (!uCheck.isOK())
            return Result.error(uCheck.error());

        List<String> following = Hibernate.getInstance().jpql(String.format(followingIdByUserId, userId), String.class);
        List<Short> feedShorts = new ArrayList<>();

        List<Short> ownShorts = Hibernate.getInstance().jpql(String.format(shortsByOwnerId, userId), Short.class);
        if (!ownShorts.isEmpty())
            feedShorts.addAll(ownShorts);

        if (!following.isEmpty()) {
            for (String f : following) {
                List<Short> shorts = Hibernate.getInstance().jpql(String.format(shortsByOwnerId, f), Short.class);

                if (!shorts.isEmpty()) {
                    for (Short s : shorts)
                        feedShorts.add(s);
                }
            }
        }

        feedShorts.sort(Comparator.naturalOrder());
        List<String> feed = new ArrayList<>();

        for (Short s : feedShorts)
            feed.add(s.getShortId());

        return Result.ok(feed);
    }

    @Override
    public Result<Void> deleteUserShorts(String userId) {
        List<Short> shorts = Hibernate.getInstance().jpql(String.format(shortsByOwnerId, userId), Short.class);

        // Deleting shorts and its likes and blobs
        for (Short s : shorts) {
            String shortenURL = s.getBlobUrl().split(RestBlobs.PATH)[0];

            Users client = ClientFactory.getClientURI(URI.create(shortenURL));
            Result<Void> delete = client.deleteBlob(s.getShortId());

            // Check if the blob was deleted
            if (!delete.isOK())
                return Result.error(delete.error());

            List<Likes> likes = Hibernate.getInstance().jpql(String.format(likesByShortId, s.getShortId()),
                    Likes.class);
            for (Likes l : likes)
                Hibernate.getInstance().delete(l);

            decrementBlobCounter(shortenURL);
            Hibernate.getInstance().delete(s);
        }

        List<Likes> ownLikes = Hibernate.getInstance().jpql(String.format(likesByUserId, userId), Likes.class);

        // Deleting user likes
        for (Likes l : ownLikes) {
            String shortId = l.getShortId();

            Short s = Hibernate.getInstance().jpql(String.format(shortByShortId, shortId), Short.class).get(0);
            s.removeLike();

            Hibernate.getInstance().update(s);
            Hibernate.getInstance().delete(l);
        }

        List<Follows> follows = Hibernate.getInstance().jpql(String.format(followsByUserId, userId, userId),
                Follows.class);

        // Deleting user following and followers
        for (Follows f : follows)
            Hibernate.getInstance().delete(f);

        return Result.ok();
    }

    private void decrementBlobCounter(String URI) {
        for (int i = 0; i < blobServers.length; i++) {
            if (blobServers[i].toString().equals(URI)) {
                blobsShortsCounterMem[i]--;
                break;
            }
        }
    }

    private String incrementBlobCounterAndGetURI() {
        int minimum = Integer.MAX_VALUE;
        int minimumPos = 0;
        for (int i = 0; i < blobsShortsCounterMem.length; i++) {
            if (blobsShortsCounterMem[i] < minimum) {
                minimum = blobsShortsCounterMem[i];
                minimumPos = i;
            }
        }

        blobsShortsCounterMem[minimumPos]++;
        return blobServers[minimumPos].toString();
    }

}