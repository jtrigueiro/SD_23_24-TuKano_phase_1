package tukano.clients;

import java.io.IOException;
import java.util.logging.Logger;

import tukano.api.User;
import tukano.api.java.Users;

public class CreateUser {
	private static Logger Log = Logger.getLogger(CreateUser.class.getName());

	public static void main(String[] args) throws IOException {
		
		if( args.length != 4) {
			System.err.println( "Use: java tukano.clients.CreateUser userId password email displayName ");
			return;
		}
		
		String userId = args[0];
		String password = args[1];
		String email = args[2];
		String displayName = args[3];
		
		Users client = ClientFactory.getClient(Users.NAME);
		
		var user = new User( userId, password, email, displayName);		
		var result = client.createUser( user );

		if( result.isOK()  )
			Log.info("Created user:" + result.value() );
		else
			Log.info("Create user failed with error: " + result.error());
	}
	
}
