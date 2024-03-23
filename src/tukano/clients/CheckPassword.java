package tukano.clients;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import tukano.clients.rest.RestUsersClient;

public class CheckPassword {
     private static Logger Log = Logger.getLogger(CheckPassword.class.getName());

	public static void main(String[] args) throws IOException {
		
		if( args.length != 3) {
			System.err.println( "Use: java lab2.clients.CheckPassword url userId password");
			return;
		}
		
		String serverUrl = args[0];
		String userId = args[1];
		String pwd = args[2];
		
		var client = new RestUsersClient( URI.create( serverUrl ) );
		
		
		var result = client.checkPassword(userId, pwd);
		if( result.isOK()  )
			Log.info("Checked user password:" + result.value() );
		else
			Log.info("User password check failed with error: " + result.error());
	}
}
