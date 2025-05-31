module com.mycompany.moviemanager {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mycompany.moviemanager to javafx.fxml;
    exports com.mycompany.moviemanager;
}
