package tukano.api;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Represents a Follows relationship between two users.
 * 
 * A follows has an unique concatenation of the two userIds that are part of the relationship;
 * Comprises of two userIds, userId1 and userId2, where user with userId1 follows user with userId2.
 *
 */
@Entity
public class Follows {
    
    @Id
    private String concat;
    private String userId1;
    private String userId2;

    public Follows() {
    }

    public Follows(String userId1, String userId2) {
        this.concat = userId1 + userId2;
        this.userId1 = userId1;
        this.userId2 = userId2;
    }

    public String getUserId1() {
        return userId1;
    }

    public String getUserId2() {
        return userId2;
    }
}
