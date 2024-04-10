package tukano.api;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class User {
	
	@Id
	private String userId;
	private String displayName;
	private String pwd;
	private String email;
	private Set<String> following;
	private Set<String> followers;
	private Set<String> likedShorts;

	public User() {
	}

	public User(String userId, String pwd, String email, String displayName) {
		this.pwd = pwd;
		this.email = email;
		this.userId = userId;
		this.displayName = displayName;
		this.following = new HashSet<>();
		this.followers = new HashSet<>();
		this.likedShorts = new HashSet<>();
	}

	public void follow(String userId) {
		if(!following.contains(userId))
			following.add(userId);
	}

	public void unfollow(String userId) {
		if(following.contains(userId))
			following.remove(userId);
	}

	public void addFollower(String userId) {
		if(!followers.contains(userId))
			followers.add(userId);
	}

	public void removeFollower(String userId) {
		if(followers.contains(userId))
			followers.remove(userId);
	}

	public void like(String shortId) {
		if(!likedShorts.contains(shortId))
			likedShorts.add(shortId);
	}

	public void unlike(String shortId) {
		if(likedShorts.contains(shortId))
			likedShorts.remove(shortId);
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String userId() {
		return userId;
	}

	public String pwd() {
		return pwd;
	}

	public String email() {
		return email;
	}

	public String displayName() {
		return displayName;
	}

	public List<String> following() {
		return List.copyOf(following);
	}

	public List<String> followers() {
		return List.copyOf(followers);
	}

	@Override
	public String toString() {
		return "User [userId=" + userId + ", pwd=" + pwd + ", email=" + email + ", displayName=" + displayName + "]";
	}
}
