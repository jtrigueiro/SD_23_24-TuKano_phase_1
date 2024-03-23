package tukano.clients;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import tukano.api.User;
import tukano.clients.rest.RestUsersClient;

public class UpdateUser {
private static Logger Log = Logger.getLogger(UpdateUser.class.getName());

	public static void main(String[] args) throws IOException {
		
		if( args.length != 7) {
			System.err.println( "Use: java lab2.clients.CreateUser url userId password new_userId new_password new_email new_displayName ");
			return;
		}
		
		String serverUrl = args[0];
		String userId = args[1];
		String password = args[2];
        String new_userId = args[3];
		String new_password = args[4];
		String new_email = args[5];
		String new_displayName = args[6];
		
		var client = new RestUsersClient( URI.create( serverUrl ) );
		
		var new_user = new User( new_userId, new_password, new_email, new_displayName);		
		
		var result = client.updateUser(userId, password, new_user);
		if( result.isOK()  )
			Log.info("Updated user:" + result.value() );
		else
			Log.info("User update failed with error: " + result.error());
	}
}
