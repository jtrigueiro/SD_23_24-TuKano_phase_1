package tukano.servers.java;

import java.util.ArrayList;
import java.util.List;

import tukano.api.User;
import tukano.api.java.Users;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.utils.Hibernate;
import tukano.clients.ClientFactory;

public class UsersServer implements Users {

	private static String userByUserId = "SELECT u FROM User u WHERE u.userId = '%s'";
	private static String allUsers = "SELECT u FROM User u";

	public UsersServer() {
	}

	@Override
	public Result<String> createUser(User user) {

		if (user.userId() == null || user.pwd() == null || user.displayName() == null || user.email() == null)
			return Result.error(Result.ErrorCode.BAD_REQUEST);

		List<User> uCheck = Hibernate.getInstance().jpql(String.format(userByUserId, user.userId()), User.class);

		// Check if the user already exists
		if (!uCheck.isEmpty())
			return Result.error(Result.ErrorCode.CONFLICT);

		Hibernate.getInstance().persist(user);
		return Result.ok(user.userId());
	}

	@Override
	public Result<User> getUser(String userId, String pwd) {

		if (userId == null || pwd == null)
			return Result.error(Result.ErrorCode.BAD_REQUEST);

		List<User> users = Hibernate.getInstance().jpql(String.format(userByUserId, userId), User.class);

		// Check if the user exists
		if (users.isEmpty())
			return Result.error(Result.ErrorCode.NOT_FOUND);

		User user = users.get(0);

		// Check if the password is correct
		if (!user.pwd().equals(pwd))
			return Result.error(Result.ErrorCode.FORBIDDEN);

		return Result.ok(user);
	}

	@Override
	public Result<User> updateUser(String userId, String pwd, User newUser) {

		if (userId == null || pwd == null || newUser == null)
			return Result.error(Result.ErrorCode.BAD_REQUEST);

		List<User> users = Hibernate.getInstance().jpql(String.format(userByUserId, userId), User.class);

		// Check if the user exists
		if (users.isEmpty())
			return Result.error(Result.ErrorCode.NOT_FOUND);

		User user = users.get(0);

		// Check if the password is correct
		if (!user.getPwd().equals(pwd))
			return Result.error(Result.ErrorCode.FORBIDDEN);

		// Check if the user is trying to change the userId
		if (newUser.getUserId() != null && !newUser.getUserId().equals(userId))
			return Result.error(Result.ErrorCode.BAD_REQUEST);

		user.update(newUser);

		Hibernate.getInstance().update(user);
		return Result.ok(user);
	}

	@Override
	public Result<User> deleteUser(String userId, String pwd) {

		if (userId == null || pwd == null)
			return Result.error(Result.ErrorCode.BAD_REQUEST);

		List<User> users = Hibernate.getInstance().jpql(String.format(userByUserId, userId), User.class);

		// Check if the user exists
		if (users.isEmpty())
			return Result.error(Result.ErrorCode.NOT_FOUND);

		User user = users.get(0);

		// Check if the password is correct
		if (!user.getPwd().equals(pwd))
			return Result.error(Result.ErrorCode.FORBIDDEN);

		// Delete the user's shorts
		Users client = ClientFactory.getClient(Shorts.NAME);
		Result<Void> deleteShorts = client.deleteUserShorts(userId);

		if (!deleteShorts.isOK())
			return Result.error(deleteShorts.error());

		Hibernate.getInstance().delete(user);
		return Result.ok(user);
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {

		List<User> users = Hibernate.getInstance().jpql(allUsers, User.class);
		List<User> matchingUsers = new ArrayList<>();

		for (User user : users) {
			if (user.getUserId().toLowerCase().contains(pattern.toLowerCase()))
				matchingUsers.add(user);
		}

		return Result.ok(matchingUsers);
	}

	// ------------------- Unimplemented methods -------------------

	@Override
	public Result<Void> createShort(String userId, String password, byte[] bytes) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'createShort' in UsersServer.java");
	}

	@Override
	public Result<String> checkBlobId(String blobId) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'checkBlobId' in UsersServer.java");
	}

	@Override
	public Result<Void> deleteUserShorts(String userId) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'deleteUserShorts' in UsersServer.java");
	}

	@Override
	public Result<Void> deleteBlob(String blobId) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'deleteBlob' in UsersServer.java");
	}

}
