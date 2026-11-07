package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class Authentication {
    private static final Scanner sc = new Scanner(System.in);
    private static final UserDao userDao = new UserDao();
    private static final ActivityLogDao logDao = new ActivityLogDao();

    public static void start() {
        while (true) {
            System.out.println("\n===============================");
            System.out.println(" GYM MANAGEMENT & ATTENDANCE");
            System.out.println("===============================");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    handleLogin();
                    break;
                case "2":
                    handleRegistration();
                    break;
                case "3":
                    System.out.println("Exiting application...");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    private static void handleRegistration() {
        System.out.print("Enter new username: ");
        String user = sc.nextLine();
        System.out.print("Enter password: ");
        String pass = sc.nextLine();

        System.out.println("Select Role: [1] Admin | [2] Staff | [3] Member | [4] Teacher | [5] Student");
        int roleChoice = sc.nextInt();
        sc.nextLine(); // Consume newline

        String role = null;
        switch (roleChoice) {
            case 1:
                role = "ADMIN";
                break;
            case 2:
                role = "STAFF";
                break;
            case 3:
                role = "MEMBER";
                break;
            case 4:
                role = "TEACHER";
                break;
            case 5:
                role = "STUDENT";
                break;
            default:
                System.out.println("Invalid role selected!");
        }

        if (role != null) {
            boolean isSuccess = userDao.registerUser(user, pass, role, "PENDING");
            if (isSuccess) {
                System.out.println("Registration successful! Status is PENDING approval.");
            } else {
                System.out.println("Registration failed. Username might already exist.");
            }
        }
    }

    private static void handleLogin() {
        System.out.print("Enter username: ");
        String loginUser = sc.nextLine();
        System.out.print("Enter password: ");
        String loginPass = sc.nextLine();

        User loggedInUser = userDao.loginUser(loginUser, loginPass);
        if (loggedInUser != null) {
            if (loggedInUser.status().equalsIgnoreCase("PENDING")) {
                System.out.println("Login Denied: Your account is still PENDING approval.");
                return;
            }

            logDao.logAction(loggedInUser.id(), "LOGIN", "User " + loggedInUser.username() + " (ID: " + loggedInUser.id() + ") logged in.");

            System.out.println("\nLogin Successful! Welcome, " + loggedInUser.username() + " (" + loggedInUser.roles() + ")");

            if (loggedInUser.roles().equalsIgnoreCase("ADMIN")) {
                AdminMenu.showMenu(sc, loggedInUser);
            } else {
                System.out.println("Dashboard for " + loggedInUser.roles() + " is under construction.");
            }
        } else {
            System.out.println("Invalid username or password!");
        }
    }
}