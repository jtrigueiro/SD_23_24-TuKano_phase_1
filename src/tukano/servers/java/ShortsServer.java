package tukano.servers.java;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import tukano.api.Short;
import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.api.java.Shorts;
import tukano.api.rest.RestUsers;
import tukano.api.Discovery;

import java.util.logging.Logger;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import tukano.persistence.Hibernate;

public class ShortsServer implements Shorts {

    protected static final int READ_TIMEOUT = 5000;
    protected static final int CONNECT_TIMEOUT = 5000;
    protected static final int MAX_RETRIES = 10;
    protected static final int RETRY_SLEEP = 5000;

    final WebTarget target;
    final URI serverURI;
    final Client client;
    final ClientConfig config;

    private static Logger Log = Logger.getLogger(ShortsServer.class.getName());
    private static Discovery discovery;
    private URI usersServer;
    private URI[] blobServers;

    public ShortsServer(URI serverURI) {
        this.serverURI = serverURI;

        this.config = new ClientConfig();

        config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

        this.client = ClientBuilder.newClient(config);

        target = client.target(serverURI).path(RestUsers.PATH);

        discovery = Discovery.getInstance();
        discovery.announce("ShortsServer", serverURI.toString());

        usersServer = discovery.knownUrisOf("UsersServer", 1)[0];
        blobServers = discovery.knownUrisOf("BlobServer", 3);
    }

    @Override
    public Result<User> checkUserIdAndPassword(String userId, String pwd) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Response r = target.path(userId)
                        .queryParam(RestUsers.PWD, pwd).request()
                        .accept(MediaType.APPLICATION_JSON)
                        .get();

                var status = r.getStatus();
                if (status != Status.OK.getStatusCode())
                    return Result.error(getErrorCodeFrom(status));
                else
                    return Result.ok(r.readEntity(User.class));

            } catch (ProcessingException x) {
                Log.info(x.getMessage());

                utils.Sleep.ms(RETRY_SLEEP);
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return Result.error(ErrorCode.TIMEOUT);
    }

    @Override
    public Result<Short> createShort(String userId, String password) {
        // Check if user is valid
        if (userId == null || password == null) {
            Log.info("Invalid user data.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }
        // TODO: evitar fazer estes ifs quando já se sabe o erro e o problema é o tipo
        // do objeto
        Result<User> result = checkUserIdAndPassword(userId, password);
        if (result.error() == ErrorCode.BAD_REQUEST) {
            return Result.error(ErrorCode.BAD_REQUEST);
        } else if (result.error() == ErrorCode.NOT_FOUND) {
            return Result.error(ErrorCode.NOT_FOUND);
        } else if (result.error() == ErrorCode.FORBIDDEN) {
            return Result.error(ErrorCode.FORBIDDEN);
        }
        // TODO: criar o URL do blob
        Short s = new Short(UUID.randomUUID().toString(), userId, "blobUrl", Instant.now().toEpochMilli());
        Hibernate.getInstance().persist(s);

        return Result.ok(s);
    }

    @Override
    public Result<Void> deleteShort(String shortId, String password) {
        String query = String.format("SELECT s FROM Short s WHERE s.shortId = '%s'", shortId);
        var result = Hibernate.getInstance().jpql(query, Short.class);

        if (result.isEmpty()) {
            return Result.error(ErrorCode.NOT_FOUND);
        }

        Short s = result.get(0);
        Result<User> check = checkUserIdAndPassword(s.getOwnerId(), password);

        if (check.error() == ErrorCode.BAD_REQUEST) {
            return Result.error(ErrorCode.BAD_REQUEST);
        } else if (check.error() == ErrorCode.FORBIDDEN) {
            return Result.error(ErrorCode.FORBIDDEN);
        }

        Hibernate.getInstance().delete(s);
        return Result.ok();
    }

    @Override
    public Result<Short> getShort(String shortId) {
        String query = String.format("SELECT s FROM Short s WHERE s.shortId = '%s'", shortId);
        var result = Hibernate.getInstance().jpql(query, Short.class);

        if (result.isEmpty()) {
            return Result.error(ErrorCode.NOT_FOUND);
        }

        return Result.ok(result.get(0));
    }

    @Override
    public Result<List<String>> getShorts(String userId) {

        Result<User> check = checkUserIdAndPassword(userId, "invalidPassword");
        // FIX ME: código esparguete?
        if (check.error() == ErrorCode.NOT_FOUND) {
            return Result.error(ErrorCode.NOT_FOUND);
        }

        String query = String.format("SELECT s.shortId FROM Short s WHERE s.ownerId = '%s'", userId); // Isto está
                                                                                                      // certo?
        var result = Hibernate.getInstance().jpql(query, String.class);

        return Result.ok(result);
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

    public static ErrorCode getErrorCodeFrom(int status) {
        return switch (status) {
            case 200, 209 -> ErrorCode.OK;
            case 409 -> ErrorCode.CONFLICT;
            case 403 -> ErrorCode.FORBIDDEN;
            case 404 -> ErrorCode.NOT_FOUND;
            case 400 -> ErrorCode.BAD_REQUEST;
            case 500 -> ErrorCode.INTERNAL_ERROR;
            case 501 -> ErrorCode.NOT_IMPLEMENTED;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }
}
