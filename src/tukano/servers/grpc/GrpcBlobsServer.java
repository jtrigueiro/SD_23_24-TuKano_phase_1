package tukano.servers.grpc;

import java.net.InetAddress;

import io.grpc.ServerBuilder;

import tukano.utils.Discovery;

public class GrpcBlobsServer {
    public static final int PORT = 8080;
    public static final String SERVICE = "blobs";
    private static final String SERVER_URI_FMT = "grpc://%s:%s%s";
	private static final String GRPC_CTX = "/gprc";


    public static void main(String[] args) throws Exception {

        var stub = new GrpcBlobsServerStub();
        var server = ServerBuilder.forPort(PORT).addService(stub).build();
        var serverURI = String.format(SERVER_URI_FMT, InetAddress.getLocalHost().getHostAddress(), PORT, GRPC_CTX);

        Discovery discovery = Discovery.getInstance();
        discovery.announce(SERVICE, serverURI);
        server.start();
        server.awaitTermination();
    }
}
