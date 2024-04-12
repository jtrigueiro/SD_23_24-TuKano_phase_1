package tukano.clients;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;
import java.nio.file.Files;
import java.nio.file.Paths;

import tukano.api.java.Result;
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
		byte[] bytes = Files.readAllBytes( Paths.get( args[3] ) );

		
		var client = new RestUsersClient( URI.create( serverUrl ) );
		Result<Void> result = client.createShort(userId, password, bytes);

		if( result.isOK() ) {
			Log.info( "Short created successfully" );
		} else {
			Log.info( "Error: " + result.error() );
		}
	}
	
}
