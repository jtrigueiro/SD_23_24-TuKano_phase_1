package tukano.clients;

import java.io.IOException;
import java.util.logging.Logger;

import tukano.api.java.Users;

public class GetUser {
	private static Logger Log = Logger.getLogger(GetUser.class.getName());

	public static void main(String[] args) throws IOException {
		
		if( args.length != 2) {
			System.err.println( "Use: java tukano.clients.GetUser userId password");
			return;
		}
		
		String userId = args[0];
		String pwd = args[1];
		
		Users client = ClientFactory.getClient(Users.NAME);
		
		var result = client.getUser(userId, pwd);
		
		if( result.isOK()  )
			Log.info("Get user:" + result.value() );
		else
			Log.info("Get user failed with error: " + result.error());
	}
	
}
