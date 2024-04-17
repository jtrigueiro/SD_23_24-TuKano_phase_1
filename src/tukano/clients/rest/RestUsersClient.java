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
import tukano.api.rest.RestBlobs;
import tukano.api.rest.RestShorts;
import tukano.api.rest.RestUsers;
import tukano.api.Short;

public class RestUsersClient extends RestClient implements Users {
	protected static final int READ_TIMEOUT = 10000;
	protected static final int CONNECT_TIMEOUT = 10000;

	final Client client;
	final ClientConfig config;

	final URI serverURI;
	private WebTarget target;

	public RestUsersClient(URI serverURI) {
		this.serverURI = serverURI;
		this.config = new ClientConfig();

		config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
		config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

		this.client = ClientBuilder.newClient(config);
	}

	private Result<String> clt_createUser(User user) {
		target = client.target(serverURI).path(RestUsers.PATH);

		return super.toJavaResult(
				target.request()
						.accept(MediaType.APPLICATION_JSON)
						.post(Entity.entity(user, MediaType.APPLICATION_JSON)),
				String.class);
	}

	private Result<User> clt_getUser(String userId, String pwd) {
		target = client.target(serverURI).path(RestUsers.PATH);

		return super.toJavaResult(
				target.path(userId)
						.queryParam(RestUsers.PWD, pwd).request()
						.accept(MediaType.APPLICATION_JSON)
						.get(),
				User.class);
	}

	private Result<User> clt_updateUser(String userId, String password, User user) {
		target = client.target(serverURI).path(RestUsers.PATH);

		return super.toJavaResult(
				target.path(userId)
						.queryParam(RestUsers.PWD, password)
						.request().accept(MediaType.APPLICATION_JSON)
						.put(Entity.entity(user, MediaType.APPLICATION_JSON)),
				User.class);
	}

	private Result<User> clt_deleteUser(String userId, String password) {
		target = client.target(serverURI).path(RestUsers.PATH);
		
		return super.toJavaResult(
				target.path(userId)
						.queryParam(RestUsers.PWD, password)
						.request()
						.delete(),
				User.class);
	}

	private Result<List<User>> clt_searchUsers(String pattern) {
		target = client.target(serverURI).path(RestUsers.PATH);

		return super.toJavaResult(
				target.queryParam(RestUsers.QUERY, pattern)
						.request(MediaType.APPLICATION_JSON)
						.get(),
				new GenericType<List<User>>() {
				});
	}

	private Result<Void> clt_createShort(String userId, String password, byte[] bytes) {
		target = client.target(serverURI).path(RestShorts.PATH);

		Result<Short> result = super.toJavaResult(
				target.path(userId)
						.queryParam(RestUsers.PWD, password)
						.request().post(null),
				Short.class);

		if (!result.isOK())
			return Result.error(result.error());

		Short s = result.value();
		URI blobURI = URI.create(s.getBlobUrl());
		WebTarget blob = client.target(blobURI);

		Result<Void> upload = super.toJavaResult(blob
				.path(s.getShortId())
				.request().accept(MediaType.APPLICATION_OCTET_STREAM)
				.post(Entity.entity(bytes, MediaType.APPLICATION_OCTET_STREAM)), Void.class);

		return upload.isOK() ? Result.ok() : Result.error(upload.error());
	}

	private Result<String> clt_checkBlobId(String blobId) {
		target = client.target(serverURI).path(RestShorts.PATH);

		return super.toJavaResult(
				target.path(blobId)
						.request()
						.accept(MediaType.APPLICATION_JSON)
						.get(),
				String.class);
	}

	private Result<Void> clt_deleteUserShorts(String userId) {
		target = client.target(serverURI).path(RestShorts.PATH);

		return super.toJavaResult(
				target.path(userId)
						.path(RestShorts.DELETES)
						.request()
						.delete(),
				Void.class);
	}

	private Result<Void> clt_deleteBlob(String blobId) {
		target = client.target(serverURI).path(RestBlobs.PATH);

		return super.toJavaResult(
				target.path(blobId)
						.request()
						.delete(),
				Void.class);
	}

	@Override
	public Result<String> createUser(User user) {
		return super.reTry(() -> clt_createUser(user));
	}

	@Override
	public Result<User> getUser(String userId, String pwd) {
		return super.reTry(() -> clt_getUser(userId, pwd));
	}

	@Override
	public Result<User> updateUser(String userId, String password, User user) {
		return super.reTry(() -> clt_updateUser(userId, password, user));
	}

	@Override
	public Result<User> deleteUser(String userId, String password) {
		return super.reTry(() -> clt_deleteUser(userId, password));
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {
		return super.reTry(() -> clt_searchUsers(pattern));
	}

	@Override
	public Result<Void> createShort(String userId, String password, byte[] bytes) {
		return super.reTry(() -> clt_createShort(userId, password, bytes));
	}

	@Override
	public Result<String> checkBlobId(String blobId) {
		return super.reTry(() -> clt_checkBlobId(blobId));
	}

	@Override
	public Result<Void> deleteUserShorts(String userId) {
		return super.reTry(() -> clt_deleteUserShorts(userId));
	}

	@Override
	public Result<Void> deleteBlob(String blobId) {
		return super.reTry(() -> clt_deleteBlob(blobId));
	}

}
