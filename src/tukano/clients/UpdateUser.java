package tukano.clients;

import java.io.IOException;
import java.util.logging.Logger;

import tukano.api.User;
import tukano.api.java.Users;

public class UpdateUser {
private static Logger Log = Logger.getLogger(UpdateUser.class.getName());

	public static void main(String[] args) throws IOException {
		
		if( args.length != 7) {
			System.err.println( "Use: java tukano.clients.CreateUser userId password new_userId new_password new_email new_displayName ");
			return;
		}
		
		String userId = args[0];
		String password = args[1];
        String new_userId = args[2];
		String new_password = args[3];
		String new_email = args[4];
		String new_displayName = args[5];
		
		new ClientFactory();
		Users client = ClientFactory.getClient(Users.NAME);
		
		User new_user = new User( new_userId, new_password, new_email, new_displayName);		
		var result = client.updateUser(userId, password, new_user);
		
		if( result.isOK() )
			Log.info("Updated user:" + result.value() );
		else
			Log.info("User update failed with error: " + result.error());
	}
}
