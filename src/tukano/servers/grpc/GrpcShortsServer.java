package tukano.servers.grpc;

import java.net.InetAddress;

import io.grpc.ServerBuilder;

import tukano.utils.Discovery;

public class GrpcShortsServer {
    public static final int PORT = 8080;
    public static final String SERVICE = "shorts";
    private static final String SERVER_URI_FMT = "http://%s:%s/grpc";

    public static void main(String[] args) throws Exception {

        var stub = new GrpcShortsServerStub();
        var server = ServerBuilder.forPort(PORT).addService(stub).build();
        var serverURI = String.format(SERVER_URI_FMT, InetAddress.getLocalHost().getHostAddress(), PORT);

        server.start();
        server.awaitTermination();

        Discovery discovery = Discovery.getInstance();
        discovery.announce(SERVICE, serverURI);
    }
}
