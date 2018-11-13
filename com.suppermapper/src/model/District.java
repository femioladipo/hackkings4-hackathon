package model;

import java.util.List;

public class District {
    private String name;
    private List<AirbnbListing> listings;


    public District(String name, List<AirbnbListing> listings) {
        this.name = name;
        this.listings = listings;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<AirbnbListing> getListings() {
        return listings;
    }

    public int getCount() {
        return listings.size();
    }
}
