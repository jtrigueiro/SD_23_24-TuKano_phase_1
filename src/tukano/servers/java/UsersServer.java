package tukano.servers.java;

import java.util.ArrayList;
import java.util.List;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.api.java.Shorts;
import tukano.utils.Hibernate;
import tukano.api.User;
import tukano.api.java.Users;
import tukano.clients.ClientFactory;

public class UsersServer extends RestServer implements Users {
	protected static final int READ_TIMEOUT = 10000;
	protected static final int CONNECT_TIMEOUT = 10000;

	private static String queryUserId = "SELECT u FROM User u WHERE u.userId = '%s'";
	private static String queryAll = "SELECT u FROM User u";

	final Client client;
	final ClientConfig config;

	// private URI[] shortsServer;
	// private WebTarget ssTarget;

	public UsersServer() {
		this.config = new ClientConfig();

		config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
		config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

		this.client = ClientBuilder.newClient(config);

		// shortsServer = discovery.knownUrisOf("shorts", 1);
		// ssTarget = client.target(shortsServer[0]).path(RestShorts.PATH);
	}

	@Override
	public Result<String> createUser(User user) {

		// Check if user data is valid
		if (user.userId() == null || user.pwd() == null || user.displayName() == null || user.email() == null)
			return Result.error(ErrorCode.BAD_REQUEST);

		var result = hibernateQuery(String.format(queryUserId, user.userId()), User.class);

		if (result.isOK() && !result.value().isEmpty())
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
		if (!result.isOK() || result.value().isEmpty())
			return Result.error(ErrorCode.NOT_FOUND);

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

		if (!result.isOK() || result.value().isEmpty())
			return Result.error(ErrorCode.NOT_FOUND);

		User dbUser = result.value().get(0);

		// Check if the password is correct
		if (!dbUser.getPwd().equals(pwd))
			return Result.error(ErrorCode.FORBIDDEN);

		if (user.getUserId() != null && !user.getUserId().equals(userId))
			return Result.error(ErrorCode.BAD_REQUEST);
		if (user.getPwd() != null)
			dbUser.setPwd(user.getPwd());
		if (user.getEmail() != null)
			dbUser.setEmail(user.getEmail());
		if (user.getDisplayName() != null)
			dbUser.setDisplayName(user.getDisplayName());

		// Update the user
		Hibernate.getInstance().update(dbUser);
		return Result.ok(dbUser);
	}

	@Override
	public Result<User> deleteUser(String userId, String pwd) {

		// Check if user is valid
		if (userId == null || pwd == null)
			return Result.error(ErrorCode.BAD_REQUEST);

		var result = hibernateQuery(String.format(queryUserId, userId), User.class);

		// Query error
		if (!result.isOK() || result.value().isEmpty())
			return Result.error(ErrorCode.NOT_FOUND);

		User user = result.value().get(0);

		// Check if the password is correct
		if (!user.getPwd().equals(pwd))
			return Result.error(ErrorCode.FORBIDDEN);

		Users client = ClientFactory.getClient(Shorts.NAME);
		Result<Void> result2 = client.deleteUserShorts(userId);

		if (!result2.isOK())
			return Result.error(result2.error());

		// Delete the user
		Hibernate.getInstance().delete(user);
		return Result.ok(user);
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {

		var result = hibernateQuery(queryAll, User.class);

		// Query error
		if (!result.isOK())
			return Result.error(ErrorCode.BAD_REQUEST);

		List<User> users = result.value();
		List<User> matchingUsers = new ArrayList<>();

		for (User user : users) {
			if (user.getUserId().toLowerCase().contains(pattern.toLowerCase()))
				matchingUsers.add(user);
		}

		return Result.ok(matchingUsers);
	}

	// nao é usado aqui
	@Override
	public Result<Void> createShort(String userId, String password, byte[] bytes) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'createShort'");
	}

	@Override
	public Result<String> checkBlobId(String blobId) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'checkBlobId'");
	}

	public Result<User> checkUserId(String userId) {

		// Check if user is valid
		if (userId == null)
			return Result.error(ErrorCode.BAD_REQUEST);

		var result = hibernateQuery(String.format(queryUserId, userId), User.class);

		// Query error
		if (!result.isOK() || result.value().isEmpty())
			return Result.error(ErrorCode.NOT_FOUND);

		User user = result.value().get(0);

		return Result.ok(user);
	}

	@Override
	public Result<Void> deleteUserShorts(String userId) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'deleteUserShorts'");
	}

	@Override
	public Result<Void> deleteBlob(String blobId) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'deleteBlob'");
	}

}
