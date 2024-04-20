package tukano.servers.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
<<<<<<< Updated upstream
=======

import tukano.api.Short;
>>>>>>> Stashed changes
import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.clients.ClientFactory;


public class BlobServer implements Blobs {

    private final Path storagePath;
<<<<<<< Updated upstream
    // private final Map<String, byte[]> blobs;

    public BlobServer() {
        // this.blobs = new HashMap<>();

        storagePath = Paths.get("src/tukano/servers/java/blobs");
        try {
            Files.createDirectories(storagePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
=======

    public BlobServer() {
        storagePath = Paths.get("src/tukano/servers/java/blobs");

        try {
            Files.createDirectories(storagePath);

        } catch (IOException e) {
            throw new RuntimeException(e);

>>>>>>> Stashed changes
        }
    }

    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        Shorts client = ClientFactory.getShortsClient();
        Result<Short> bCheck = client.getShort(blobId);

        // Check if the blobId is verified
        if (!bCheck.isOK())
            return Result.error(Result.ErrorCode.FORBIDDEN);
    
        Path filePath = storagePath.resolve(blobId);

<<<<<<< Updated upstream
        // // Check if the blobId is already in use with a different value
        // if (blobs.containsKey(blobId) && !blobs.values().contains(bytes))
        // return Result.error(Result.ErrorCode.CONFLICT);

        // // Check if the blobId is not in use
        // if (!blobs.containsKey(blobId))
        // blobs.put(blobId, bytes);

        Path filePath = storagePath.resolve(blobId); // Generate file path

        if (Files.exists(filePath)) {
            byte[] fileBytes;
            try {
                fileBytes = Files.readAllBytes(filePath);
            } catch (IOException e) {
                return Result.error(Result.ErrorCode.CONFLICT);
            }

            if (fileBytes.length != bytes.length) {
                Result.error(Result.ErrorCode.CONFLICT);
            }

            for (int i = 0; i < fileBytes.length; i++) {
                if (fileBytes[i] != bytes[i]) {
                    return Result.error(Result.ErrorCode.CONFLICT); // Byte mismatch found
                }
=======
        if (Files.exists(filePath)) {
            byte[] fileBytes;

            try {
                fileBytes = Files.readAllBytes(filePath);

            } catch (IOException e) {
                return Result.error(Result.ErrorCode.CONFLICT);

            }

            if (fileBytes.length != bytes.length)
                Result.error(Result.ErrorCode.CONFLICT);

            for (int i = 0; i < fileBytes.length; i++) {

                // Check for bytes mismatch
                if (fileBytes[i] != bytes[i])
                    return Result.error(Result.ErrorCode.CONFLICT);
>>>>>>> Stashed changes
            }

        } else {
            try {
<<<<<<< Updated upstream
                Files.write(filePath, bytes); // Write bytes to file
            } catch (IOException e) {
                return Result.error(Result.ErrorCode.CONFLICT);
=======
                Files.write(filePath, bytes);

            } catch (IOException e) {
                return Result.error(Result.ErrorCode.CONFLICT);

>>>>>>> Stashed changes
            }
        }
        return Result.ok();
    }

    @Override
    public Result<byte[]> download(String blobId) {
        Path filePath = storagePath.resolve(blobId);

<<<<<<< Updated upstream
        // Check if the blobId exists
        // if (!blobs.containsKey(blobId))
        // return Result.error(Result.ErrorCode.NOT_FOUND);

        // return Result.ok(blobs.get(blobId));
        Path filePath = storagePath.resolve(blobId);
=======
>>>>>>> Stashed changes
        if (Files.exists(filePath)) {
            try {
                byte[] data = Files.readAllBytes(filePath); // Read bytes from file
                return Result.ok(data);
<<<<<<< Updated upstream
            } catch (IOException e) {
                return Result.error(Result.ErrorCode.CONFLICT);
            }
        } else
            return Result.error(Result.ErrorCode.NOT_FOUND);

=======

            } catch (IOException e) {
                return Result.error(Result.ErrorCode.CONFLICT);
                
            }
        } else
            return Result.error(Result.ErrorCode.NOT_FOUND);
>>>>>>> Stashed changes
    }


    @Override
    public Result<Void> delete(String blobId) {
        Path filePath = storagePath.resolve(blobId);

<<<<<<< Updated upstream
        // Check if the blobId exists
        // if (!blobs.containsKey(blobId))
        // return Result.error(Result.ErrorCode.NOT_FOUND);

        // blobs.remove(blobId);

        Path filePath = storagePath.resolve(blobId);
        if (filePath.toFile().exists()) {
            try {
                Files.delete(filePath);
            } catch (IOException e) {
                return Result.error(Result.ErrorCode.CONFLICT);
            }
        } else {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }
=======
        if (filePath.toFile().exists()) {
            try {
                Files.delete(filePath);

            } catch (IOException e) {
                return Result.error(Result.ErrorCode.CONFLICT);

            }
        } else
            return Result.error(Result.ErrorCode.NOT_FOUND);

>>>>>>> Stashed changes
        return Result.ok();
    }

}
