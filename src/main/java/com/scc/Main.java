package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.ui.Authentication;

public class Main {
    public static void main(String[] args) {
        // Initialize database tables automatically
        DatabaseInitializer.initializeDatabase();

        // Delegate execution to Authentication UI handler
        Authentication.start();
    }
}