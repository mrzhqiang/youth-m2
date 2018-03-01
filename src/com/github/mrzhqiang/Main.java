package com.github.mrzhqiang;

import javafx.application.Application;
import javafx.stage.Stage;

import javax.swing.*;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        JFrame frame = new JFrame("RandallCenter");
        frame.setContentPane(new RandallCenter().root);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
