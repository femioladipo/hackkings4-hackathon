package view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import util.Map;

public class MapView {

    @FXML
    Map map;
    @FXML
    Button up;
    @FXML
    Button down;
    @FXML
    Button circle;
    @FXML
    AnchorPane root;

    /**
     * Sets up the fxml objects.
     */
    @FXML
    public void initialize() {
        up.setOnAction((event) -> {
            map.setZoom(map.getZoom() * 1.4);
        });
        circle.setOnAction((event) -> map.setShowDots(!map.isShowDots()));
        down.setOnAction((event) -> map.setZoom(map.getZoom() * 1. / 1.4));

    }

    /**
     * Map object viewed in GUI
     * @return Map object in GUI.
     */
    public Map getMap() {
        return map;
    }

    /**
     * Sets map used in the GUI
     * @param map Map object to be viewed.
     */
    public void setMap(Map map) {
        this.map = map;
    }
}
