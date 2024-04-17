package tukano.api;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Represents a Likes relationship between a user and a short.
 * 
 * A likes has an unique concatenation of the userId and the shortId that are part of the relationship;
 * Comprises of a userId and a shortId, where user with userId likes short with shortId.
 *
 */
@Entity
public class Likes {
    
    @Id
    private String concat;
    private String userId, shortId;

    public Likes() {}

    public Likes(String userId, String shortId) {
        this.concat = userId + shortId;
        this.userId = userId;
        this.shortId = shortId;
    }

    public String getUserId() {
        return userId;
    }

    public String getShortId() {
        return shortId;
    }
}
