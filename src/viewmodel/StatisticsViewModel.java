package viewmodel;


import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.AirbnbListing;
import model.DataStore;
import org.apache.commons.lang3.tuple.Pair;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class StatisticsViewModel {

    public LinkedList<Pair<String, Binding>> hidden = new LinkedList<>();
    public ObservableList<Pair<String, Binding>> visible = FXCollections.observableArrayList();
    private DataStore data = DataStore.get();

    /**
     * Instantiates the statistics and their bindings, initially displaying the last
     * 4 statistics in the GUI.
     */
    public StatisticsViewModel() {
        hidden.add(Pair.of("The priciest district", priciestNeighbourhood()));
        hidden.add(Pair.of("Average reviews\nper property", averageReviewScore()));
        hidden.add(Pair.of("Most rated district", mostRatedDistrict()));
        hidden.add(Pair.of("Average availability", averageAvailability()));
        hidden.add(Pair.of("District with longest\nrunning listings", districtWithLongestRunningListings()));
        hidden.add(Pair.of("Number of entire homes\navailable for rental", entireHomesAvailable()));
        hidden.add(Pair.of("Number of private rooms\navailable", privateRoomsAvailable()));
        hidden.add(Pair.of("Total number of rentals\navailable", allAvailableRentals()));
        hidden.add(Pair.of("Host with highest number\nof listings", MostActiveHost()));

        for (int i = 0; i <= 3; i++) {
            visible.add(hidden.removeLast());
        }
    }

    /**
     * Displays the statistic at the start of the linked list in GUI.
     * @param index Index of the pane to display the statistic.
     */
    public void getNext(Integer index) {
        Pair<String, Binding> next = hidden.removeFirst();
        Pair<String, Binding> previous = visible.set(index, next);
        hidden.addLast(previous);
    }

    /**
     * Displays the statistic at the end of the linked list in GUI.
     * @param index Index of the pane to display the statistic.
     */
    public void getPrevious(Integer index) {
        Pair<String, Binding> next = hidden.removeLast();
        Pair<String, Binding> previous = visible.set(index, next);
        hidden.addFirst(previous);
    }

    /**
     * Return name of the district with highest average minimal price(calculated as price per night * minimal number of nights)
     */
    private Binding priciestNeighbourhood() {
        return (Bindings.createStringBinding(() -> data.listingsFiltered.stream()
                .collect(Collectors.groupingBy(AirbnbListing::getNeighbourhood))
                .entrySet()
                .stream()
                .map(entry -> Pair.of(entry.getKey(), entry.getValue().stream().map(listing -> listing.getPrice() * listing.getMinimumNights()).reduce(Integer::sum)
                        .get() / (entry.getValue().size() == 0 ? 1   : entry.getValue().size())))
                .reduce((previous, current) -> (previous.getRight() > current.getRight() ? previous : current)).get().getLeft() ,data.listingsFiltered
        ));
    }

    /**
     * The average number of reviews for a listing.
     */
    private Binding averageReviewScore() {
        return (Bindings.createStringBinding(() -> new DecimalFormat("#0.00").format(data.listingsFiltered.stream()
                .map(AirbnbListing::getNumberOfReviews)
                .reduce(Integer::sum).get() / (double) data.listingsFiltered.size()), data.listingsFiltered)
        );
    }

    /**
     * The district with the highest number of ratings.
     */
    private Binding mostRatedDistrict() {
        return (Bindings.createStringBinding(() -> data.listingsFiltered.stream()
                .collect(Collectors.groupingBy(AirbnbListing::getNeighbourhood))
                .entrySet()
                .stream()
                .map(entry -> Pair.of(
                        entry.getKey(), entry.getValue().stream().map(AirbnbListing::getNumberOfReviews).reduce(Integer::sum).get()
                ))
                .reduce((previous, current) -> (previous.getRight() > current.getRight()) ? previous : current).get().getLeft() ,data.listingsFiltered
        ));
    }

    /**
     * Calculates the estimated number of months each listing has been listed, the returns a binding for
     * the district where the averahe months is greatest.
     */
    private Binding districtWithLongestRunningListings() {
        return (Bindings.createStringBinding(() -> data.listingsFiltered.stream()
                .collect(Collectors.groupingBy(AirbnbListing::getNeighbourhood))
                .entrySet()
                .stream()
                .map(entry -> Pair.of(
                        entry.getKey(),
                        entry.getValue().stream().map(listing -> listing.getNumberOfReviews() / listing.getReviewsPerMonth()).reduce(Double::sum).get() / (entry.getValue().size() == 0 ? 1 : entry.getValue().size())
                ))
                .reduce((previous, current) -> (previous.getRight() > current.getRight()) ? previous : current
                ).get().getLeft(), data.listingsFiltered
        )); // this can also be used to find the longest running listing or sth like that.
    }

    /**
     * Return average avaibility of properties in currently selected price range
     */
    private Binding averageAvailability() {
        return (Bindings.createIntegerBinding(() -> data.listingsFiltered.stream()
                .map(AirbnbListing::getAvailability365)
                .reduce(Integer::sum).get() / (data.listingsFiltered.size()==0?1:data.listingsFiltered.size()), data.listingsFiltered).asString()
        );
    }

    /**
     * The total number of whole homes or apartments available for rental.
     */
    private Binding entireHomesAvailable() {
        return (Bindings.createStringBinding(() -> data.listingsFiltered.stream()
                .map(listing -> listing.getRoom_type().equals("Entire home/apt") ? 1 : 0)
                .reduce(Integer::sum).get().toString(), data.listingsFiltered
        ));
    }

    /**
     * Check whether the room is of type private if yes then count it into the sum.
     * Display the sum of available private rooms in the statistics panel.
     */
    private Binding privateRoomsAvailable() {
        return (Bindings.createStringBinding(() -> data.listingsFiltered.stream()
                .map(listing -> listing.getRoom_type().equals("Private room") ? 1 : 0)
                .reduce(Integer::sum).get().toString() ,data.listingsFiltered
        ));
    }

    /**
     * Check if listing is available by checking if value of listing is 1
     * Display the sum of available listings in the statistics panel.
     */
    private Binding allAvailableRentals() {
        return (Bindings.createStringBinding(() -> data.listingsFiltered.stream()
                .map(listing -> 1)
                .reduce(Integer::sum).get().toString() , data.listingsFiltered
        ));
    }

    /**
     *  Group by host name and check which host has the highest number of listings by
     *  comparing whether the currently checked host has a higher number of listings than previous one
     *  if yes then compare it with the next host if no then compare the new host with highest number of listings.
     */
    private Binding MostActiveHost() {
        return (Bindings.createStringBinding(() -> data.listingsFiltered.stream()
                .collect(Collectors.groupingBy(AirbnbListing::getHost_name))
                .entrySet()
                .stream()
                .map(entry -> Pair.of(
                        entry.getKey(), entry.getValue().stream().map(AirbnbListing::getCalculatedHostListingsCount).reduce(Integer::sum).get()
                ))
                .reduce((previous, current) -> (previous.getRight() > current.getRight()) ? previous : current).get().getLeft(), data.listingsFiltered
        ));
    }

}
