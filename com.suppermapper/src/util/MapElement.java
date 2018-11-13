package util;

import javafx.beans.binding.ObjectBinding;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import model.AirbnbListing;
import model.MapData;
import org.locationtech.jts.geom.Coordinate;

import java.util.List;

public class MapElement {
    public MapData mapData;
    public ObjectBinding<FilteredList<AirbnbListing>> listings;
    public Map map;
    private Path path = new Path();
    private Bubble bubble;
    private Tooltip tooltip = new Tooltip();

    /**
     * @param mapData the mapData element that contains the name and other stuff
     * @param listings the observable list of listings
     * @param coordinatesList the coordinates used to create the map segments
     */
    public MapElement(MapData mapData, ObjectBinding<FilteredList<AirbnbListing>> listings, List<Coordinate[]> coordinatesList) {
        this.mapData = mapData;
        this.listings = listings;

        path.setStrokeWidth(0.05);
        path.setFill(Color.WHITE);

        // here the map elements are actually built.
        for (Coordinate[] coordinates : coordinatesList) {
            // we start by setting the start to the first point in the polygon
            path.getElements().add(new MoveTo(coordinates[0].x, coordinates[0].y));

            // Then for every subsequent point we create a line to the point in the polygon
            for (int j = 1; j < coordinates.length; j++) {
                path.getElements().add(new LineTo(coordinates[j].x, coordinates[j].y));
            }

            // To finish a line is created that connects the last point to the first one, so we don't have missing borders
            path.getElements().add(new LineTo(coordinates[0].x, coordinates[0].y));

            // Here you can see that we setup the events so that every path(map segment) can receive different mouse events such as:
            path.setOnMouseEntered(this::pathMouseEntered);
            path.setOnMouseExited(this::pathMouseExited);
            path.setOnMousePressed(this::pathMousePressed);
            path.setOnMouseMoved(this::pathMouseMoved);

            // here the final tooltip is created so that the user can know the name of the district his mouse is over.
            tooltip.setText(mapData.getName());

            //
        }

        this.listings.addListener((observableValue, old, current) -> {
            if (old != null) {
                old.removeListener(this::change);
            }
            current.addListener(this::change);
            this.change(null);
        });
    }

    /**
     * Handles the event when the mouse is moving over a map segment.
     * It shows a tooltip.
     */
    private void pathMouseMoved(MouseEvent event) {
        tooltip.show(path, event.getScreenX(), event.getScreenY() + 22);
    }

    /**
     * Handles changes in the observable list, so that the number of items in a bubble can be updated accordingly.
     */
    private void change(ListChangeListener.Change<? extends AirbnbListing> change) {
        FilteredList<AirbnbListing> current = this.listings.get();

        if (this.bubble != null) {
            bubble.setText(Integer.toString(current.size()));

            double size = map.airbnbListingsProperty().get().size();

            bubble.setColoration(current.size() / size);
        }
    }

    /**
     * Returns the path, which represents the map segment on the screen.
     */
    public Path getPath() {
        return path;
    }

    /**
     * Sets the path, which represent the map segment on the screen.
     * @param path the
     */
    public void setPath(Path path) {
        this.path = path;
    }

    /**
     * Handles the event, when the mouse is pressed on the map segment.
     */
    private void pathMousePressed(MouseEvent event) {
        this.map.onClick.accept(this);
    }

    /**
     * Handles the event, when the mouse stops hovering over the map segment.
     */
    private void pathMouseExited(MouseEvent event) {
        Path target = this.path;
        target.setEffect(null);
//        target.setFill(Color.WHITE);
        tooltip.hide();
    }

    /**
     * Handles the event, when the mouse starts hovering over the map segment.
     */
    private void pathMouseEntered(MouseEvent event) {
        InnerShadow shadow = new InnerShadow();
        shadow.setColor(Color.WHITE);
        Path target = this.path;
        target.setEffect(shadow);
    }

    /**
     * Sets the bubble of the map
     * @param bubble the bubble that will be set.
     */
    public void setBubble(Bubble bubble) {
        this.bubble = bubble;
    }
}
