package viewmodel;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import model.AirbnbListing;
import model.DataStore;

import java.util.Optional;
import java.util.function.Predicate;

public class MainViewModel {

    public ObjectProperty<Integer> from = new SimpleObjectProperty<>();
    public ObjectProperty<Integer> to = new SimpleObjectProperty<>();
    public ListProperty<Integer> possiblePriceRanges = new SimpleListProperty<>(FXCollections.observableArrayList());
    public FilteredList<AirbnbListing> listingsFiltered = DataStore.get().listingsFiltered;


    public MainViewModel() {
        Integer maxPrice = DataStore.get().listings.stream().map(AirbnbListing::getPrice).reduce(Integer::max).get();
        Integer minPrice = DataStore.get().listings.stream().map(AirbnbListing::getPrice).reduce(Integer::min).get();
        possiblePriceRanges.add(minPrice - minPrice % 100);

        for (double t = 6; t >= 0; t -= 0.25) {
            int p = (int) Math.floor(maxPrice * Math.exp(-t));
            if (p > 1000) {
                possiblePriceRanges.add(p - p % 100);
            } else if (p > 100) {
                possiblePriceRanges.add(p - p % 10);
            } else {
                possiblePriceRanges.add(p - p % 5);
            }
        }

        ObjectBinding binding = Bindings
                .createObjectBinding(
                        () -> (Predicate<? extends AirbnbListing>)
                                (item) -> item.getPrice() > Optional.ofNullable(from.get()).orElse(0)
                                        && item.getPrice() < Optional.ofNullable(to.get()).orElse(Integer.MAX_VALUE),
                        from, to);

        listingsFiltered.predicateProperty().bind(binding);

    }

}
