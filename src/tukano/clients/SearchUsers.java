package tukano.clients;

import java.io.IOException;
import java.util.logging.Logger;

import tukano.api.java.Users;

public class SearchUsers {
	private static Logger Log = Logger.getLogger(SearchUsers.class.getName());

	public static void main(String[] args) throws IOException {
	
		if( args.length != 1) {
			System.err.println( "Use: java tukano.clients.GetUser pattern");
			return;
		}
		
		String pattern = args[0];
		
		Users client = ClientFactory.getClient(Users.NAME);

		var result = client.searchUsers(pattern);

		if( result.isOK() )
			Log.info("Search users:" + result.value() );
		else
			Log.info("Search users failed with error: " + result.error());
		
	}

}
