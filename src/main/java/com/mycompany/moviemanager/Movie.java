package com.mycompany.moviemanager;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

//Αρχικοποίηση κλάσης που αναπαριστά μια ταινία στην εφαρμογή.
public class Movie {

    private final StringProperty title;
    private final StringProperty genre;
    private final StringProperty date;
    private final StringProperty status;

    //Κατασκευαστής
    public Movie(String title, String genre, String date, String status) {
        this.title = new SimpleStringProperty(title);
        this.genre = new SimpleStringProperty(genre);
        this.date = new SimpleStringProperty(date);
        this.status = new SimpleStringProperty(status);
    }

    //Setters και Geters
    public void setTitle(String value) { 
        title.set(value); 
    }
    public void setGenre(String value) {
        genre.set(value); 
    }
    public void setDate(String value) { 
        date.set(value); 
    }
    public void setStatus(String value) { 
        status.set(value); 
    }
    
    //Properties για binding για tableView
    public String getTitle() {
        return title.get(); 
    }
    public String getGenre() {
        return genre.get(); 
    }
    public String getDate() {
        return date.get(); 
    }
    public String getStatus() { 
        return status.get(); 
    }

    public StringProperty titleProperty() { 
        return title; 
    }
    public StringProperty genreProperty() { 
        return genre; }
    public StringProperty dateProperty() { 
        return date; 
    }
    public StringProperty statusProperty() { 
        return status; 
    }
}
