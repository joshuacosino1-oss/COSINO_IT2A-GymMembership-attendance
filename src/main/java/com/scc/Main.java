package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.ui.Authentication;

public class Main {
    public static void main(String[] args) {
        // 1. Initialize Database & Tables automatically
        DatabaseInitializer.initializeDatabase();

        // 2. Delegate execution to Authentication UI handler
        Authentication.start();
    }
}