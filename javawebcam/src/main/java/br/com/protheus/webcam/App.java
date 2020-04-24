package br.com.protheus.webcam;

import java.io.ByteArrayInputStream;
import java.util.Arrays;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.*;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.scene.control.ColorPicker;

import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


/**
 *@author  Wellington Gabriel Oliveira de Carvalho
 *@version 0.0.1
 *@date April 23st, 2020
 **/
public class App extends Application{

	int i = 1;
	Mat maskframe = new Mat();
	double red = 124, green = 26, blue = 23;
	int thresholdR = 0, thresholdG = 0, thresholdB = 0;
	PixelReader px;
	Color color;
	Image img = new Image("file:assets/img/imagem2.png");
	double[] rgb;
	double[] branco = {255.0,255.0,255.0};
	double[] preto = {0.0,0.0,0.0};

    public static void main( String[] args ) throws Exception{
    	System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // Carrega as bibliotecas nativas do opencv
    	launch(args);
    	
    }

    @Override
    public void start(Stage primaryStage) throws Exception, InterruptedException{
    	primaryStage.setTitle("Original");

    	Stage secondaryStage = new Stage();
    	secondaryStage.setTitle("Máscara");

    	var b = true;
    	
    	var frame = new Mat();
    	var byteframe = new MatOfByte();
    	var bytemask = new MatOfByte();
    	var video = new VideoCapture(0);

    	var imgvw = new ImageView(img);
    	imgvw.setFitHeight(240);
    	imgvw.setFitWidth(320);
    	imgvw.setPreserveRatio(true);
    	imgvw.setOnMouseClicked(e -> {
    		px = imgvw.getImage().getPixelReader();
    		color = px.getColor((int)e.getX(),(int)e.getY());
    		red = color.getRed() * 255;
    		green = color.getGreen() * 255; 
    		blue = color.getBlue() * 255;
    		System.out.println(red + " " + green + " " + blue);
    	});

    	var imgvw2 = new ImageView(img);
    	imgvw2.setFitHeight(240);
    	imgvw2.setFitWidth(320);
    	imgvw2.setPreserveRatio(true);

    	var button = new Button();
    	button.setText("Click me");
    	button.setOnAction(e -> {
    		new Thread(new Runnable(){
    			@Override
    			public void run(){
    				try{
    					if(video.isOpened()){
    						while(true){
    							video.read(frame);
    							Imgproc.resize(frame, frame, new Size(320,240));
    							if(!frame.empty()){
    								maskframe = frame.clone();
    								for(int j = 0; j < frame.cols(); j++){
    									for(int k = 0; k < frame.rows(); k++){
    										rgb = frame.get(k,j);
    										double r = (rgb[2] - red > 0) ? (rgb[2] - red): -(rgb[2] - red);
    										double g = (rgb[1] - green > 0) ? (rgb[1] - green): -(rgb[1] - green);
    										double b = (rgb[0] - blue > 0) ? (rgb[0] - blue): -(rgb[0] - blue);
    										if(r < thresholdR && g < thresholdG && b < thresholdB){
    											maskframe.put(k,j,branco);
    										}else{
    											maskframe.put(k,j,preto);
    										}
    									}
    								}
    								Imgcodecs.imencode(".jpg", frame, byteframe);
    								Imgcodecs.imencode(".jpg", maskframe, bytemask);
    								Platform.runLater(() -> {
    									img  = new Image(new ByteArrayInputStream(byteframe.toArray()));
    									imgvw.setImage(img);
    									imgvw2.setImage(new Image(new ByteArrayInputStream(bytemask.toArray())));
    								});
    							}
    							Thread.sleep(66);
    							//System.out.println(thresholdR + " " + thresholdG + " " + thresholdB + " ");
    						}
    					}
    				}catch(Exception ex){
    					System.out.println(ex);
    				}
    			}
    		}).start();
    	});

    	var button2 = new Button();
    	button2.setText("R+");
    	button2.setOnAction(e -> {
    		thresholdR+=1;
    		System.out.println(thresholdR);
    	});
    	var button3 = new Button();
    	button3.setText("R-");
    	button3.setOnAction(e -> thresholdR-=1);

    	var button4 = new Button();
    	button4.setText("G+");
    	button4.setOnAction(e -> thresholdG+=1);
    	var button5 = new Button();
    	button5.setText("G-");
    	button5.setOnAction(e -> thresholdG-=1);

    	var button6 = new Button();
    	button6.setText("B+");
    	button6.setOnAction(e -> thresholdB+=1);
    	var button7 = new Button();
    	button7.setText("B-");
    	button7.setOnAction(e -> thresholdB-=1);

    	var bottom = new HBox();
    	bottom.getChildren().addAll(button, button2, button3, button4, button5, button6, button7);

    	var layout = new BorderPane();
    	layout.setCenter(imgvw);
    	layout.setBottom(bottom);

    	var layout2 = new BorderPane();
    	layout2.setCenter(imgvw2);

    	var scene2 = new Scene(layout2, 320, 240);

    	var scene = new Scene(layout, 400, 320);

    	primaryStage.setScene(scene);
    	primaryStage.show();

    	secondaryStage.setScene(scene2);
    	secondaryStage.show();
    }
}
