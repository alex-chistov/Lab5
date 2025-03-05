package org.example;

public class Book {
    private int id;
    private String title;
    private String author;
    private String publisher;
    private int year;

    public Book(int id, String title, String author, String publisher, int year) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.year = year;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getPublisher() { return publisher; }
    public int getYear() { return year; }

    @Override
    public String toString() {
        return id + ": " + title + " by " + author + " (" + publisher + ", " + year + ")";
    }
}
