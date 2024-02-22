module com.example.candlestick {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.candlestick to javafx.fxml;
    exports com.example.candlestick;
}