package tukano.servers.rest;

import java.net.URI;
import jakarta.inject.Singleton;

import tukano.servers.java.BlobServer;
import tukano.api.java.Blobs;
import tukano.api.rest.RestBlobs;

@Singleton
public class RestBlobsResource extends RestResource implements RestBlobs {

    final Blobs impl;

    public RestBlobsResource(URI serverURI) {
        this.impl = new BlobServer();
    }

    @Override
    public void upload(String blobId, byte[] bytes) {
        resultOrThrow(impl.upload(blobId, bytes));
    }

    @Override
    public byte[] download(String blobId) {
        return resultOrThrow(impl.download(blobId));
    }

}