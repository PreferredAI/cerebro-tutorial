package ai.preferred.blankserver.webservice.models;

import org.springframework.data.annotation.Id;

/**
 * @author hpminh@apcs.vn
 */
public class Users {
    @Id
    String _id;
    String gender;
    String age;
    String occupation;
    String zip_code;

    public Users(String _id, String gender, String age, String occupation, String zip_code) {
        this._id = _id;
        this.gender = gender;
        this.age = age;
        this.occupation = occupation;
        this.zip_code = zip_code;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getZip_code() {
        return zip_code;
    }

    public void setZip_code(String zip_code) {
        this.zip_code = zip_code;
    }
}
