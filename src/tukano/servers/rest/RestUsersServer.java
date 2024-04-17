package tukano.servers.rest;

import java.net.URI;
import java.net.InetAddress;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;


import tukano.utils.Discovery;

public class RestUsersServer {

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
	}

	public static final int PORT = 8080;
	public static final String SERVICE = "users";
	private static final String SERVER_URI_FMT = "http://%s:%s/rest";

	public static void main(String[] args) {

		try {

			ResourceConfig config = new ResourceConfig();
			config.register(  RestUsersResource.class );
			
			String ip = InetAddress.getLocalHost().getHostAddress();
			String serverURI = String.format(SERVER_URI_FMT, ip, PORT);
			JdkHttpServerFactory.createHttpServer(URI.create(serverURI), config);	
			
			Discovery discovery = Discovery.getInstance();
			discovery.announce(SERVICE, serverURI.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
}
