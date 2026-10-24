package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private static final UserDao userDao = new UserDao();

    public static void showMenu(Scanner sc) {
        while (true) {
            System.out.println("\n===============================");
            System.out.println(" ADMIN MANAGEMENT MENU");
            System.out.println("===============================");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Logout");
            System.out.print("Choose an option: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    printUserTable(userDao.getAllUsers(), "ALL SYSTEM USERS");
                    break;
                case "2":
                    printUserTable(userDao.getPendingUsers(), "PENDING REGISTRATIONS");
                    break;
                case "3":
                    handleApproval(sc);
                    break;
                case "4":
                    System.out.println("Logging out from Admin menu...");
                    return;
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }
    }

    private static void printUserTable(List<User> users, String title) {
        System.out.println("\n--- " + title + " ---");
        if (users.isEmpty()) {
            System.out.println("No records found.");
            return;
        }

        String format = "%-5s | %-15s | %-12s | %-10s%n";
        System.out.printf(format, "ID", "Username", "Role", "Status");
        System.out.println("--------------------------------------------------");
        for (User u : users) {
            System.out.printf(format, u.id(), u.username(), u.roles(), u.status());
        }
        System.out.println("--------------------------------------------------");
    }

    private static void handleApproval(Scanner sc) {
        List<User> pendingUsers = userDao.getPendingUsers();
        if (pendingUsers.isEmpty()) {
            System.out.println("No pending accounts to approve.");
            return;
        }

        printUserTable(pendingUsers, "PENDING ACCOUNTS");
        System.out.print("Enter User ID to approve (Enter 0 to cancel): ");

        if (!sc.hasNextInt()) {
            System.out.println("Invalid input. Action cancelled.");
            sc.nextLine();
            return;
        }

        int targetId = sc.nextInt();
        sc.nextLine(); // Consume newline

        if (targetId == 0) {
            System.out.println("Approval action cancelled.");
            return;
        }

        boolean success = userDao.approveUser(targetId);
        if (success) {
            System.out.println("Success! User ID " + targetId + " status updated to 'APPROVED'.");
        } else {
            System.out.println("Failed to approve user. Please verify the User ID.");
        }
    }
}1