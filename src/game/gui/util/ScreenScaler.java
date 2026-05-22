package game.gui.util;
import javafx.beans.value.ChangeListener;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Scale;

public class ScreenScaler {

    /**
     * Wraps your root layout in a scalable, centered scene.
     * * @param gameRoot       Your main layout (e.g., BorderPane, AnchorPane, VBox)
     * @param designedWidth  The original width you designed the UI for (e.g., 1200)
     * @param designedHeight The original height you designed the UI for (e.g., 800)
     * @return A dynamically scalable Scene
     */
    public static Scene createScalableScene(Pane gameRoot, double designedWidth, double designedHeight) {
        
        // 1. Lock the game root to its exact designed resolution
        gameRoot.setPrefSize(designedWidth, designedHeight);
        gameRoot.setMinSize(designedWidth, designedHeight);
        gameRoot.setMaxSize(designedWidth, designedHeight);

        // 2. Wrap it in a Group. 
        // A Group prevents parent layout managers from trying to resize its children.
        Group scaleGroup = new Group(gameRoot);

        // 3. Place the Group in a StackPane.
        // A StackPane will automatically keep the Group perfectly centered in the window.
        StackPane windowRoot = new StackPane(scaleGroup);
        
        // Optional: Set the background color for the "letterbox" bars that appear 
        // if the user makes the window extremely wide or tall.
        windowRoot.setStyle("-fx-background-color: #1a1a1a;"); 

        // 4. Create the final Scene
        Scene scene = new Scene(windowRoot, designedWidth, designedHeight);

        // 5. Create the Scale Transform and attach it to your game layout
        Scale scale = new Scale(1, 1);
        scale.setPivotX(0);
        scale.setPivotY(0);
        gameRoot.getTransforms().add(scale);

        // 6. Listen for window size changes and recalculate the scale
        ChangeListener<Number> resizeListener = (observable, oldValue, newValue) -> {
            // Calculate how much the window has grown/shrunk compared to the design size
            double scaleX = scene.getWidth() / designedWidth;
            double scaleY = scene.getHeight() / designedHeight;
            
            // Use Math.min to ensure the aspect ratio is strictly maintained
            double finalScale = Math.min(scaleX, scaleY);
            
            // Apply the new scale
            scale.setX(finalScale);
            scale.setY(finalScale);
        };

        // Attach the listener to both width and height properties
        scene.widthProperty().addListener(resizeListener);
        scene.heightProperty().addListener(resizeListener);

        return scene;
    }
}