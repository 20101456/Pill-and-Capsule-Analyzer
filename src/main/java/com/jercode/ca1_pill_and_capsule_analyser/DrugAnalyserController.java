package com.jercode.ca1_pill_and_capsule_analyser;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PipedWriter;
import java.util.*;

public class DrugAnalyserController {
    @FXML private Label welcomeText;
    @FXML private ImageView myImageView;
    @FXML private ImageView bwImageView;
    @FXML private ImageView RecImageView;
    @FXML private Canvas canvas;
    @FXML private MenuItem chooseFile;
    @FXML private String imageFileName;
    @FXML private Image originalImage;
    @FXML private ComboBox<String> comboBox;
    @FXML private Button toggleViewButton;
    @FXML private Button drawBoxesButton;
    @FXML private Slider hueSlider;
    @FXML private Slider saturationSlider;
    @FXML private Slider brightnessSlider;
    @FXML private Label hueValueLabel;
    @FXML private Label saturationValueLabel;
    @FXML private Label brightnessValueLabel;
    @FXML private Label componentValueLabel;
    @FXML private Label averageSizeLabel;
    @FXML private Slider minSizeSlider;
    @FXML private Slider maxSizeSlider;
    @FXML private Label minSizeValueLabel;
    @FXML private Label maxSizeValueLabel;
    @FXML private MenuItem closeMenuItem;
    @FXML protected void onComboBoxChange() {
        String selectedItem=comboBox.getSelectionModel().getSelectedItem();
        welcomeText.setText("You selected: " + selectedItem);
    }

    private File file; //reference currently loaded image file
    private boolean showingComponents=false; //tracks witch view is currently displayed
    private WritableImage bwImage; //member variable for black and white image
    private UniFindOp ufo; //member variable for Union-Find operations
    private Color lastSampledColor;


