package tukano.servers.java;

import java.util.HashMap;
import java.util.Map;

import java.util.logging.Logger;

import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;


public class BlobServer implements Blobs{
    private final Map<String, byte[]> blobs = new HashMap<>();
    private static Logger Log = Logger.getLogger(BlobServer.class.getName());

    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        Log.info("BlobServer: uploading blob " + blobId);
        
        if(blobId == null || false ){//verificar com o sv de shorts?
            Log.info("BlobServer: blob id is invalid or already exists.");
            return Result.error(ErrorCode.FORBIDDEN);
        }
        
        // If the blob exists but the short is different, i.e. it's not a duplicate, give an error
        if(blobs.containsKey(blobId) && !blobs.values().contains(bytes)){
            return Result.error(ErrorCode.CONFLICT);
        }
       
        // If there is no error and it's a new blob add it to the map
        if(!blobs.containsKey(blobId)){
            blobs.put(blobId, bytes);
            Log.info("BlobServer: uploaded blob " + blobId);
        }
        
        return Result.ok();
    }

    @Override
    public Result<byte[]> download(String blobId) {
        if(!blobs.containsKey(blobId)){
            return Result.error(ErrorCode.NOT_FOUND);
        }
        return Result.ok(blobs.get(blobId));
    }

}

