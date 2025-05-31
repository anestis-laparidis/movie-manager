package com.mycompany.moviemanager;

import java.util.Comparator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.ComboBoxTableCell;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class MainController {

    //Δήλωση GUI
    @FXML private TextField titleField;
    @FXML private ComboBox<String> genreBox;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> statusBox;
    @FXML private ComboBox<String> sortBox;

    @FXML private TableView<Movie> toWatchTable;
    @FXML private TableColumn<Movie, String> titleColumn1, genreColumn1, dateColumn1, statusColumn1;

    @FXML private TableView<Movie> seenTable;
    @FXML private TableColumn<Movie, String> titleColumn2, genreColumn2, dateColumn2, statusColumn2;
    
    //Λίστες κατάστασης ταινιών
    private final ObservableList<Movie> toWatchList = FXCollections.observableArrayList();
    private final ObservableList<Movie> seenList = FXCollections.observableArrayList();

    //Αρχικοποίηση και φόρτωση δεδομένων απο το αρχείο .txt
    @FXML
    public void initialize() {
        genreBox.setItems(FXCollections.observableArrayList("Action", "Comedy", "Drama", "Horror", "Sci-Fi"));
        statusBox.setItems(FXCollections.observableArrayList("To Watch", "Seen"));
        sortBox.setItems(FXCollections.observableArrayList("Είδος (A-Z)", "Ημερομηνία ↑", "Ημερομηνία ↓"));

        // Setup tables
        setupTable(toWatchTable, titleColumn1, genreColumn1, dateColumn1, statusColumn1, toWatchList);
        setupTable(seenTable, titleColumn2, genreColumn2, dateColumn2, statusColumn2, seenList);
        
        loadFromFile();
    }
    
    //Μέθοδος για τη ρύθμιση στηλών καιεπεξεσία status για TableView
    private void setupTable(TableView<Movie> table,
                            TableColumn<Movie, String> titleCol,
                            TableColumn<Movie, String> genreCol,
                            TableColumn<Movie, String> dateCol,
                            TableColumn<Movie, String> statusCol,
                            ObservableList<Movie> list) {

        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        genreCol.setCellValueFactory(new PropertyValueFactory<>("genre"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        //Κάνει το status επεξεργάσιμο με επιλογή απο comboBox
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(ComboBoxTableCell.forTableColumn("To Watch", "Seen"));
        statusCol.setOnEditCommit(event -> {
            Movie movie = event.getRowValue();
            String newStatus = event.getNewValue();
            movie.setStatus(newStatus);

            // Μετακίνηση σε αντίστοιχη λίστα seen/want to see
            if (newStatus.equals("Seen") && toWatchList.contains(movie)) {
                toWatchList.remove(movie);
                seenList.add(movie);
            } else if (newStatus.equals("To Watch") && seenList.contains(movie)) {
                seenList.remove(movie);
                toWatchList.add(movie);
            }
        });

        table.setItems(list);
        table.setEditable(true);
    }

    //Μέθοδος προσθήκης νέας ταινίας
    @FXML
    private void handleAddMovie() {
        String title = titleField.getText();
        String genre = genreBox.getValue();
        String status = statusBox.getValue();
        String date = (datePicker.getValue() != null) ? datePicker.getValue().toString() : "";

        if (title.isEmpty() || genre == null || status == null) {
            showAlert("Σφάλμα", "Συμπλήρωσε όλα τα πεδία.");
            return;
        }

        Movie movie = new Movie(title, genre, date, status);

        if (status.equals("Seen")) {
            seenList.add(movie);
        } else {
            toWatchList.add(movie);
        }

        clearFields();//Καθαρίζει ολα τα πεδία μετά την εισαγωγή
    }

    //Μέθοδος που διαγράφει την επιλεγμένη ταινία απο τη λίστα "Προς Παρακολούθηση"
    @FXML
    private void handleDeleteFromToWatch() {
        Movie selected = toWatchTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            toWatchList.remove(selected);
        } else {
            showAlert("Καμία Επιλογή", "Επίλεξε μια ταινία για διαγραφή από τη λίστα 'Προς Παρακολούθηση'.");
        }
    }

    //Μέθοδος που διαγράφει την επιλεγμένη ταινία απο τη λίστα "Προβληθείσες"
    @FXML
    private void handleDeleteFromSeen() {
        Movie selected = seenTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            seenList.remove(selected);
        } else {
            showAlert("Καμία Επιλογή", "Επίλεξε μια ταινία για διαγραφή από τη λίστα 'Προβληθείσες'.");
        }
    }
    //Μέθοδος ταξινόμησης
    @FXML
    private void handleSort() {
        String selected = sortBox.getValue();
        if (selected == null) return;

        Comparator<Movie> comparator = null;

        switch (selected) {
            case "Είδος (A-Z)":
                comparator = Comparator.comparing(Movie::getGenre, String::compareToIgnoreCase);
                break;
            case "Ημερομηνία ↑":
                comparator = Comparator.comparing(Movie::getDate);
                break;
            case "Ημερομηνία ↓":
                comparator = Comparator.comparing(Movie::getDate).reversed();
                break;
        }

        if (comparator != null) {
            FXCollections.sort(toWatchList, comparator);
            FXCollections.sort(seenList, comparator);
        }
    }
    
    //Μέθοδος που αποθηκεύει τα δεδομένα σε αρχείο movies.txt στον φάκελο του project
    @FXML
    private void handleSaveToFile() {
        String fileName = "movies.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("Τίτλος | Είδος | Ημερομηνία | Κατάσταση");
            writer.newLine();

            for (Movie movie : toWatchList) {
                writer.write(formatMovie(movie));
                writer.newLine();
            }

            for (Movie movie : seenList) {
                writer.write(formatMovie(movie));
                writer.newLine();
            }

            showAlert("Επιτυχία", "Οι ταινίες αποθηκεύτηκαν στο αρχείο: " + fileName);
        } catch (IOException e) {
            showAlert("Σφάλμα", "Αποτυχία αποθήκευσης: " + e.getMessage());
        }
    }

    //Μορφοποίηση μιας ταινίας ως γραμμή κειμένου για το αρχείο .txt
    private String formatMovie(Movie movie) {
        return movie.getTitle() + " | " + movie.getGenre() + " | " + movie.getDate() + " | " + movie.getStatus();
    }

    //Φορτώμνει τις ταινίες απο το αρχείο movies.txt κατα την εκκίνηση
    private void loadFromFile() {
        File file = new File("movies.txt");
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // παράλειψη header
                }

                String[] parts = line.split(" \\| ");
                if (parts.length != 4) continue;

                String title = parts[0].trim();
                String genre = parts[1].trim();
                String date = parts[2].trim();
                String status = parts[3].trim();

                Movie movie = new Movie(title, genre, date, status);

                if (status.equals("Seen")) {
                    seenList.add(movie);
                } else {
                    toWatchList.add(movie);
                }
            }
        } catch (Exception e) {
            showAlert("Σφάλμα", "Αποτυχία φόρτωσης αρχείου: " + e.getMessage());
        }
    }

    //Καθαρίζει όλα τα πεδία εισόδου
    private void clearFields() {
        titleField.clear();
        genreBox.getSelectionModel().clearSelection();
        datePicker.setValue(null);
        statusBox.getSelectionModel().clearSelection();
    }

    //Εμφανίζει προειδοποιήτικό ή πληροφοριακό παράθυρο
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
