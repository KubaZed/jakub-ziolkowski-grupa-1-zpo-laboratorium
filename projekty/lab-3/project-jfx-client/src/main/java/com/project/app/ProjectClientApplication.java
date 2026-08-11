package com.project.app;

import com.project.controller.ProjectController;
import com.project.dao.ProjektDAO;
import com.project.dao.ProjektDAOImpl;
import com.project.datasource.DbInitializer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ProjectClientApplication extends Application {
    private Parent root;
    private FXMLLoader loader;

    public static void main(String[] args) {
        DbInitializer.init();
        launch(ProjectClientApplication.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml/ProjectFrame.fxml"));
        
        // Tworzenie obiektu DAO i przekazanie go do kontrolera przez fabrykę
        ProjektDAO projektDAO = new ProjektDAOImpl();
        loader.setControllerFactory(controllerClass -> new ProjectController(projektDAO));
        
        root = loader.load();
        primaryStage.setTitle("Projekty");
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
        
        // Pobranie kontrolera i rejestracja automatycznego zamykania wątków
        ProjectController controller = loader.getController();
        primaryStage.setOnCloseRequest(event -> {
            controller.shutdown();
            Platform.exit();
        });
        
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
    }
}