package util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class Util {
    private static Util ourInstance = new Util();

    /**
     * Singleton constructor.
     */
    private Util() {
    }

    /**
     * Returns singleton instance.
     *
     * @return Singleton instance.
     */
    public static Util getInstance() {
        return ourInstance;
    }

    /**
     * Returns an elements root pane.
     *
     * @param c View class.
     * @return Root pane.
     */
    public static Parent getViewRoot(Class c) {
        try {
            String[] tmp = c.getName().split("\\.");
            return FXMLLoader.load(c.getResource(tmp[tmp.length - 1] + ".fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns a views associated pane.
     *
     * @param name Name of view
     * @return Root pane.
     */
    public static Parent getView(String name) {
        try {
            return FXMLLoader.load(FXMLLoader.class.getResource("/view/" + name + ".fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Loads a view from fxml and also returns the controlling class associated with it.
     *
     * @param c Class of view.
     * @return Pair of Controller class (left), and associated pane (right).
     */
    public static <T> Pair<T, Parent> getViewWithController(Class c) {
        try {
            String[] tmp = c.getName().split("\\.");
            FXMLLoader loader = new FXMLLoader(c.getResource(tmp[tmp.length - 1] + ".fxml"));

            Parent parent = loader.load();
            T controller = loader.getController();
            return Pair.of(controller, parent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Loads a fxml class and injects a class that will be controlling it. And returns the Controller and pane.
     *
     * @param aClass The type of the class that will be created and injected.
     * @param args The arguments that will be passed to the constructor of the object.
     * @return Pair of Controller class (left), and associated pane (right).
     */
    public static <T> Pair<T, Parent> getViewWithControllerArguments(Class aClass, Object... args) {
        try {
            String[] tmp = aClass.getName().split("\\.");
            FXMLLoader loader = new FXMLLoader(aClass.getResource(tmp[tmp.length - 1] + ".fxml"));
            loader.setController(aClass.getConstructor(Arrays.stream(args).map(arg -> arg.getClass()).toArray(size -> new Class[size])).newInstance(args));
            Parent parent = loader.load();
            T controller = loader.getController();
            return Pair.of(controller, parent);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Loads a fxml class and injects a class that will be controlling it. Buy it doesn't return the Controller.
     *
     * @param aClass The type of the class that will be created and injected.
     * @param args The arguments that will be passed to the constructor of the object.
     * @return Root pane.
     */
    public static Parent getViewWithArguments(Class aClass, Object... args) {
        try {
            String[] tmp = aClass.getName().split("\\.");
            FXMLLoader loader = new FXMLLoader(aClass.getResource(tmp[tmp.length - 1] + ".fxml"));

            loader.setController(ConstructorUtils.invokeConstructor(aClass, args));

            Parent parent = loader.load();
            return parent;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }
}
