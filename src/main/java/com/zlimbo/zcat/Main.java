package com.zlimbo.zcat;

import com.zlimbo.zcat.connect.ConnectionLog;
import com.zlimbo.zcat.gui.MainWindow;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends Application {

    /**
     * 日志
     */
    final Logger logger = LoggerFactory.getLogger(getClass());


    public static void main(String[] args) {
        ConnectionLog.load();
        launch(args);
        ConnectionLog.save();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        logger.debug("[start] start");

//        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainWindow.fxml"));

//        primaryStage.setScene(new Scene(root));

        Parent root = new MainWindow();
        Scene scene =  new Scene(root, 1200, 800);
        primaryStage.setScene(scene);

        Application.setUserAgentStylesheet(STYLESHEET_MODENA);
        primaryStage.setTitle("zCat");
        primaryStage.getIcons().add(new Image(
                getClass().getResourceAsStream("/image/ouyeel_bc.png")));
        primaryStage.show();

        logger.debug("[start] end");
    }
}
