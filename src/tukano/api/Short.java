package tukano.api;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;
import java.util.List;

/**
 * Represents a Short video uploaded by an user.
 * 
 * A short has an unique shortId and is owned by a given user;
 * Comprises of a short video, stored as a binary blob at some bloburl;.
 * A post also has a number of likes, which can increase or decrease over time.
 * It is the only piece of information that is mutable.
 * A short is timestamped when it is created.
 *
 */
@Entity
public class Short implements Comparable<Short>{
	@Id
	private String shortId;
	private String ownerId;
	private String blobUrl;
	private long timestamp;
	private int likes;

	public Short() {
	}

	public Short(String ownerId, String blobUrl) {
		this.shortId = UUID.randomUUID().toString();
		this.ownerId = ownerId;
		this.blobUrl = blobUrl;
		this.timestamp = System.currentTimeMillis();
		this.likes = 0;
	}

	public Short(String shortId, String ownerId, String blobUrl, long timestamp, int likes) {
		this.shortId = shortId;
		this.ownerId = ownerId;
		this.blobUrl = blobUrl;
		this.timestamp = timestamp;
		this.likes = likes;
	}

	@Override
	public int compareTo(Short o2) {
		return Long.compare(this.timestamp, o2.getTimestamp());
	}

	/*
	public void addLike(String userId) {
		if(!likes.contains(userId))
			likes.add(userId);
	}

	public void removeLike(String userId) {
		likes.remove(userId);
	}*/

	@Override
	public String toString() {
		return "Short [shortId=" + shortId + ", ownerId=" + ownerId + ", blobUrl=" + blobUrl + ", timestamp="
				+ timestamp + ", totalLikes=" + likes + "]";
	}

	public Short copyOf() {
		return new Short(shortId, ownerId, blobUrl, timestamp, likes);
	}

	// GETTERS AND SETTERS

	public String getShortId() {
		return shortId;
	}

	public void setShortId(String shortId) {
		this.shortId = shortId;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public String getBlobUrl() {
		return blobUrl;
	}

	public void setBlobUrl(String blobUrl) {
		this.blobUrl = blobUrl;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/*
	public int getTotalLikes() {
		return likes.size();
	}

	public List<String> getLikes() {
		return List.copyOf(likes);
	}
*/
}