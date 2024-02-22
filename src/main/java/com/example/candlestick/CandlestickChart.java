package com.example.candlestick;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CandlestickChart extends Application {

    private final int candleWidth = 5; // Width of each candlestick
    private final int spaceBetweenCandles = 2; // Space between candles
    private final int chartHeight = 300; // Height of the chart
    private final int chartWidth = 1000; // Width of the chart
    private double zoomFactor = 1.0; // Initial zoom factor
    private double scrollPosition = 0.0; // Initial scroll position

    private List<Candle> candles;

    @Override
    public void start(Stage primaryStage) {
        // Generate sample candlestick data
        generateCandlestickData();

        // Create Canvas for drawing candles
        Canvas canvas = new Canvas(chartWidth, chartHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Create ScrollPane for scrolling and zooming
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(canvas);

        // Create StackPane to hold ScrollPane
        StackPane root = new StackPane();
        root.getChildren().add(scrollPane);

        // Create Scene
        Scene scene = new Scene(root, 800, 400);

        // Add event listeners for zooming and scrolling
        scrollPane.viewportBoundsProperty().addListener((observable, oldValue, newValue) -> {
            zoomFactor = newValue.getWidth() / chartWidth;
            scrollPosition = scrollPane.getHvalue();
            drawCandles(gc);
        });

        // Set up the stage
        primaryStage.setTitle("Candlestick Chart");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Draw initial candles
        drawCandles(gc);
    }

    // Method to generate sample candlestick data
    private void generateCandlestickData() {
        candles = pricesFromCSV("XAUUSD_2023_01.csv");
//        candles = new ArrayList<>();
//        double[] open = new double[10000];
//        double[] high = new double[10000];
//        double[] low = new double[10000];
//        double[] close = new double[10000];
//        int volume = 1000;
//        Arrays.fill(open, 100.0);
//        Arrays.fill(high, 110.0);
//        Arrays.fill(low, 90.0);
//        Arrays.fill(close, 105.0);
//        candles.add(new Candle(open, high, low, close, volume));
    }

    private List<Candle> pricesFromCSV(String fileName) {
        BufferedReader bufferedReader = null;
        List<Candle> list = new ArrayList<>();
        String line;
        try {
            bufferedReader = new BufferedReader(new FileReader(fileName));
            while ((line = bufferedReader.readLine()) != null) {
                String[] row = line.split(",");
                list.add(new Candle(new double[]{Double.parseDouble(row[2]), Double.parseDouble(row[2])}, new double[]{Double.parseDouble(row[3]), Double.parseDouble(row[3])}, new double[]{Double.parseDouble(row[4]), Double.parseDouble(row[4])}, new double[]{Double.parseDouble(row[5]), Double.parseDouble(row[5])}, Integer.parseInt(row[6])));
            }
            bufferedReader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    // Method to draw candles
    private void drawCandles(GraphicsContext gc) {
        // Clear canvas
        gc.clearRect(0, 0, chartWidth, chartHeight);

        // Calculate the number of candles to be displayed based on zoom factor and scroll position
        int startIdx = (int) (scrollPosition * (candles.get(0).open.length - 1));
        int endIdx = (int) ((scrollPosition + zoomFactor) * (candles.get(0).open.length - 1));
        int numCandles = endIdx - startIdx;

        // Calculate candle width based on zoom factor
        double scaledCandleWidth = candleWidth * zoomFactor;
        double space = spaceBetweenCandles * zoomFactor;

        // Calculate the range of candle values
        double minCandleValue = 1700; // Minimum candle value
        double maxCandleValue = 2100; // Maximum candle value
        double candleValueRange = maxCandleValue - minCandleValue;

        // Draw candles
        for (int i = startIdx; i < endIdx; i++) {
            double x = (i - startIdx) * (scaledCandleWidth + space);

            // Scale candle dimensions to fit within the chart height
            double yOpen = chartHeight * (1 - (candles.get(0).open[i] - minCandleValue) / candleValueRange);
            double yClose = chartHeight * (1 - (candles.get(0).close[i] - minCandleValue) / candleValueRange);
            double yHigh = chartHeight * (1 - (candles.get(0).high[i] - minCandleValue) / candleValueRange);
            double yLow = chartHeight * (1 - (candles.get(0).low[i] - minCandleValue) / candleValueRange);

            // Draw candlestick body
            if (candles.get(0).close[i] > candles.get(0).open[i]) {
                gc.setFill(Color.GREEN); // Bullish candlestick
                gc.fillRect(x, yClose, scaledCandleWidth, yOpen - yClose);
            } else {
                gc.setFill(Color.RED); // Bearish candlestick
                gc.fillRect(x, yOpen, scaledCandleWidth, yClose - yOpen);
            }

            // Draw candlestick wicks
            gc.setStroke(Color.BLACK);
            gc.strokeLine(x + scaledCandleWidth / 2, yHigh, x + scaledCandleWidth / 2, yLow);
        }

        // Draw scale
        drawScale(gc, startIdx, endIdx, 1700, 2100);
    }

    // Method to draw scale
    private void drawScale(GraphicsContext gc, int startIdx, int endIdx, double minValue, double maxValue) {
        int numTicks = 10; // Number of ticks on the scale
        double tickSpacing = (endIdx - startIdx) / (double) numTicks;
        double scaledWidth = chartWidth * zoomFactor;

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeLine(0, chartHeight, scaledWidth, chartHeight); // X-axis

        // Draw ticks and labels
        for (int i = 0; i <= numTicks; i++) {
            double x = i * tickSpacing * (candleWidth + spaceBetweenCandles) / (endIdx - startIdx);
            gc.strokeLine(x, chartHeight - 5, x, chartHeight + 5); // Tick mark
            gc.fillText(Integer.toString((int) (minValue + (maxValue - minValue) * (startIdx + i * tickSpacing) / candles.get(0).open.length)), x - 10, chartHeight + 20); // Label
        }
    }



    // Method to draw candles
//    private void drawCandles(GraphicsContext gc) {
//        // Clear canvas
//        gc.clearRect(0, 0, chartWidth, chartHeight);
//
//        // Calculate the number of candles to be displayed based on zoom factor and scroll position
//        int startIdx = (int) (scrollPosition * (candles.get(0).open.length - 1));
//        int endIdx = (int) ((scrollPosition + zoomFactor) * (candles.get(0).open.length - 1));
//        int numCandles = endIdx - startIdx;
//
//        // Calculate candle width based on zoom factor
//        double scaledCandleWidth = candleWidth * zoomFactor;
//        double space = spaceBetweenCandles * zoomFactor;
//
//        // Draw candles
//        for (int i = startIdx; i < endIdx; i++) {
//            double x = (i - startIdx) * (scaledCandleWidth + space);
//
//            // Calculate candle dimensions
//            double yOpen = chartHeight - candles.get(0).open[i];
//            double yClose = chartHeight - candles.get(0).close[i];
//            double yHigh = chartHeight - candles.get(0).high[i];
//            double yLow = chartHeight - candles.get(0).low[i];
//
//            // Draw candlestick body
//            if (candles.get(0).close[i] > candles.get(0).open[i]) {
//                gc.setFill(Color.GREEN); // Bullish candlestick
//                gc.fillRect(x, yClose, scaledCandleWidth, yOpen - yClose);
//            } else {
//                gc.setFill(Color.RED); // Bearish candlestick
//                gc.fillRect(x, yOpen, scaledCandleWidth, yClose - yOpen);
//            }
//
//            // Draw candlestick wicks
//            gc.setStroke(Color.BLACK);
//            gc.strokeLine(x + scaledCandleWidth / 2, yHigh, x + scaledCandleWidth / 2, yLow);
//        }
//
//        // Draw scale
//        drawScale(gc, startIdx, endIdx);
//    }

    // Method to draw scale
    private void drawScale(GraphicsContext gc, int startIdx, int endIdx) {
        int numTicks = 10; // Number of ticks on the scale
        double tickSpacing = (endIdx - startIdx) / (double) numTicks;
        double scaledWidth = chartWidth * zoomFactor;

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeLine(0, chartHeight, scaledWidth, chartHeight); // X-axis

        // Draw ticks and labels
        for (int i = 0; i <= numTicks; i++) {
            double x = i * tickSpacing * (candleWidth + spaceBetweenCandles) / (endIdx - startIdx);
            gc.strokeLine(x, chartHeight - 5, x, chartHeight + 5); // Tick mark
            gc.fillText(Integer.toString((int) ((startIdx + i * tickSpacing))), x - 10, chartHeight + 20); // Label
        }
    }

    private class Candle {
        double[] open, high, low, close;
        int volume;

        Candle(double[] open, double[] high, double[] low, double[] close, int volume) {
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
            this.volume = volume;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
