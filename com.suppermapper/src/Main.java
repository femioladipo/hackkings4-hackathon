import javafx.application.Application;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import util.Util;
import view.MainView;

public class Main extends Application {
    /**
     * Start application
     *
     * @param args Launch options
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     *
     * @param primaryStage Initial window displayed.
     */
    @Override
    public void start(Stage primaryStage) {
        Parent root = Util.getViewRoot(MainView.class);
        root.getStylesheets().add("style.css");
        primaryStage.setTitle("SuperMapper");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setMinHeight(400);
        primaryStage.setMinWidth(500);
        primaryStage.show();
        
    }
}
