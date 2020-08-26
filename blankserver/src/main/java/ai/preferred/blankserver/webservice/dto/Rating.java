package ai.preferred.blankserver.webservice.dto;

/**
 * @author hpminh@apcs.vn
 */
public class Rating {
    public String userID;
    public String itemID;
    public float rating;

    public Rating(String userID, String itemID, float rating) {
        this.userID = userID;
        this.itemID = itemID;
        this.rating = rating;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
