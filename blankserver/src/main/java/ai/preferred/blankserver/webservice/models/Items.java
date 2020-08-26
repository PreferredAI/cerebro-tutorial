package ai.preferred.blankserver.webservice.models;

import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * @author hpminh@apcs.vn
 */
public class Items {
    @Id
    public String _id;
    public String title;
    public String genres;

    public Items(String _id, String title, String genres) {
        this._id = _id;
        this.title = title;
        this.genres = genres;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

}

