/*
 * Copyright (C) 2015, 2016  Green Screens Ltd.
 */
package io.greenscreens.application;
	
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;

/**
 * Main JavaFX GUI app
 * 
 * NOTE: To switch to async mode, replace caller class in Sample.xml
 *
 */
public class ApplicationGUI extends Application {
	
	public static ApplicationGUI app;
	public static Stage stage;
	
	@Override
	public void start(Stage primaryStage) {
		app = this;
		stage = primaryStage;
		
		try {
			VBox root = (VBox)FXMLLoader.load(getClass().getResource("Sample.fxml"));
			Scene scene = new Scene(root,700,400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
