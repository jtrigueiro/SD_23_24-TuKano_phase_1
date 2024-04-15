package tukano.clients;

import java.io.IOException;
import java.util.logging.Logger;

import tukano.api.java.Users;

public class DeleteUser {
    private static Logger Log = Logger.getLogger(DeleteUser.class.getName());

	public static void main(String[] args) throws IOException {
		
		if( args.length != 2) {
			System.err.println( "Use: java tukano.clients.DeleteUser userId password");
			return;
		}
		
		String userId = args[0];
		String pwd = args[1];
		
		new ClientFactory();
		Users client = ClientFactory.getClient(Users.NAME);
		
		var result = client.deleteUser(userId, pwd);

		if( result.isOK()  )
			Log.info("Deleted user:" + result.value() );
		else
			Log.info("Delete user failed with error: " + result.error());
	}
}
