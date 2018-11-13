package view;

import javafx.beans.binding.Binding;
import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.apache.commons.lang3.tuple.Pair;
import viewmodel.StatisticsViewModel;

public class StatisticsView {
    @FXML
    VBox statisticsView;
    private StatisticsViewModel viewModel = new StatisticsViewModel();

    /**
     * Sets up the fxml objects.
     */
    @FXML
    public void initialize() {
        generateStatistics();
    }

    /**
     * Populate the view with 4 initial statistics.
     */
    private void generateStatistics() {
        viewModel.visible.addListener((ListChangeListener<Pair<String, Binding>>) change -> {
            while (change.next()) {
                int fromIndex = change.getFrom();
                int toIndex = change.getTo();
                if (change.wasAdded()) {
                    Pair<String, Binding> stat = viewModel.visible.get(fromIndex);
                    BorderPane pane = (BorderPane) statisticsView.getChildren().get(fromIndex);

                    pane.getChildren().remove(pane.getCenter());
                    pane.setCenter(createStatisticPane(stat.getLeft(), stat.getRight()));

                }

            }
        });

        int idx = 0;
        for (Pair<String, Binding> stat : viewModel.visible) {
            ((BorderPane) statisticsView.getChildren().get(idx++)).setCenter(createStatisticPane(stat.getLeft(), stat.getRight()));
        }
    }

    /**
     * Create statistic with given name and value
     * @param name name of the statistic
     * @param binding value of the statistic
     * @return Pane with name and value of the statistic
     */
    private Pane createStatisticPane(String name, Binding binding) {
        BorderPane statistic = new BorderPane();
        Text header = new Text(name);
        header.setFill(Color.web("#9EA5F3"));
        header.setStyle("-fx-font-weight: bold;");
        statistic.setTop(header);
        Label label = new Label();
        label.setId("value");
        label.textProperty().bind(binding);
        statistic.setCenter(label);
        setUpStatisticPane(statistic);
        return statistic;
    }

    /**
     * Set all essential properties of the statistic pane
     * @param statistic pane to set up
     */
    private void setUpStatisticPane(BorderPane statistic) {
        statistic.setPrefSize(200, 200);
        BorderPane.setAlignment(statistic.getTop(), Pos.CENTER);
        BorderPane.setMargin(statistic.getTop(), new Insets(12, 12, 12, 12));
        Label label = (Label) statistic.getCenter();
        label.setStyle("-fx-text-fill: black; -fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #7FFFD4, #ADD8E6); -fx-background-radius: 10px;");
        label.setPadding(new Insets(10));
    }

    /**
     * Changes the statistic displayed to the next one in the list
     * for the pane calling the method.
     * @param event Click event.
     */
    public void next(Event event) {
        int index = Integer.parseInt(((Node) event.getTarget()).getUserData().toString());
        viewModel.getNext(index);
    }

    /**
     * Changes the statistic displayed to the previous one in the list
     * for the pane calling the method.
     * @param event Click event.
     */
    public void previous(Event event) {
        int index = Integer.parseInt(((Node) event.getTarget()).getUserData().toString());
        viewModel.getPrevious(index);
    }
}
