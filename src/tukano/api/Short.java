package tukano.api;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

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
public class Short {
	@Id
	String shortId;
	String ownerId;
	String blobUrl;
	long timestamp;
	int totalLikes;

	public Short() {
	}

	public Short(String shortId, String ownerId, String blobUrl, long timestamp) {
		this.shortId = shortId;
		this.ownerId = ownerId;
		this.blobUrl = blobUrl;
		this.timestamp = timestamp;
		this.totalLikes = 0;
	}

	public String getShortId() {
		return shortId;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public String getBlobUrl() {
		return blobUrl;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public int getTotalLikes() {
		return totalLikes;
	}

	public void addLike() {
		totalLikes++;
	}

	public void removeLike() {
		totalLikes--;
	}

}