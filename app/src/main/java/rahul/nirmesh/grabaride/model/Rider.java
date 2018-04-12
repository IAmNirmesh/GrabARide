package rahul.nirmesh.grabaride.model;

/**
 * Created by NIRMESH on 17-Mar-18.
 */

public class Rider {
    private String name, email, phone, password, avatarUrl, rates;

    public Rider() {
    }

    public Rider(String name, String email, String phone, String password, String avatarUrl, String rates) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.avatarUrl = avatarUrl;
        this.rates = rates;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getRates() {
        return rates;
    }

    public void setRates(String rates) {
        this.rates = rates;
    }
}
