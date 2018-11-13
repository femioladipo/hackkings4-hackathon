package model;


import com.opencsv.bean.CsvBindByName;

/**
 *
 */
public class LondonCultureVenue {

    /**
     * The id and name of the individual property
     */
    @CsvBindByName
    private int id;
    @CsvBindByName
    private String name;

    /**
     * The address which can be used for directions,
     * by end-user.
     */
    @CsvBindByName
    private String address;

    /**
     * The address which can be used for directions,
     * by end-user.
     */
    @CsvBindByName
    private String category;

    /**
     * The location on a map where the property is situated.
     */
    @CsvBindByName(column = "lat")
    private double latitude;
    @CsvBindByName(column = "lng")
    private double longitude;


    /**
     * The address which can be used for directions,
     * by end-user.
     */
    @CsvBindByName
    private String details;

    // TODO crawl web-link for number of reviews.
    //@CsvBindByName(column = "number_of_reviews")
    //private int numberOfReviews;

    public LondonCultureVenue() {
    }

    public LondonCultureVenue(int id, String name, String address, String category, double latitude,
                              double longitude, String details) {

        this.id = id;
        this.name = name;
        this.address = address;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.details = details;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "LondonCultureVenue{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", category='" + category + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", details='" + details +
                '}';
    }
}
