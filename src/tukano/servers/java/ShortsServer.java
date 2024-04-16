package tukano.servers.java;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import tukano.api.Follows;
import tukano.api.Short;
import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.api.java.Shorts;
import tukano.api.java.Users;
import tukano.api.rest.RestBlobs;
import tukano.api.rest.RestUsers;
import tukano.clients.ClientFactory;
import tukano.utils.Discovery;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import tukano.utils.Hibernate;
import java.util.logging.Logger;


public class ShortsServer extends RestServer implements Shorts {

    protected static final int READ_TIMEOUT = 5000;
    protected static final int CONNECT_TIMEOUT = 5000;

    final Client client;
    final ClientConfig config;

    private static String queryShortId = "SELECT s FROM Short s WHERE s.shortId = '%s'";
    private static String queryOwnerId = "SELECT s FROM Short s WHERE s.ownerId = '%s'";
    private static String queryFollows = "SELECT f FROM Follows f WHERE f.userId1 = '%s' AND f.userId2 = '%s'";
    private static String queryFollowers = "SELECT f.userId1 FROM Follows f WHERE f.userId2 = '%s'";
    private static String queryFollowing = "SELECT f.userId2 FROM Follows f WHERE f.userId1 = '%s'";
    private static Discovery discovery;
    
    private URI[] blobServers;

    private int currentBlob;
    private HashSet<String> verifiers;
    private Logger log = Logger.getLogger(ShortsServer.class.getName());

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

        if (result.error() != ErrorCode.OK)
            return Result.error(result.error());

        Short s = result.value().get(0);
        
        Users client = ClientFactory.getClient(Users.NAME);
        Result<User> check = client.getUser(s.getOwnerId(), password);

        if (!check.isOK())
            return Result.error(check.error());

        Hibernate.getInstance().delete(s);
        return Result.ok();
    }

    @Override
    public Result<Short> getShort(String shortId) {
        var result = hibernateQuery(String.format(queryShortId, shortId), Short.class);

        if(!result.isOK() || result.value().isEmpty())
            return Result.error(Result.ErrorCode.NOT_FOUND);

        return Result.ok(result.value().get(0));
    }

    @Override
    public Result<List<String>> getShorts(String userId) {
        Users client = ClientFactory.getClient(Users.NAME);
        Result<User> result = client.getUser(userId, "");

        if(result.error().equals(Result.ErrorCode.NOT_FOUND))
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

        Result<User> check2 = client.getUser(userId1, "");

        if (check2.error().equals(Result.ErrorCode.NOT_FOUND))
            return Result.error(Result.ErrorCode.NOT_FOUND);

        Result<List<Follows>> follows = hibernateQuery(String.format(queryFollows, userId1, userId2), Follows.class);

        if(isFollowing) {
            if(follows.isOK() && follows.value().isEmpty()) {
                Follows f = new Follows(userId1, userId2);
                Hibernate.getInstance().persist(f);
            }
        }
        else {
            if(follows.isOK() && !follows.value().isEmpty())
                Hibernate.getInstance().delete(follows.value().get(0));
        }

        return Result.ok();
    }

    @Override
    public Result<List<String>> followers(String userId, String password) {
        Users client = ClientFactory.getClient(Users.NAME);
        Result<User> result = client.getUser(userId, password);

        if(!result.isOK())
            return Result.error(result.error());

        Result<List<String>> follows = hibernateQuery(String.format(queryFollowers, userId), String.class);
        return Result.ok(follows.value());
    }

    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
        Users client = ClientFactory.getClient(Users.NAME);
        Result<User> check = client.getUser(userId, password);

        if (!check.isOK())
            return Result.error(check.error());

        Result<List<Short>> check2 = hibernateQuery(String.format(queryShortId, shortId), Short.class);

        if (!check2.isOK())
            return Result.error(check2.error());

        User u = check.value();
        Short s = check2.value().get(0);

        if (isLiked) {
            return null;
            //Result<List<Likes>> result = hibernateQuery(String.format(queryLikes, userId, shortId), Likes.class);
        } else {
            u.unlike(shortId);
            //s.removeLike(userId);
        }

        Hibernate.getInstance().update(s);
        return null;
        //return updateUser(userId, password, u);
    }

    @Override
    public Result<List<String>> likes(String shortId, String password) {
        var shortResult = hibernateQuery(String.format(queryShortId, shortId), Short.class);

        if (shortResult.error() != ErrorCode.OK)
            return Result.error(shortResult.error());

        /*
        Result<User> check = checkUserIdAndPassword(shortResult.value().get(0).getOwnerId(), password);
        if (check.error() != ErrorCode.OK)
            return Result.error(check.error());

        Short s = shortResult.value().get(0);
        return Result.ok(s.getLikes());*/
        return null;
    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {
        log.info("INSIIIIDEEEEEEEEEEEEEEEEEEEEEEEEE FEEEEEEEEEEEEEEEEEEEEEEEEEEEEED");
        Users client = ClientFactory.getClient(Users.NAME);
        Result<User> check = client.getUser(userId, password);

        if (!check.isOK())
            return Result.error(check.error());

        log.info("BEFOREEEEEEEEEEE FOLLOWINGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG");
        Result<List<String>> following = hibernateQuery(String.format(queryFollowing, userId), String.class);
        List<Short> shorts = new ArrayList<>();

        log.info("BEFOREEEEEEEEEEEEEEEEEEEE OWNNEEEEEEEEEEEEEEEEEEEEEERRRRRRRRRRRRRRRR");
        Result<List<Short>> own = hibernateQuery(String.format(queryOwnerId, userId), Short.class);
        if(own.isOK() && !own.value().isEmpty())
        log.info("LOOOK HEEEEEEEEEEEREEEEEEEEEEEE 77777777777777777");
            shorts.addAll(own.value());

        if(following.isOK() && !following.value().isEmpty()) {
            for (String f : following.value()) {
                if(f.equals(userId))
                    continue;
                
                log.info("LOOOK HEEEEEEEEEEEEEEREEEEEEEEEEEE 0000000000000000000");
                var result = hibernateQuery(String.format(queryOwnerId, f), Short.class);

                if(!result.value().isEmpty()) {
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

}