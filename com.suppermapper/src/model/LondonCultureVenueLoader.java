package model;

import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LondonCultureVenueLoader {

    /**
     * Return an ArrayList containing the rows in the AirBnB London data set csv file.
     */
    public List<LondonCultureVenue> loadAttractions() {
        return load("london-attraction-clean.csv", 0, 1, 2, 3, 5, 6, 7);
    }

    public List<LondonCultureVenue> loadPOI() {
        return load("london-poi.csv", 2, 6, 0, 1, 3, 4, 10);
    }

    public List<LondonCultureVenue> loadRestaurants() {
        return load("london-restaurant.csv", 0, 1, 2, 3, 5, 6, 7);
    }

    private List<LondonCultureVenue> load(String fileLocation, int _id, int _name, int _address, int _category,
                                          int _latitude, int _longitude, int _details) {

        List<LondonCultureVenue> venues = new ArrayList<>();

        try {
            URL url = getClass().getResource(fileLocation);
            CSVReader reader = new CSVReader(new FileReader(new File(url.toURI()).getAbsolutePath()), ',', '"');
            String[] line;
            //skip the first row (column headers)
            reader.readNext();
            while ((line = reader.readNext()) != null) {
                int id = convertInt(line[_id]);
                String name = line[_name];
                String address = line[_address];
                String category = line[_category];
                double latitude = convertDouble(line[_latitude]);
                double longitude = convertDouble(line[_longitude]);
                String details = line[_details];

                LondonCultureVenue venue = new LondonCultureVenue(id, name, address,
                        category, latitude, longitude, details
                );

                venues.add(venue);
            }
        } catch (IOException | URISyntaxException e) {
            System.out.println("Failure! Something went wrong");
            e.printStackTrace();
        }
        System.out.println("Success! Number of loaded records: " + venues.size());
        return venues;
    }

    /**
     * @param doubleString the string to be converted to Double type
     * @return the Double value of the string, or -1.0 if the string is
     * either empty or just whitespace
     */
    private Double convertDouble(String doubleString) {
        if (doubleString != null && !doubleString.trim().equals("")) {
            return Double.parseDouble(doubleString);
        }
        return -1.0;
    }

    /**
     * @param intString the string to be converted to Integer type
     * @return the Integer value of the string, or -1 if the string is
     * either empty or just whitespace
     */
    private Integer convertInt(String intString) {
        if (intString != null && !intString.trim().equals("")) {
            return Integer.parseInt(intString);
        }
        return -1;
    }

    public static void main(String[] args) {
        List<LondonCultureVenue> list = new LondonCultureVenueLoader().loadAttractions();
        System.out.println("______________");
        System.out.println(list.toString());
    }
}
