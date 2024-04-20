package tukano.clients;

import java.net.URI;

import tukano.api.java.Blobs;
import tukano.api.java.Shorts;
import tukano.api.java.Users;
import tukano.utils.Discovery;
import tukano.clients.rest.RestBlobsClient;
import tukano.clients.rest.RestShortsClient;
import tukano.clients.rest.RestUsersClient;
import tukano.clients.grpc.GrpcBlobsClient;
import tukano.clients.grpc.GrpcShortsClient;
import tukano.clients.grpc.GrpcUsersClient;

public class ClientFactory {
     
   public static Users getUsersClient() {
      var serverURI = Discovery.getInstance().knownUrisOf(Users.NAME, 1)[0];
      
      if( serverURI.toString().endsWith("rest") )
         return new RestUsersClient( serverURI );
      else
         return new GrpcUsersClient( serverURI );
   }

   public static Shorts getShortsClient() {
      var serverURI = Discovery.getInstance().knownUrisOf(Shorts.NAME, 1)[0];

      if( serverURI.toString().endsWith("rest") )
         return new RestShortsClient( serverURI );
      else
         return new GrpcShortsClient( serverURI ); 
   }

   public static Blobs getBlobsClient( URI serverURI ) {

      if( serverURI.toString().endsWith("rest") )
         return new RestBlobsClient( serverURI );
      else
         return new GrpcBlobsClient( serverURI );
   }
}
