package view;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import model.AirbnbListing;
import model.LondonCultureVenue;
import model.index.AirbnbListingIndex;
import model.DataStore;
import model.index.VenueIndex;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.RAMDirectory;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.distance.DistanceUtils;
import org.locationtech.spatial4j.shape.Point;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

// TODO: Rebuild the index when the items change.
public class SearchView {
    @FXML
    TextField search;

    @FXML
    ListView results;

    private StringProperty district = new SimpleStringProperty("");
    private AirbnbListingIndex index;
    private ObservableList<AirbnbListing> listings = FXCollections.observableArrayList();

    /**
     * Constructor used when creating new search tabs,
     * based on district.
     *
     * @param district Name of district.
     * @param list Filtered list of listings on currently selected price and district.
     */
    public SearchView(String district, FilteredList<AirbnbListing> list) {

        this.district.set(district);

        this.listings = list.sorted(Comparator.comparingInt(AirbnbListing::getNumberOfReviews).reversed());

        this.index = new AirbnbListingIndex(new RAMDirectory(), this.listings);


    }

    /**
     * Creates a search tab with all listings.
     *
     * @param district Name of district.
     * @param index Starting index.
     */
    public SearchView(String district, AirbnbListingIndex index) {
        this.district.set(district);
        this.index = index;
    }

    /**
     * Instantiate fxml objects.
     */
    @FXML
    public void initialize() {
        results.setCellFactory(listView -> new ListCell<AirbnbListing>() {
            private Boolean show = false;


            @Override
            protected void updateItem(AirbnbListing item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {

                    HBox container = new HBox();
                    container.setSpacing(6.0);

                    Button button = new Button("\\/");
                    VBox box = new VBox();


                    container.getChildren().add(button);
                    container.getChildren().add(box);
                    HBox.setHgrow(box, Priority.ALWAYS);
                    box.setFillWidth(true);

                    ListView<String> hidden = new ListView();
                    hidden.setStyle(" -fx-background-insets: 0; -fx-padding: 0;");

                    button.setOnAction(event -> {
                        show = !show;

                        if(show){
                            VenueIndex venueIndex = DataStore.get().venueIndex;

                            IndexReader indexReader = null;
                            try {
                                indexReader = DirectoryReader.open(venueIndex.getDirectory());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                            IndexSearcher indexSearcher = new IndexSearcher(indexReader);


                            Point pt = SpatialContext.GEO.makePoint(item.getLongitude(), item.getLatitude());
                            DoubleValuesSource valueSource = venueIndex.strategy.makeDistanceValueSource(pt, DistanceUtils.DEG_TO_KM);//the distance (in km)

                            Sort distSort = null;//false=asc dist
                            try {
                                distSort = new Sort(valueSource.getSortField(false)).rewrite(indexSearcher);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            TopDocs docs = null;
                            try {
                                docs = indexSearcher.search(new MatchAllDocsQuery(), 10, distSort);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            int i = 0;
                            for(ScoreDoc scoreDoc: docs.scoreDocs){
                                Document document = null;
                                try {
                                    document = indexSearcher.doc(scoreDoc.doc);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                LondonCultureVenue venue =  DataStore.get().venues.get(Integer.parseInt(document.get("id")));

                                double distanceInDegrees = SpatialContext.GEO.calcDistance(pt.getCenter(), venue.getLongitude(), venue.getLatitude());


                                hidden.getItems().add(String.format("%s, distance: %.2f m", venue.getName(),DistanceUtils.degrees2Dist(distanceInDegrees, DistanceUtils.EARTH_MEAN_RADIUS_KM) * 1000 ));


                                i++;
                            }


                            hidden.setMaxHeight(10 * 26.5);
                            box.getChildren().add(hidden);
                            button.setText("/\\");
                        }else{
                            hidden.getItems().clear();
                            box.getChildren().remove(hidden);
                            button.setText("\\/");

                        }
                    });

                    HBox.getHgrow(box);

                    box.getChildren().add(new Text(item.getName()));
                    HBox desc = new HBox();
                    box.getChildren().add(desc);
                    desc.getChildren().add(new Text(String.format("Price: £%d.00 ", item.getPrice())));
                    desc.getChildren().add(new Text(String.format("Host: %s ", item.getHost_name())));
                    desc.getChildren().add(new Text(String.format("Reviews: %d ", item.getNumberOfReviews())));
                    desc.getChildren().add(new Text(String.format("Availability: %d ", item.getAvailability365())));
                    desc.getChildren().add(new Text(String.format("Reviews per month: %.2f ", item.getReviewsPerMonth())));

                    setGraphic(container);

                }
            }
        });

        resetList();
    }

    /**
     * Changes listings displayed, when called.
     */
    private void resetList() {
        if (listings != null) {
            this.results.setItems(listings);
        }
    }

    /**
     * Create a list of items matching the queried text.
     *
     * @param event Click event.
     */
    public void search(KeyEvent event) throws IOException {

        if (event.getCode() == KeyCode.ENTER) {
            String searchText = search.getText();

            if (searchText.trim().length() == 0) {
                resetList();
                return;
            }

            Query q = null;

            BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();

            try {
                q = new QueryParser("name", index.getAnalyzer()).parse(Arrays.stream(searchText.split(" ")).map(str -> str + "~ ").reduce(String::concat).get());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            booleanQuery.add(q, BooleanClause.Occur.SHOULD);

            IndexReader reader = DirectoryReader.open(index.getDirectory());

            IndexSearcher searcher = new IndexSearcher(reader);

            TopDocs docs = searcher.search(q, Integer.MAX_VALUE, new Sort(new SortedNumericSortField("reviews", SortField.Type.LONG, true)));

            ScoreDoc[] hits = docs.scoreDocs;

            districtProperty().setValue("Found " + hits.length + " hits.");

            ObservableList<AirbnbListing> list = FXCollections.observableList(new ArrayList<>());
            for (int i = 0; i < hits.length; ++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                AirbnbListing listing = DataStore.get().listings.get(Integer.parseInt(d.get("id")));
                list.add(listing);
            }

            results.setItems(list);
        }
    }

    /**
     * @return District name as a string.
     */
    public String getDistrict() {
        return district.get();
    }

    /**
     * @param district Sets the name of district.
     */
    public void setDistrict(String district) {
        this.district.set(district);
    }

    /**
     * @return District name as a property.
     */
    public StringProperty districtProperty() {
        return district;
    }
}
