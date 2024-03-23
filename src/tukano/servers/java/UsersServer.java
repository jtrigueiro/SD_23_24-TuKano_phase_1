package tukano.servers.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.persistence.Hibernate;
import tukano.api.User;
import tukano.api.java.Users;

public class UsersServer implements Users {
	private final Map<String,User> users = new HashMap<>();

	private static Logger Log = Logger.getLogger(UsersServer.class.getName());

	@Override
	public Result<String> createUser(User user) {
		Log.info("createUser : " + user);
		
		// Check if user data is valid
		if(user.userId() == null || user.pwd() == null || user.displayName() == null || user.email() == null) {
			Log.info("User object invalid.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}
		String query = String.format("SELECT u FROM User u WHERE u.userId = '%s'", user.userId());
		var result = Hibernate.getInstance().jpql(query, User.class);

		// Insert user, checking if name already exists
		//if( users.putIfAbsent(user.userId(), user) != null ) {
		if(!result.isEmpty()){
			Log.info("User already exists.");
			return Result.error( ErrorCode.CONFLICT);
		}
		Hibernate.getInstance().persist(user);
		return Result.ok( user.userId() );
	}

	@Override
	public Result<User> getUser(String userId, String pwd) {
		Log.info("getUser : user = " + userId + "; pwd = " + pwd);
		
		// Check if user is valid
		if(userId == null || pwd == null) {
			Log.info("Name or Password null.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}
		
		User user = users.get(userId);			
		// Check if user exists 
		if( user == null ) {
			Log.info("User does not exist.");
			return Result.error( ErrorCode.NOT_FOUND);
		}
		
		//Check if the password is correct
		if( !user.pwd().equals( pwd)) {
			Log.info("Password is incorrect.");
			return Result.error( ErrorCode.FORBIDDEN);
		}
		
		return Result.ok(user);
	}

	@Override
	public Result<User> updateUser(String userId, String pwd, User user) {
		Log.info("updateUser : user = " + userId + "; pwd = " + pwd + "; updatedUser = " + user);

		// Check if user is valid
		if (userId == null || pwd == null || user == null) {
			Log.info("Invalid user data.");
			return Result.error(ErrorCode.BAD_REQUEST);
		}

		// Check if user exists
		if (!users.containsKey(userId)) {
			Log.info("User does not exist.");
			return Result.error(ErrorCode.NOT_FOUND);
		}

		// Check if the password is correct
		User existingUser = users.get(userId);
		if (!existingUser.pwd().equals(pwd)) {
			Log.info("Password is incorrect.");
			return Result.error(ErrorCode.FORBIDDEN);
		}

		// Update the user 
		users.remove(userId);
		users.put(userId, user);
		
		return Result.ok(existingUser);
	}

	@Override
	public Result<User> deleteUser(String userId, String pwd) {
		Log.info("deleteUser : user = " + userId + "; pwd = " + pwd);

		// Check if user is valid
		if (userId == null || pwd == null) {
			Log.info("Invalid user data.");
			return Result.error(ErrorCode.BAD_REQUEST);
		}

		// Check if user exists
		if (!users.containsKey(userId)) {
			Log.info("User does not exist.");
			return Result.error(ErrorCode.NOT_FOUND);
		}

		// Check if the password is correct
		User existingUser = users.get(userId);
		if (!existingUser.pwd().equals(pwd)) {
			Log.info("Password is incorrect.");
			return Result.error(ErrorCode.FORBIDDEN);
		}

		// Delete the user
		users.remove(userId);

		return Result.ok(existingUser);
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {
			List<User> matchingUsers = new ArrayList<>();

		for (User user : users.values()) {
			if (user.displayName().contains(pattern)) {
				matchingUsers.add(user);
			}
		}
		
		return Result.ok(matchingUsers);
	}

	@Override
	public Result<Void> checkPassword(String userId, String pwd) {
		Log.info("checkPassword : user = " + userId + "; pwd = " + pwd);

		// Check if user is valid
		if (userId == null || pwd == null) {
			Log.info("Invalid user data.");
			return Result.error(ErrorCode.BAD_REQUEST);
		}

		// Check if user exists
		if (!users.containsKey(userId)) {
			Log.info("User does not exist.");
			return Result.error(ErrorCode.NOT_FOUND);
		}

		// Check if the password is correct
		User existingUser = users.get(userId);
		if (!existingUser.pwd().equals(pwd)) {
			Log.info("Password is incorrect.");
			return Result.error(ErrorCode.FORBIDDEN);
		}
		
		return Result.ok();
	}
}
