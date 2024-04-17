package tukano.servers.rest;

import java.util.List;

import jakarta.inject.Singleton;

import tukano.api.User;
import tukano.api.java.Users;
import tukano.api.rest.RestUsers;
import tukano.servers.java.UsersServer;

@Singleton
public class RestUsersResource extends RestResource implements RestUsers {

	final Users impl;

	public RestUsersResource() {
		this.impl = new UsersServer();
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

}
