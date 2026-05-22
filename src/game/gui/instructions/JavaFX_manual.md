# JavaFX Library: Developing Applications Using JavaFX

**CSEN 401 - Computer Programming Lab**  
**Lecture 6 - JavaFX**

**Instructors:**
- Prof. Dr. Slim Abdennadher
- Dr. Nourhan Ehab
- Dr. Ahmed Abdelfattah

Faculty of Media Engineering and Technology

---

## Table of Contents
1. JavaFX Library
2. Developing Applications Using JavaFX
3. Recap

---

## 1. JavaFX Library

### GUI Design Using JavaFX

- **JavaFX** is a Java library and a GUI toolkit designed to develop and facilitate web and desktop applications.
- Applications written using JavaFX can run over multiple operating systems (Windows, Linux, iOS, Android), and across different platforms (Desktop, Web, Mobile Phones, Tablets, etc).
- Introduced to supersede the Java Swing library offering more features all in one library.

### JavaFX Architecture

The architecture consists of the following layers:

1. **JavaFX API**: Implements all the required classes that are capable of producing a full-featured JavaFX application with rich graphics.

2. **Scene Graph**: The starting point of GUI development using JavaFX. Made up of a collection of nodes in a tree structure representing the UI components.

3. **Quantum Toolkit**: Ties the components of the graphics engine together and makes them available to JavaFX.

4. **JavaFX Graphics Engine**: Responsible of the hardware-accelerated graphics rendering, embedding web pages and media components, and interfacing with the OS.

Below these layers are:
- **JDK API and Tools**
- **Java Virtual Machine**

The underlying graphics engine uses Prism, Class, Web view, Media, Win32 GTK, OpenGL D3D, Web kit, and C Streams.

### JavaFX Application Structure

JavaFX applications follow a hierarchical structure:

```
Stage (Window)
  └─ Scene (Physical Contents)
      └─ Scene Graph (Tree Structure)
          ├─ Root Node
          │   ├─ Branch Node
          │   │   ├─ Leaf Node
          │   │   └─ Leaf Node
          │   └─ Leaf Node
```

**Key Components:**

1. **Stage**: A window containing all objects in the application. Represented by the `Stage` class in `javafx.stage`.

2. **Scene**: The physical contents of the application in the form of a Scene Graph. Represented by the `Scene` class in `javafx.scene`.

3. **Scene Graph**: A tree-like data structure representing the contents of a scene. Each node corresponds to a graphical object in the scene. A graphical object can be a container, a UI component, a 2D/3D object, or a media object.

### Life Cycle of a JavaFX Application

All JavaFX classes must extend `Application`. There are four methods in this super class:

1. **`init()`**: An initialization method. It can be overridden if needed.

2. **`start(Stage primaryStage)`**: The entry point method of the JavaFX application where all the graphics code of JavaFX is to be written. This method **must be overridden**.

3. **`stop()`**: A clean-up method. It can be overridden if needed.

4. **`launch(String[] s)`**: A static method that launches the application. Generally called in the main method.

---

## 2. Developing Applications Using JavaFX

### First JavaFX Application

There are **5 steps** that should be followed:

1. Build the **Scene Graph** with root `root`.
2. Create a new **Scene** with `root` (and optionally its width and height).
3. Add the created **Scene** to the **Stage**.
4. Show the stage.
5. Call `launch` in the main method.

**Example Code:**

```java
public class Main extends Application {
    public void start(Stage primaryStage) throws Exception {
        // Step 1: Build Scene Graph
        Group root = new Group();
        Label l = new Label("Hello World!");
        root.getChildren().add(l);
        
        // Step 2: Create Scene
        Scene s = new Scene(root, 1000, 800);
        
        // Step 3: Add Scene to Stage
        primaryStage.setScene(s);
        primaryStage.setTitle("First FX Application");
        
        // Step 4: Show Stage
        primaryStage.show();
    }
    
    // Step 5: Launch in main
    public static void main(String[] args) {
        launch(args);
    }
}
```

### JavaFX Layouts

Layouts describe how the UI elements are to be viewed on the screen. Some built-in layouts are:

1. **VBox**: Places elements vertically.
2. **HBox**: Places elements horizontally.
3. **StackPane**: Places elements in the center on top of one another.
4. **BorderPane**: Separates the window into top, bottom, center, left, right.
5. **GridPane**: Arranges the elements in a grid of rows and columns.

#### VBox Example

```java
public void start(Stage primaryStage) throws Exception {
    VBox root = new VBox();
    root.setSpacing(20); // Define vertical spacing
    Label l1 = new Label("Hello World!");
    Label l2 = new Label("Hello World 2!");
    root.getChildren().addAll(l1, l2);
    
    Scene s = new Scene(root, 1000, 600);
    primaryStage.setScene(s);
    primaryStage.show();
}
```

#### HBox Example

```java
public void start(Stage primaryStage) throws Exception {
    HBox root = new HBox();
    root.setSpacing(20); // Define horizontal spacing
    Label l1 = new Label("Hello World!");
    Label l2 = new Label("Hello World 2!");
    root.getChildren().addAll(l1, l2);
    
    Scene s = new Scene(root, 1000, 600);
    primaryStage.setScene(s);
    primaryStage.show();
}
```

#### StackPane Example

```java
public void start(Stage primaryStage) throws Exception {
    StackPane root = new StackPane();
    Label l1 = new Label("Hello World!");
    Label l2 = new Label("Hello World 2!");
    l2.setTranslateX(400);
    root.getChildren().addAll(l1, l2);
    
    Scene s = new Scene(root, 1000, 600);
    primaryStage.setScene(s);
    primaryStage.show();
}
```

