package gui;

public class Launcher {
    public static void main(String[] args) {
        new magaza.dao.ReviewDAO().createTableIfNotExists();
        MainApp.main(args);
    }
}