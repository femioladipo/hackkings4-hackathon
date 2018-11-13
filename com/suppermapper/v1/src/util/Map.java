package util;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import model.AirbnbListing;
import model.DataStore;
import model.MapData;
import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class Bubble extends Group {
    private Circle circle = new Circle();
    private Text text = new Text();

    private DoubleProperty coloration = new SimpleDoubleProperty(0.5);
    private StringProperty textProperty = new SimpleStringProperty() {
        @Override
        public void set(String value) {
            // transforming the text so it stays in the middle of the bubble.
            text.setText(value);
            Bounds bounds = text.getLayoutBounds();
            text.getTransforms().removeIf(transform -> transform instanceof Translate);
            text.getTransforms().add(new Translate(-bounds.getWidth() / 2., bounds.getHeight() / 2. - 3));
        }
    };

    public Bubble(double radius) {
        circle.setRadius(radius);
        circle.fillProperty().setValue(Color.LIGHTBLUE);
        circle.setMouseTransparent(true);
        InnerShadow s = new InnerShadow();

        s.setColor(Color.AQUAMARINE);
        s.radiusProperty().bind(coloration.multiply(radius * 100));

        s.setBlurType(BlurType.GAUSSIAN);

        circle.setEffect(s);

        text.getTransforms().add(new Scale(radius / 22, radius / 22));

        text.setMouseTransparent(true);

        this.getChildren().add(0, circle);
        this.getChildren().add(1, text);
    }

    public Circle getCircleNode() {
        return circle;
    }

    public Text getTextNode() {
        return text;
    }

    public double getColoration() {
        return coloration.get();
    }

    public void setColoration(double coloration) {
        this.coloration.set(coloration);
    }

    public DoubleProperty colorationProperty() {
        return coloration;
    }

    public String getText() {
        return textProperty.get();
    }

    public void setText(String textProperty) {
        this.textProperty.set(textProperty);
    }

    public StringProperty textProperty() {
        return textProperty;
    }
}

public class Map extends Pane {
    final private ReadOnlyDoubleProperty scale; // the scale of the map this is done so that we get a mpa that is the size of the initial container so we can start with zoom equal to 0
    public Consumer<MapElement> onClick;
    /*
    the groups that hold the different elements
     */
    private Group mapGeometry = new Group(); // the group that holds the geometries of the map segments
    private Group mapBubbles = new Group(); // the group that hold the map bubbles
    private Group mapGroup = new Group(); // this one hold the previous two for easier transformations
    private Group dotGroup = new Group(); // the group that holds the dotts for the single listings
    /**
     * Controls the clipping of the map. Clips the map if it overflows the size of the container.
     */
    private BooleanProperty clipMap = new SimpleBooleanProperty(false, "clip map");
    /**
     * Renders a rect around the map to aid debugging. Useless in other contexts.
     */
    private BooleanProperty mapEnvelope = new SimpleBooleanProperty(true, "map envelope");
    /**
     * Controls the visibility of the dots reprezenting airbinb listings/
     */
    private BooleanProperty showDots = new SimpleBooleanProperty(false, "show airbinb listings");
    /**
     * Controls how zoomed the map is. The map resizes when the window is resized.
     */
    private DoubleProperty zoom = new SimpleDoubleProperty(1);
    /**
     * Holds the MapData fragments which are used to construct the different regions of the map.
     */
    private ListProperty<MapData> mapDataList = new SimpleListProperty<>();
    /**
     * Holds the filtered airbnb listings so that they can be displayed.
     */
    private ObjectProperty<ObservableList<AirbnbListing>> airbnbListings = new SimpleObjectProperty<>(FXCollections.observableArrayList());
    /**
     * Holds the names of the districts and their respective map elements. Which hold a path and a bubble.
     */
    private HashMap<String, MapElement> mapElements = new HashMap<>();
    /**
     * The geo size of the map.
     */
    private BoundingBox mapBoundingBox;
    // the map offsets
    private double x;
    private double y;

    public Map() {
        this(new SimpleListProperty<MapData>(new ObservableListWrapper<>(DataStore.get().mapDataList)));
    }


//    private Pair<Coordinate, Coordinate> drag = Pair.of(new Coordinate(0,0), new Coordinate(0,0));


