package tukano.clients.rest;

import java.net.URI;
import java.util.List;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import tukano.api.java.Result;
import tukano.api.User;
import tukano.api.java.Users;
import tukano.api.rest.RestUsers;
import tukano.api.Discovery;


public class RestUsersClient extends RestClient implements Users {
	protected static final int READ_TIMEOUT = 5000;
	protected static final int CONNECT_TIMEOUT = 5000;

	final Client client;
	final ClientConfig config;

	final WebTarget usTarget, ssTarget;
	final URI[] usersServer, shortsServer, blobServers;
	private static Discovery discovery;

	public RestUsersClient() {
		this.config = new ClientConfig();

		config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
		config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

		this.client = ClientBuilder.newClient(config);

		discovery = Discovery.getInstance();
		usersServer = discovery.knownUrisOf("UsersServer", 1);
		shortsServer = discovery.knownUrisOf("ShortsServer", 1);
		blobServers = discovery.knownUrisOf("BlobServer", 3);

		usTarget = client.target(usersServer[0]).path(RestUsers.PATH);
		ssTarget = client.target(shortsServer[0]).path(RestUsers.PATH);
	}

	private Result<String> clt_createUser(User user) {
    	return super.toJavaResult( 
    		usTarget.request()
    		.accept(MediaType.APPLICATION_JSON)
    		.post(Entity.entity(user, MediaType.APPLICATION_JSON)), String.class );
    }

	private Result<User> clt_getUser(String userId, String pwd) {
    	return super.toJavaResult(
    			usTarget.path( userId )
    			.queryParam(RestUsers.PWD, pwd).request()
    			.accept(MediaType.APPLICATION_JSON)
    			.get(), User.class);
    }

	private Result<User> clt_updateUser(String userId, String password, User user) {
		return super.toJavaResult(
				usTarget.path(userId)
				.queryParam(RestUsers.PWD, password)
				.request()
				.put(Entity.entity(user, MediaType.APPLICATION_JSON)), User.class);
	}

	private Result<User> clt_deleteUser(String userId, String password) {
		return super.toJavaResult(
				usTarget.path(userId)
				.queryParam(RestUsers.PWD, password)
				.request()
				.delete(), User.class);
	}

	private Result<List<User>> clt_searchUsers(String pattern) {
		return super.toJavaResult(
				usTarget.queryParam(RestUsers.QUERY, pattern)
				.request(MediaType.APPLICATION_JSON)
				.get(), new GenericType<List<User>>() {});
	}

	@Override
	public Result<String> createUser(User user) {
		return super.reTry( () -> clt_createUser(user));
	}

	@Override
    public Result<User> getUser(String userId, String pwd) {
    	return super.reTry( () -> clt_getUser(userId, pwd));
    }

	@Override
	public Result<User> updateUser(String userId, String password, User user) {
		return super.reTry( () -> clt_updateUser(userId, password, user));
	}

	@Override
	public Result<User> deleteUser(String userId, String password) {
		return super.reTry( () -> clt_deleteUser(userId, password));
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {
		return super.reTry( () -> clt_searchUsers(pattern));
	}
}