    public void initialize() { //sets up listeners and handlers for UI components.
        chooseFile.setOnAction(event -> chooseImage());
        toggleViewButton.setOnAction(actionEvent -> onToggleViewClicked());
        myImageView.setOnMouseClicked(this::onImageClick);
        drawBoxesButton.setOnAction(actionEvent -> onDrawBoxesClicked());

        //listeners for slider labels
        hueSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (lastSampledColor!=null) {
                convertToBlackAndWhiteImage(lastSampledColor);
            }
            hueValueLabel.setText(String.format("%.2f", newValue.doubleValue()));
        });
        saturationSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (lastSampledColor!=null) {
                convertToBlackAndWhiteImage(lastSampledColor);
            }
            saturationValueLabel.setText(String.format("%.2f", newValue.doubleValue()));
        });
        brightnessSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (lastSampledColor!=null) {
                convertToBlackAndWhiteImage(lastSampledColor);
            }
            brightnessValueLabel.setText(String.format("%.2f", newValue.doubleValue()));
        });
        minSizeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (showingComponents) {
                displayColoredComponents();
            }
            minSizeValueLabel.setText(String.format("%.2f", newValue.doubleValue()));
        });
        maxSizeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (showingComponents) {
                displayColoredComponents();
            }
            maxSizeValueLabel.setText(String.format("%.2f", newValue.doubleValue()));
        });

        //close app
        closeMenuItem.setOnAction(event -> {
            Platform.exit();
        });
    }

    //file chooser loads image into myImageView
    private void chooseImage() {
        Stage stage=(Stage) myImageView.getScene().getWindow();
        FileChooser fileChooser=new FileChooser();
        //extension filters
        FileChooser.ExtensionFilter imageFilter=new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg");
        fileChooser.getExtensionFilters().add(imageFilter);
        //open file chooser dialog
        this.file=fileChooser.showOpenDialog(stage);

        if (file!=null) {
            try {
                originalImage = new Image(new FileInputStream(file));
                myImageView.setImage(originalImage);
                System.out.println("File loaded: " + file.getAbsolutePath());
            } catch (FileNotFoundException e) {
                System.err.println("File not found: " + e.getMessage());
                Alert alert =new Alert(Alert.AlertType.ERROR, "Could not load the image file: " + file.getName());
                alert.showAndWait();
            } catch (IllegalArgumentException | NullPointerException e) {
                System.err.println("Invalid image file: " + e.getMessage());
                Alert alert=new Alert(Alert.AlertType.ERROR, "Invalid image file selected. Please choose a valid image.");
                alert.showAndWait();
            }
        } else {
            System.out.println("File Selection Cancelled");
        }
    }

    private void onImageClick(MouseEvent event) { //Handles clicks to sample colors and start image processing
        if (originalImage == null) {
            System.out.println("No image loaded.");
            return;
        }
        double x = event.getX() * originalImage.getWidth()/myImageView.getBoundsInLocal().getWidth();
        double y = event.getY() * originalImage.getHeight()/myImageView.getBoundsInLocal().getHeight();
        PixelReader pixelReader=originalImage.getPixelReader();
        lastSampledColor=pixelReader.getColor((int) x, (int) y);
        System.out.println("Clicked color: " + lastSampledColor);
        convertToBlackAndWhiteImage(lastSampledColor);
    }

    private void convertToBlackAndWhiteImage(Color sampledColor) {
        int width=(int) originalImage.getWidth();//Get dimensions of originalImage
        int height=(int) originalImage.getHeight();
        bwImage= new WritableImage(width, height);//create a new writable image for the black and white version
        PixelWriter pixelWriter=bwImage.getPixelWriter();
        PixelReader pixelReader=originalImage.getPixelReader();
        ufo = new UniFindOp(width*height);

        for (int y=0; y<height;y++) {
            for (int x =0; x<width; x++) {
                Color color=pixelReader.getColor(x,y);
                int index=y * width + x;

                if (colorSimilarity(color, sampledColor)) {
                    pixelWriter.setColor(x, y, Color.WHITE);

                    if (x>0 && colorSimilarity(pixelReader.getColor(x-1, y), sampledColor)) {
                        ufo.union(index, index-1);
                    }
                    if (y>0 && colorSimilarity(pixelReader.getColor(x, y - 1), sampledColor)) {
                        ufo.union(index,index-width);
                    }
                } else {
                    pixelWriter.setColor(x, y, Color.BLACK);
                }
            }
        }
        bwImageView.setImage(bwImage);//updates the ImageView with the new black and white image
    }

    @FXML
    protected void onToggleViewClicked() {// Toggles between black and white image and colored components
        if (showingComponents) {
            bwImageView.setImage(bwImage);
        } else {
            displayColoredComponents();
        }
        showingComponents=!showingComponents;
    }

    private void displayColoredComponents() { // displays components in random colors based on their significance and size
        int width=(int) originalImage.getWidth();
        int height=(int) originalImage.getHeight();
        WritableImage colorComponentsImage=new WritableImage(width, height);
        PixelWriter writer=colorComponentsImage.getPixelWriter();

        Map<Integer, Color> componentColors = new HashMap<>();
        Map<Integer, Integer> componentSizes=new HashMap<>(); //stores sizes of each component
        Random rand=new Random();
        Set<Integer> displayedComponents=new HashSet<>();//store roots of displayed components

        double minSize=minSizeSlider.getValue();
        double maxSize=maxSizeSlider.getValue();
        //first pass to identify significant components and assign colors
        for (int y=0; y<height; y++) {
            for (int x=0; x < width; x++) {
                int index = y * width + x;
                int root = ufo.find(index);
                componentSizes.put(root, componentSizes.getOrDefault(root, 0) + 1); //counter, Increments size of the component

                if (!componentColors.containsKey(root)) {
                    if (!ufo.isSignificant(root, minSize, maxSize)) {
                        componentColors.put(root, new Color(rand.nextDouble(), rand.nextDouble(), rand.nextDouble(), 1.0));
                        displayedComponents.add(root);
                    } else {
                        componentColors.put(root, Color.BLACK);
                    }
                }
            }
            bwImageView.setImage(colorComponentsImage);
        }
        //second pass to apply colors based on component significance
        for (int y=0; y<height; y++) {
            for (int x=0; x<width; x++) {
                int index = y*width + x;
                int root = ufo.find(index);
                Color color=componentColors.getOrDefault(root, Color.BLACK);
                writer.setColor(x, y, color);
            }
        }
        bwImageView.setImage(colorComponentsImage);

        int totalSize=0;
        for (int root:displayedComponents) {
            totalSize+=componentSizes.get(root);
        }
        componentValueLabel.setText(" " + displayedComponents.size());
        //calculate average size of displayed components
        double averageSize=displayedComponents.size() > 0 ? (double) totalSize/displayedComponents.size() : 0;
        averageSizeLabel.setText(" " + String.format("%.2f", averageSize));
    }

    private boolean colorSimilarity(Color color1, Color color2) {
        double hueThreshold=hueSlider.getValue();
        double saturationThreshold=saturationSlider.getValue();
        double brightnessThreshold=brightnessSlider.getValue();

        double hueDifference=Math.abs(color1.getHue() - color2.getHue());
        double saturationDifference=Math.abs(color1.getSaturation() - color2.getSaturation());
        double brightnessDifference=Math.abs(color1.getBrightness() - color2.getBrightness());

        return hueDifference<hueThreshold && saturationDifference <= saturationThreshold && brightnessDifference<= brightnessThreshold;
    }

    @FXML
    protected void onDrawBoxesClicked() {
        GraphicsContext gc=canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());//clears previous recs
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(2);

        //static test rectangle
        gc.strokeRect(10, 10, 50, 50);

        RecImageView.setImage(originalImage); //Display the original image with rectangles
        canvas.toFront(); //bring the canvas to the front to show rectangles
    }

}
