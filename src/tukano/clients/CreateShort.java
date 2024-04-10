package tukano.clients;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import tukano.api.Short;
import tukano.clients.rest.RestUsersClient;

public class CreateShort {
	private static Logger Log = Logger.getLogger(CreateUser.class.getName());

	public static void main(String[] args) throws IOException {
		
		if( args.length != 5) {
			System.err.println( "Use: java lab2.clients.CreateShort url userId password ");
			return;
		}
		
		String serverUrl = args[0];
		String userId = args[1];
		String password = args[2];
        //byte[] bytes = args[3];

		
		var client = new RestUsersClient( URI.create( serverUrl ) );
	}
	
}
