package tukano.impl.grpc.common;

import java.util.ArrayList;
import java.util.List;

import tukano.api.User;
import tukano.api.Short;
import tukano.impl.grpc.generated_java.ShortsProtoBuf.GrpcShort;
import tukano.impl.grpc.generated_java.UsersProtoBuf.GrpcUser;

public class DataModelAdaptor {

	public static User GrpcUser_to_User(GrpcUser from) {
		return new User(
				from.getUserId(),
				from.getPassword(),
				from.getEmail(),
				from.getDisplayName());
	}

	public static GrpcUser User_to_GrpcUser(User from) {
		return GrpcUser.newBuilder()
				.setUserId(from.getUserId())
				.setPassword(from.getPwd())
				.setEmail(from.getEmail())
				.setDisplayName(from.getDisplayName())
				.build();
	}

	public static List<GrpcUser> UserList_to_GrpcUserList(List<User> from) {
		List<GrpcUser> grpcUsersList = new ArrayList<>();
		for (User u : from)
			grpcUsersList.add(User_to_GrpcUser(u));
		return grpcUsersList;
	}

	public static GrpcShort Short_to_GrpcShort(Short from) {
		return GrpcShort.newBuilder()
				.setShortId(from.getShortId())
				.setOwnerId(from.getOwnerId())
				.setBlobUrl(from.getBlobUrl())
				.setTimestamp(from.getTimestamp())
				.setTotalLikes(from.getTotalLikes())
				.build();
	}
}
