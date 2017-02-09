package ServerPackage;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		Parent root = null;
		
		try {
			FXMLLoader fxmlLoader = new FXMLLoader();
			root = fxmlLoader.load(getClass().getResource("ServerGUI.fxml").openStream());
			//controller = (StartController)fxmlLoader.getController();
			Scene loginScene = new Scene(root, 800, 1000);
			primaryStage.setScene(loginScene);
			primaryStage.show();
		}
		catch(IOException e) { }
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
