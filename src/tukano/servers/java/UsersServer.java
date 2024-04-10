package tukano.servers.java;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import jakarta.ws.rs.client.Client;
//import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.client.ClientBuilder;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
//import tukano.api.rest.RestShorts;
import tukano.persistence.Hibernate;
import tukano.api.User;
import tukano.api.java.Users;
import tukano.api.Discovery;

public class UsersServer extends RestServer implements Users {
	protected static final int READ_TIMEOUT = 5000;
    protected static final int CONNECT_TIMEOUT = 5000;

	private static String queryUserId = "SELECT u FROM User u WHERE u.userId = '%s'";
	private static String queryAll = "SELECT u FROM User u";

	final Client client;
    final ClientConfig config;

	private static Discovery discovery;
	final URI serverURI;

	//private URI[] shortsServer;
	//private WebTarget ssTarget;
	
	public UsersServer(URI serverURI) {
		this.serverURI = serverURI;
		this.config = new ClientConfig();

        config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

		this.client = ClientBuilder.newClient(config);

		discovery = Discovery.getInstance();
		discovery.announce("UsersService", serverURI.toString());

		//shortsServer = discovery.knownUrisOf("ShortsService", 1);

		//ssTarget = client.target(shortsServer[0]).path(RestShorts.PATH);
	}

	@Override
	public Result<String> createUser(User user) {

		// Check if user data is valid
		if (user.userId() == null || user.pwd() == null || user.displayName() == null || user.email() == null)
			return Result.error(ErrorCode.BAD_REQUEST);

		var result = hibernateQuery(String.format(queryUserId, user.userId()), User.class);

		// Query error
		if(result.error() != ErrorCode.OK)
			return Result.error(result.error());

		// Insert user, checking if name already exists
		if (!result.value().isEmpty())
			return Result.error(ErrorCode.CONFLICT);

		Hibernate.getInstance().persist(user);
		return Result.ok(user.userId());
	}

	@Override
	public Result<User> getUser(String userId, String pwd) {

		// Check if user is valid
		if (userId == null || pwd == null)
			return Result.error(ErrorCode.BAD_REQUEST);

		var result = hibernateQuery(String.format(queryUserId, userId), User.class);

		// Query error
		if(result.error() != ErrorCode.OK)
			return Result.error(result.error());

		User user = result.value().get(0);

		// Check if the password is correct
		if (!user.getPwd().equals(pwd))
			return Result.error(ErrorCode.FORBIDDEN);

		return Result.ok(user);
	}

	@Override
	public Result<User> updateUser(String userId, String pwd, User user) {

		// Check if user is valid
		if (userId == null || pwd == null || user == null)
			return Result.error(ErrorCode.BAD_REQUEST);

		var result = hibernateQuery(String.format(queryUserId, userId), User.class);

		// Query error
		if(result.error() != ErrorCode.OK)
			return Result.error(result.error());

		User dbUser = result.value().get(0);

		// Check if the password is correct
		if (!dbUser.getPwd().equals(pwd))
			return Result.error(ErrorCode.FORBIDDEN);

		// Update the user
		Hibernate.getInstance().update(user);
		return Result.ok(user);
	}

	@Override
	public Result<User> deleteUser(String userId, String pwd) {

		// Check if user is valid
		if (userId == null || pwd == null)
			return Result.error(ErrorCode.BAD_REQUEST);

		var result = hibernateQuery(String.format(queryUserId, userId), User.class);

		// Query error
		if(result.error() != ErrorCode.OK)
			return Result.error(result.error());

		User user = result.value().get(0);

		// Check if the password is correct
		if (!user.getPwd().equals(pwd))
			return Result.error(ErrorCode.FORBIDDEN);

		// Delete the user
		Hibernate.getInstance().delete(user);
		return Result.ok(user);
	}
	
	
	@Override
	public Result<List<User>> searchUsers(String pattern) {

		var result = hibernateQuery(queryAll, User.class);

		// Query error
		if(result.error() != ErrorCode.OK)
			return Result.error(result.error());

		List<User> users = result.value();
		List<User> matchingUsers = new ArrayList<>();

		for (User user : users) {
			if (user.displayName().contains(pattern))
				matchingUsers.add(user);
		}
		
		return Result.ok(matchingUsers);
	}

// -------------------------NAO USADO??-------------------------------
	 /*
	 * @Override
	 * public Result<Void> checkPassword(String userId, String pwd) {
	 * Log.info("checkPassword : user = " + userId + "; pwd = " + pwd);
	 * 
	 * // Check if user is valid
	 * if (userId == null || pwd == null) {
	 * Log.info("Invalid user data.");
	 * return Result.error(ErrorCode.BAD_REQUEST);
	 * }
	 * 
	 * // Check if user exists
	 * if (!users.containsKey(userId)) {
	 * Log.info("User does not exist.");
	 * return Result.error(ErrorCode.NOT_FOUND);
	 * }
	 * 
	 * // Check if the password is correct
	 * User existingUser = users.get(userId);
	 * if (!existingUser.pwd().equals(pwd)) {
	 * Log.info("Password is incorrect.");
	 * return Result.error(ErrorCode.FORBIDDEN);
	 * }
	 * 
	 * return Result.ok();
	 * }
	 */
}
