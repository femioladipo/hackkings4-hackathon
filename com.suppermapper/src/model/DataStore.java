package model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import model.index.AirbnbListingIndex;
import model.index.VenueIndex;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DataStore {
    private static DataStore ourInstance;

    static {
        try {
            ourInstance = new DataStore();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * the observable list of all listings.
     */
    public ObservableList<AirbnbListing> listings;

    /**
     * The list of loaded mapData elements.
     */
    public List<MapData> mapDataList;

    /**
     * The filtered listings controlled from the predicate.
     */
    public FilteredList<AirbnbListing> listingsFiltered;

    /**
     * The globalIndex that can be used query all the listings.
     */
    public AirbnbListingIndex globalIndex;

    /**
     * The globalIndex that can be used query all the listings.
     */
    public VenueIndex venueIndex;

    public List<LondonCultureVenue> venues = new ArrayList<>();

    private DataStore() throws IOException {

        AirbnbDataLoader loader = new AirbnbDataLoader();
        listings = FXCollections.observableList(loader.load());
        listingsFiltered = listings.filtered(listing -> true);
        MapDataLoader mapDataLoader = new MapDataLoader();
        mapDataList = mapDataLoader.load();

        // using an file directory so we don't recreate the index every time we lauch the program.
        globalIndex = new AirbnbListingIndex(new NIOFSDirectory(Paths.get("./index.lucene")), listings);

        LondonCultureVenueLoader venueLoader = new LondonCultureVenueLoader();

//        venues.addAll(venueLoader.loadAttractions());
        venues.addAll(venueLoader.loadPOI());
//        venues.addAll(venueLoader.loadRestaurants());

        venueIndex = new VenueIndex(new RAMDirectory(), venues);
    }

    public static DataStore get() {
        return ourInstance;
    }


}
