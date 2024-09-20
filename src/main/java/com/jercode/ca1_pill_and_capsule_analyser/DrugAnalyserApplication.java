package com.jercode.ca1_pill_and_capsule_analyser;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class DrugAnalyserApplication extends Application {

    public static Stage primStage;
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(DrugAnalyserApplication.class.getResource("drug-analyser-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700, 700);
        stage.setTitle("Drug Analyser");
        stage.setScene(scene);
        stage.show();
        DrugAnalyserApplication.primStage=stage;
    }

    public static void main(String[] args) {
        launch();
    }
}

