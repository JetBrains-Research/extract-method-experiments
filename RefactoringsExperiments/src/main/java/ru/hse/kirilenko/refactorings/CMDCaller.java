package ru.hse.kirilenko.refactorings;
/*import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;*/
import ru.hse.kirilenko.refactorings.csv.SparseCSVBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class CMDCaller {
    /**
     Calls for <code>ExtractionRunner</code> from command-line
     */
    public static void main(String[] args){
        String path = args[0];
        System.out.println(path);
        File file = new File(path);

        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            List<String> repos = new ArrayList<>();
            br.lines().forEach(s -> repos.add(s));

            SparseCSVBuilder.sharedInstance = new SparseCSVBuilder("true.csv", ExtractionConfig.nFeatures);
            ExtractionRunner runner = new ExtractionRunner(repos);
            new Thread(() -> runner.run()).start();

        } catch(Exception e) {
            String errormsg = String.format("Warning, there is no such file: %s\nExiting...", path);
            System.out.println(errormsg);
            System.exit(0);
        }
    }
}