package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.ui.MainMenu;

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseInitializer.initialize();
            new MainMenu().start();
        } catch (Exception e) {
            System.out.println("Unable to start the system: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
