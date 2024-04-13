package tukano.servers.rest;

import java.util.List;
import java.net.URI;

import jakarta.inject.Singleton;

import tukano.servers.java.UsersServer;
import tukano.api.java.Users;
import tukano.api.rest.RestUsers;
import tukano.api.User;

@Singleton
public class RestUsersResource extends RestResource implements RestUsers {

	final Users impl;

	public RestUsersResource(URI serverURI) {
		this.impl = new UsersServer(serverURI);
	}

	@Override
	public String createUser(User user) {
		return resultOrThrow(impl.createUser(user));
	}

	@Override
	public User getUser(String userId, String pwd) {
		return resultOrThrow(impl.getUser(userId, pwd));
	}

	@Override
	public User updateUser(String name, String pwd, User user) {
		return resultOrThrow(impl.updateUser(name, pwd, user));
	}

	@Override
	public User deleteUser(String name, String pwd) {
		return resultOrThrow(impl.deleteUser(name, pwd));
	}

	@Override
	public List<User> searchUsers(String pattern) {
		return resultOrThrow(impl.searchUsers(pattern));
	}

	@Override
	public User checkUserId(String userId) {
		return resultOrThrow(impl.checkUserId(userId));
	}

}
