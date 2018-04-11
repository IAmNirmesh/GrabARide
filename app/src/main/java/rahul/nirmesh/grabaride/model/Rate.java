package rahul.nirmesh.grabaride.model;

/**
 * Created by NIRMESH on 10-Apr-18.
 */

public class Rate {
    private String rates, comments;

    public Rate() {
    }

    public Rate(String rates, String comments) {
        this.rates = rates;
        this.comments = comments;
    }

    public String getRates() {
        return rates;
    }

    public void setRates(String rates) {
        this.rates = rates;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