#### BorderPane Example

```java
public void start(Stage primaryStage) throws Exception {
    BorderPane root = new BorderPane();
    Label l1 = new Label("Top");
    Label l2 = new Label("Bottom");
    Label l3 = new Label("Center");
    Label l4 = new Label("Left");
    Label l5 = new Label("Right");
    
    root.setTop(l1);
    root.setBottom(l2);
    root.setCenter(l3);
    root.setRight(l4);
    root.setLeft(l5);
    
    Scene s = new Scene(root, 1000, 600);
    primaryStage.setScene(s);
    primaryStage.show();
}
```

#### GridPane Example

```java
public void start(Stage primaryStage) throws Exception {
    GridPane root = new GridPane();
    Label l1 = new Label("Text 1");
    Label l2 = new Label("Text 2");
    Label l3 = new Label("Text 3");
    Label l4 = new Label("Text 4");
    
    root.add(l1, 0, 0);
    root.add(l2, 1, 0);
    root.add(l3, 0, 1);
    root.add(l4, 1, 1);
    
    Scene s = new Scene(root, 1000, 600);
    primaryStage.setScene(s);
    primaryStage.show();
}
```

#### Nesting Layouts Example

```java
public void start(Stage primaryStage) throws Exception {
    GridPane root = new GridPane();
    HBox h = new HBox();
    Label l1 = new Label("Text 1");
    Label l2 = new Label("Text 2");
    Label l3 = new Label("Text 3");
    Label l4 = new Label("Text 4");
    
    h.getChildren().addAll(l1, l2);
    root.add(h, 0, 0);
    root.add(l3, 0, 1);
    root.add(l4, 1, 1);
    
    Scene s = new Scene(root, 1000, 600);
    primaryStage.setScene(s);
    primaryStage.show();
}
```

### JavaFX UI Components

Some available UI Components:

- **Label**
- **Button**
- **CheckBox**
- **TextField**
- **RadioButton**
- **ProgressBar**
- **FileChooser**
- **Menu**

#### Example: Layout and UI Components with Event Handling

```java
public void start(Stage primaryStage) throws Exception {
    GridPane root = new GridPane();
    Label l = new Label();
    Button b1 = new Button("Press Me");
    Button b2 = new Button("Press Me");
    root.add(b1, 0, 0);
    root.add(b2, 1, 0);
    
    // Add listener to b1
    b1.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            l.setText("0,0");
        }
    });
    
    // Add listener to b2
    b2.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            l.setText("0,1");
        }
    });
    
    Scene s = new Scene(root, 1000, 800);
    primaryStage.setScene(s);
    primaryStage.setTitle("First FX Application");
    primaryStage.show();
}
```

### Shapes and Colors

```java
public void start(Stage primaryStage) throws Exception {
    VBox root = new VBox();
    Circle c = new Circle(100, 100, 50);
    Button b1 = new Button("Change Color!");
    
    b1.setOnMouseClicked(new EventHandler<Event>() {
        int i = 0;
        
        @Override
        public void handle(Event event) {
            if (i % 2 == 0)
                c.setFill(Color.RED);
            else
                c.setFill(Color.BLACK);
            i++;
        }
    });
    
    root.getChildren().addAll(c, b1);
    Scene s = new Scene(root, 1000, 600);
    primaryStage.setScene(s);
    primaryStage.show();
}
```

### Adding Images to Buttons

```java
public void start(Stage primaryStage) throws Exception {
    VBox root = new VBox();
    Image img = new Image("image.jpg");
    ImageView view = new ImageView(img);
    view.setFitHeight(100);
    view.setFitWidth(100);
    Button b = new Button();
    b.setGraphic(view);
    b.setPrefSize(100, 100);
    root.getChildren().add(b);
    
    Scene s = new Scene(root, 1000, 600);
    primaryStage.setScene(s);
    primaryStage.show();
}
```

### Playing Audio

```java
public void start(Stage primaryStage) throws Exception {
    String path = "test.mp3";
    Media media = new Media(new File(path).toURI().toString());
    MediaPlayer mediaPlayer = new MediaPlayer(media);
    mediaPlayer.setAutoPlay(true);
    primaryStage.setTitle("Playing Audio");
    primaryStage.show();
}
```

### Adding Animation

```java
public void start(Stage primaryStage) throws Exception {
    Rectangle rect1 = new Rectangle(20, 20, 200, 150);
    rect1.setFill(Color.RED);
    TranslateTransition translate = new TranslateTransition();
    translate.setByX(100);
    translate.setDuration(Duration.millis(1000));
    translate.setCycleCount(10);
    translate.setAutoReverse(true);
    translate.setNode(rect1);
    translate.play();
    
    Group root = new Group();
    root.getChildren().add(rect1);
    Scene scene = new Scene(root, 500, 400);
    primaryStage.setScene(scene);
    primaryStage.setTitle("Translation Example");
    primaryStage.show();
}
```

---

## 3. Recap

### Points To Take Home

- JavaFX Architecture and Application Structure
- Building JavaFX Stages and Scenes
- JavaFX Layouts
- JavaFX UI Components and Event Handling
- JavaFX 2D Shapes and Colors
- Playing Audio
- Adding Animation

### Additional Resources

For more JavaFX features and sample code, visit:  
https://www.javatpoint.com/javafx-tutorial
