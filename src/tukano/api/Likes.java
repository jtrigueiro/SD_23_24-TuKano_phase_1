package tukano.api;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Likes {
    
    @Id
    private String userId, shortId;

    public Likes(String userId, String shortId) {
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
