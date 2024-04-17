package tukano.clients;

import java.net.URI;

import tukano.api.java.Users;
import tukano.clients.rest.RestUsersClient;
import tukano.clients.grpc.GrpcUsersClient;

import tukano.utils.Discovery;

public class ClientFactory {
     
   public static Users getClient(String service) {
      var serverURI = Discovery.getInstance().knownUrisOf(service, 1)[0];
      if( serverURI.toString().endsWith("rest") )
         return new RestUsersClient( serverURI );
      else
         return new GrpcUsersClient( serverURI );
   }

   public static Users getClientURI( URI serverURI ) {
      if( serverURI.toString().endsWith("rest") )
         return new RestUsersClient( serverURI );
      else
         return new GrpcUsersClient( serverURI );
   }
}