    public Map(ListProperty<MapData> mapDataList) {
        this.mapDataList = mapDataList;
        GeometryCollection collection = new GeometryCollection(mapDataList.parallelStream().map(MapData::getGeometry).toArray(Geometry[]::new), new GeometryFactory());

        // here we get the geo center of the map so we can center all our coordinates around the (0,0)
        final Envelope envelope = collection.getEnvelopeInternal();
        x = envelope.centre().x;
        y = envelope.centre().y;


        // calculate the scale based on some numbers because if the scale is too small it brakes in unexpected ways, as the precision of the float is not enough.
        double map_width = envelope.getWidth();
        scale = new ReadOnlyDoubleWrapper((200 / map_width));

        // we go around the map data
        for (MapData mapData : mapDataList) {
            Geometry geometry = mapData.getGeometry();

            Point center = geometry.getCentroid();
            String name = mapData.getName();
            // make binding so that the bubbles update when the original list of listings is filtered. We create a binding for every neighborhood.
            ObjectBinding<FilteredList<AirbnbListing>> listings = Bindings.createObjectBinding(() -> airbnbListings.get().filtered(listing -> listing.getNeighbourhood().equals(name)), this.airbnbListingsProperty()); // I would have used a lambda but that causes an error

            // the envelope for the specific district
            Envelope segmentEnvelope = geometry.getBoundary().getEnvelopeInternal();


            // we decide the size of the circle based on the size of the district
            // we get the smaller side and make it 5 times smaller.
            double smaller_side = (segmentEnvelope.getWidth() < segmentEnvelope.getHeight() ? segmentEnvelope.getWidth() : segmentEnvelope.getHeight()) * scale.get();
            Bubble bubble = new Bubble(smaller_side / 5);

            // then center it
            bubble.setTranslateX((center.getX() - x) * scale.get());
            bubble.setTranslateY(-(center.getY() - y) * scale.get());


            mapBubbles.getChildren().add(bubble);

            // get all the coordinates for the polygon and transform them by subtracting the center and multiplying by the scale.
            List<Coordinate[]> coordinateList = this.geometryToList(geometry).stream().map(Geometry::getCoordinates).map(coordinates -> Arrays.stream(coordinates)
                    .map(coordinate -> new Coordinate((coordinate.x - x) * scale.get(), -(coordinate.y - y) * scale.get(), 0))
                    .toArray(Coordinate[]::new)).collect(Collectors.toList());

            // the map element is created and put inside the group ready to be baked :D
            MapElement mapElement = new MapElement(mapData, listings, coordinateList);
            mapElement.setBubble(bubble);
            mapElements.put(name, mapElement);
            mapElement.map = this;
            mapGeometry.getChildren().add(mapElement.getPath());
        }

        // get the bounding box so that the map can be always centered and always zoom enough so it fill the container.
        mapBoundingBox = new BoundingBox((envelope.getMinX() - x) * scale.get(), (envelope.getMinY() - y) * scale.get(), envelope.getWidth() * scale.get(), envelope.getHeight() * scale.get());

        // let's show what we've got

        mapGroup.getChildren().add(mapGeometry);
        mapGroup.getChildren().add(dotGroup);
        mapGroup.getChildren().add(mapBubbles);
        this.getChildren().add(mapGroup);

        // make the map always centered
        mapGroup.translateYProperty().bind(this.heightProperty().divide(2));
        mapGroup.translateXProperty().bind(this.widthProperty().divide(2));

        // scala the map based on the zoom property
        this.mapGroup.scaleXProperty().bind(this.zoomProperty());
        this.mapGroup.scaleYProperty().bind(this.zoomProperty());

        // make the map always scaled
        this.layoutBoundsProperty().addListener(this::onResize);


        // listen for changes of different props.
        clipMap.addListener(this::onClip);
        mapEnvelope.addListener(this::showEnvelope);
        showDots.addListener(this::dotsListener);
    }

