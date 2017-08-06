package seng302.v.map;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class TestMapController implements Initializable{

	@FXML
	private Canvas mapCanvas;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		GraphicsContext gc = mapCanvas.getGraphicsContext2D();
		Boundary bound = new Boundary(57.662943, 11.848501, 57.673945, 11.824966);
		CanvasMap canvasMap = new CanvasMap(bound);
		gc.drawImage(canvasMap.getMapImage(), 0, 0, canvasMap.getWidth(), canvasMap.getHeight());
	}
}
