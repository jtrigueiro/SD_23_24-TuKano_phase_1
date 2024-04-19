package tukano.clients.grpc;

import java.util.function.Supplier;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ClientBuilder;

import tukano.utils.Sleep;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;

import io.grpc.Status;
import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import static tukano.api.java.Result.error;
import static tukano.api.java.Result.ok;

public class GrpcClient {
    
    protected static final int MAX_RETRIES = 3;
	protected static final int RETRY_SLEEP = 1000;
	protected static final int READ_TIMEOUT = 10000;
	protected static final int CONNECT_TIMEOUT = 10000;

	protected Client client;
	protected ClientConfig config;

	public GrpcClient() {
		this.config = new ClientConfig();

		config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
		config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

		this.client = ClientBuilder.newClient(config);
	}

    protected <T> Result<T> reTry(Supplier<Result<T>> func) {
		for (int i = 0; i < MAX_RETRIES; i++)
			try {
				return func.get();
			} catch (ProcessingException x) {
				Sleep.ms(RETRY_SLEEP);
			} catch (Exception x) {
				x.printStackTrace();
				return Result.error(Result.ErrorCode.INTERNAL_ERROR);
			}
		return Result.error(Result.ErrorCode.TIMEOUT);
	}

    static <T> Result<T> toJavaResult(Supplier<T> func) {
        try {
            return ok(func.get());
        } catch (StatusRuntimeException sre) {
            var code = sre.getStatus().getCode();
            if (code == Code.UNAVAILABLE || code == Code.DEADLINE_EXCEEDED)
                throw sre;
            return error(statusToErrorCode(sre.getStatus()));
        }
    }

    static Result<Void> toJavaResult(Runnable func) {
        try {
            func.run();
            return ok(null);
        } catch (StatusRuntimeException sre) {
            var code = sre.getStatus().getCode();
            if (code == Code.UNAVAILABLE || code == Code.DEADLINE_EXCEEDED)
                throw sre;
            return error(statusToErrorCode(sre.getStatus()));
        }
    }

    static ErrorCode statusToErrorCode(Status status) {
        return switch (status.getCode()) {
            case OK -> ErrorCode.OK;
            case NOT_FOUND -> ErrorCode.NOT_FOUND;
            case ALREADY_EXISTS -> ErrorCode.CONFLICT;
            case PERMISSION_DENIED -> ErrorCode.FORBIDDEN;
            case INVALID_ARGUMENT -> ErrorCode.BAD_REQUEST;
            case UNIMPLEMENTED -> ErrorCode.NOT_IMPLEMENTED;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }

}
