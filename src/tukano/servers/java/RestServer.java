package tukano.servers.java;

import java.util.function.Supplier;
import java.util.List;
import tukano.utils.Hibernate;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ProcessingException;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.utils.Sleep;

public class RestServer {
	protected static final int MAX_RETRIES = 3;
	protected static final int RETRY_SLEEP = 1000;

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

	protected <T> Result<T> toJavaResult(Response r, Class<T> entityType) {
		try {
			var status = r.getStatusInfo().toEnum();
			if (status == Response.Status.OK && r.hasEntity())
				return Result.ok(r.readEntity(entityType));
			else if (status == Response.Status.NO_CONTENT)
				return Result.ok();

			return Result.error(getErrorCodeFrom(status.getStatusCode()));
		} finally {
			r.close();
		}
	}

	protected <T> Result<T> toJavaResult(Response r, GenericType<T> entityType) {
		try {
			var status = r.getStatusInfo().toEnum();
			if (status == Response.Status.OK && r.hasEntity())
				return Result.ok(r.readEntity(entityType));
			else if (status == Response.Status.NO_CONTENT)
				return Result.ok();

			return Result.error(getErrorCodeFrom(status.getStatusCode()));
		} finally {
			r.close();
		}
	}

	protected <T> Result<List<T>> hibernateQuery(String query, Class<T> clazz) {
		var result = Hibernate.getInstance().jpql(query, clazz);

		return Result.ok(result);
	}

	private static ErrorCode getErrorCodeFrom(int status) {
		return switch (status) {
			case 200, 209 -> ErrorCode.OK;
			case 409 -> ErrorCode.CONFLICT;
			case 403 -> ErrorCode.FORBIDDEN;
			case 404 -> ErrorCode.NOT_FOUND;
			case 400 -> ErrorCode.BAD_REQUEST;
			case 500 -> ErrorCode.INTERNAL_ERROR;
			case 501 -> ErrorCode.NOT_IMPLEMENTED;
			default -> ErrorCode.INTERNAL_ERROR;
		};
	}
}
