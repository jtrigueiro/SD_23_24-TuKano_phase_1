package tukano.clients;

import java.util.logging.Logger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.api.java.Users;

public class CreateShort {
	private static Logger Log = Logger.getLogger(CreateUser.class.getName());

	public static void main(String[] args) throws IOException {
		
		if( args.length != 3) {
			System.err.println( "Use: java tukano.clients.CreateShort userId password bytes");
			return;
		}
		
		String userId = args[0];
		String password = args[1];
		byte[] bytes = Files.readAllBytes( Paths.get( args[3] ) );

		new ClientFactory();
		Users client = ClientFactory.getClient(Shorts.NAME);
		
		Result<Void> result = client.createShort(userId, password, bytes);

		if( result.isOK() ) {
			Log.info( "Short created successfully" );
		} else {
			Log.info( "Error: " + result.error() );
		}
	}
	
}
