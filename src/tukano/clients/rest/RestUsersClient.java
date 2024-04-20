package tukano.clients.rest;

import java.net.URI;

import java.util.List;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.GenericType;

import tukano.api.User;
import tukano.api.java.Users;
import tukano.api.java.Result;
import tukano.api.rest.RestUsers;

public class RestUsersClient extends RestClient implements Users {

	private final WebTarget target;

	public RestUsersClient(URI serverURI) {
		super();
		target = client.target(serverURI).path(RestUsers.PATH);
	}

	private Result<String> clt_createUser(User user) {
		return super.toJavaResult(
				target.request()
						.accept(MediaType.APPLICATION_JSON)
						.post(Entity.entity(user, MediaType.APPLICATION_JSON)),
				String.class);
	}

	private Result<User> clt_getUser(String userId, String pwd) {
		return super.toJavaResult(
				target.path(userId)
						.queryParam(RestUsers.PWD, pwd).request()
						.accept(MediaType.APPLICATION_JSON)
						.get(),
				User.class);
	}

	private Result<User> clt_updateUser(String userId, String password, User user) {
		return super.toJavaResult(
				target.path(userId)
						.queryParam(RestUsers.PWD, password)
						.request().accept(MediaType.APPLICATION_JSON)
						.put(Entity.entity(user, MediaType.APPLICATION_JSON)),
				User.class);
	}

	private Result<User> clt_deleteUser(String userId, String password) {
		return super.toJavaResult(
				target.path(userId)
						.queryParam(RestUsers.PWD, password)
						.request()
						.delete(),
				User.class);
	}

	private Result<List<User>> clt_searchUsers(String pattern) {
		return super.toJavaResult(
				target.queryParam(RestUsers.QUERY, pattern)
						.request(MediaType.APPLICATION_JSON)
						.get(),
				new GenericType<List<User>>() {
				});
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

}