    /**
     * Converts a geometry to a collection which can be iterated.
     *
     * @param geometry the Geometry containing sub geometries
     * @return the List of Geometries that can be iterated
     **/
    private List<Geometry> geometryToList(Geometry geometry) {
        List<Geometry> geometries = new ArrayList<>();
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            geometries.add(geometry.getGeometryN(i));
        }
        return geometries;
    }

    /**
     * Listens for changes of the boolean so we can update the display
     * I use low dash for arguments that are ignored by me.
     */
    private void dotsListener(Observable __o, Boolean __, Boolean current) {
        this.airbnbListings.addListener(this::objectChangeListener);

        if (current) {
            if (airbnbListings.get() != null) {
                this.objectChangeListener(null, null, airbnbListings.get());
                this.changeListener.onChanged(null);
            }
        } else {
            this.airbnbListings.get().removeListener(this.changeListener);

            dotGroup.getChildren().clear();

        }
    }

    /**
     * Deals with the cange of the object not the listing elements itself.
     */
    private void objectChangeListener(Observable o, ObservableList<AirbnbListing> old, ObservableList<AirbnbListing> curent) {
        if (old != null) {
            old.removeListener(this.changeListener);
        }

        if (showDots.get()) {
            curent.addListener(this.changeListener);
            this.changeListener.onChanged(null);
        }
    }

    /**
     * Listens for changes in the airbnb listing so the the dotts can be updated
     * This is invoked when an element is added or removed from the list.
     * And if you ask why don't I just use a lambda, it is because the lambda doesn't get removed probably because when passing a lambda a new class is created or the JVM is doing some funny stuff.
     */
    private final ListChangeListener<? super AirbnbListing> changeListener = new ListChangeListener<AirbnbListing>() {
        @Override
        public void onChanged(Change<? extends AirbnbListing> change) {
            dotGroup.getChildren().clear();

            for (AirbnbListing listing : airbnbListings.get()) {
                Circle circle = new Circle();
                circle.setCenterX((listing.getLongitude() - x) * scale.get());
                circle.setCenterY(-(listing.getLatitude() - y) * scale.get());
                circle.setRadius(0.1);
                circle.fillProperty().setValue(Color.RED);
                circle.setMouseTransparent(true);
                dotGroup.getChildren().add(circle);

            }
        }
    };

    // only for debug
    private void showEnvelope(javafx.beans.Observable observable, Boolean old, Boolean show) {
        if (show) {
            Path path = new Path();
            path.setStrokeWidth(0.1);
            path.getElements().add(new MoveTo(mapBoundingBox.getMinX(), mapBoundingBox.getMinY()));
            path.getElements().add(new LineTo(mapBoundingBox.getMaxX(), mapBoundingBox.getMinY()));
            path.getElements().add(new LineTo(mapBoundingBox.getMaxX(), mapBoundingBox.getMaxY()));
            path.getElements().add(new LineTo(mapBoundingBox.getMinX(), mapBoundingBox.getMaxY()));
            path.getElements().add(new LineTo(mapBoundingBox.getMinX(), mapBoundingBox.getMinY()));
            path.setUserData("envelope");
            mapGeometry.getChildren().add(path);
        } else {
            mapGeometry.getChildren().removeIf(child -> child.getUserData().equals("envelope"));
        }
    }


    private void onClip(ObservableValue<? extends Boolean> observableValue, boolean previous, boolean current) {
        if (current) {
            Rectangle rectangle = new Rectangle();
            rectangle.widthProperty().bind(this.widthProperty());
            rectangle.heightProperty().bind(this.heightProperty());
            this.setClip(rectangle);
        } else {
            this.setClip(null);
        }
    }

    /**
     * This listens for changes in the bound of the container, so it can resize the map accordingly.
     */
    private void onResize(ObservableValue<? extends Bounds> observableValue, Bounds previous, Bounds bounds) {
        double height = bounds.getHeight();
        double width = bounds.getWidth();


        Bounds boundingBox = this.localToParent(this.mapBoundingBox);

        // we scala the map based on some coefficient so the aspect ration of the map is not changed
        double coef_w = width / boundingBox.getWidth() - 0.1;
        double coef_h = height / boundingBox.getHeight() - 0.1;


        if (coef_h > coef_w) {
            zoom.set(coef_w);

        } else {
            zoom.set(coef_h);
        }

    }

    public double getScale() {
        return scale.get();
    }

    public boolean isClipMap() {
        return clipMap.get();
    }

    public void setClipMap(boolean clipMap) {
        this.clipMap.set(clipMap);
    }

    public BooleanProperty clipMapProperty() {
        return clipMap;
    }

    public double getZoom() {
        return zoom.get();
    }

    public void setZoom(double zoom) {
        this.zoom.set(zoom);
    }

    public DoubleProperty zoomProperty() {
        return zoom;
    }

    public boolean getMapEnvelope() {
        return mapEnvelope.get();
    }

    public void setMapEnvelope(boolean mapEnvelope) {
        this.mapEnvelope.set(mapEnvelope);
    }

    public BooleanProperty mapEnvelopeProperty() {
        return mapEnvelope;
    }

    public ObservableList<MapData> getMapDataList() {
        return mapDataList.get();
    }

    public void setMapDataList(ObservableList<MapData> mapDataList) {
        this.mapDataList.set(mapDataList);
    }

    public ListProperty<MapData> mapDataListProperty() {
        return mapDataList;
    }

    public ObservableList<AirbnbListing> getAirbnbListings() {
        return airbnbListings.get();
    }

    public void setAirbnbListings(ObservableList<AirbnbListing> airbnbListings) {
        this.airbnbListings.set(airbnbListings);
    }

    public final ObjectProperty<ObservableList<AirbnbListing>> airbnbListingsProperty() {
        if (airbnbListings == null) {
            airbnbListings = new SimpleObjectProperty<ObservableList<AirbnbListing>>(this, "items");
        }
        return airbnbListings;
    }

    public boolean isShowDots() {
        return showDots.get();
    }

    public void setShowDots(boolean showDots) {
        this.showDots.set(showDots);
    }

    public BooleanProperty showDotsProperty() {
        return showDots;
    }
}
