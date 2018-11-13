package view;

import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import model.DataStore;
import org.apache.commons.lang3.tuple.Pair;
import util.Util;
import viewmodel.MainViewModel;

import java.util.Optional;

public class MainView {
    MainViewModel viewModel = new MainViewModel();
    @FXML
    Button back;
    @FXML
    ComboBox from;
    @FXML
    ComboBox to;
    @FXML
    BorderPane homeTab;
    @FXML
    Button forward;
    @FXML
    TabPane tabs;
    @FXML
    Tab home;
    Boolean fromSelected = false, toSelected = false;

    /**
     * Instantiate fxml objects.
     */
    @FXML
    public void initialize() {
        initMap();
        initUI();
        initStatistics();
    }

    /**
     * Setup UI components.
     */
    private void initUI() {
        Tab search = new Tab();

        search.setContent(Util.getViewWithArguments(SearchView.class, "", DataStore.get().globalIndex));
        search.setText("Search");
        search.setClosable(false);
        tabs.getTabs().add(search);

        from.setItems(viewModel.possiblePriceRanges);
        to.setItems(viewModel.possiblePriceRanges);

        from.valueProperty().bindBidirectional(viewModel.from); // Two-directional value binding
        to.valueProperty().bindBidirectional(viewModel.to);

        from.setCellFactory(lv -> new ListCell<Integer>() {
            private void to(ObservableValue observableValue, Object old, Object current) {
                if (getItem() == null) {
                    return;
                }
                setDisable(getItem().intValue() > (int) Optional.ofNullable(to.getValue()).orElse(Integer.MAX_VALUE));
            }

            @Override
            public void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setDisable(item.intValue() > (int) Optional.ofNullable(to.getValue()).orElse(Integer.MAX_VALUE));
                    to.valueProperty().addListener(this::to);
                }
            }
        });

        to.setCellFactory(lv -> new ListCell<Integer>() {
            private void from(Observable observable) {
                if (getItem() == null) {
                    return;
                }
                setDisable(getItem().intValue() <= (int) Optional.ofNullable(from.getValue()).orElse(0));
            }

            @Override
            public void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setDisable(item.intValue() <= (int) Optional.ofNullable(from.getValue()).orElse(0));
                    from.valueProperty().addListener(this::from);
                }
            }
        });
        forward.setOnAction(e -> handleForward());
        back.setOnAction(e -> handleBack());
        from.setOnAction(e -> selectFrom());
        to.setOnAction(e -> selectTo());
    }

    /**
     * Create map to be displayed.
     */
    private void initMap() {
        Pair<MapView, Parent> result = Util.getViewWithController(MapView.class);
        homeTab.centerProperty().setValue(result.getRight()); // A view inside another view

        result.getLeft().map.setAirbnbListings(viewModel.listingsFiltered);
        result.getLeft().map.onClick = (element) -> {
            Tab listingsTab = new Tab();

            listingsTab.setContent(Util.getViewWithArguments(SearchView.class, element.mapData.getName(), element.listings.get()));

            listingsTab.setText(String.format("%s listings", element.mapData.getName()));
            tabs.getTabs().add(listingsTab);
        };

    }

    /**
     * After selecting price range check if second is alos selected and if so enable button and home tab
     */
    private void selectFrom() {
        fromSelected = true;
        if (toSelected) enableButtons();
    }
    /**
     * After selecting price range check if second is alos selected and if so enable button and home tab
     */
    private void selectTo() {
        toSelected = true;
        if (fromSelected) enableButtons();
    }

    /**
     * Enable buttons and home pane
     */
    private void enableButtons() {
        forward.setDisable(false);
        back.setDisable(false);
        home.setDisable(false);
    }

    /**
     * Setup statistics pane.
     */
    private void initStatistics() {
        homeTab.rightProperty().setValue(Util.getViewRoot(StatisticsView.class));
    }

    /**
     * Select tab on the left from current/do  nothing if there is none
     */
    private void handleBack() {
        tabs.getSelectionModel().selectPrevious();
    }
    /**
     * Select tab on the right from current/do  nothing if there is none
     */
    private void handleForward() {
        tabs.getSelectionModel().selectNext();
    }
}
