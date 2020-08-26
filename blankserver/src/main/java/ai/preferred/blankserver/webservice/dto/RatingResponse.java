package ai.preferred.blankserver.webservice.dto;

/**
 * @author hpminh@apcs.vn
 */
public class RatingResponse {
    Double rating;

    public RatingResponse(Double rating) {
        this.rating = rating;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }
}
