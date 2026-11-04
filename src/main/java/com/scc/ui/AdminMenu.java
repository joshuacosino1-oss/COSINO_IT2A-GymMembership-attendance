package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private static final UserDao userDao = new UserDao();

    public static void showMenu(Scanner sc, User currentAdmin) {
        while (true) {
            System.out.println("\n=================================================");
            System.out.println("                 ADMIN DASHBOARD");
            System.out.println("=================================================");
            System.out.println("1. View All Users");
            System.out.println("2. View Pending User Approvals");
            System.out.println("3. Approve User Registration");
            System.out.println("4. Add New User");
            System.out.println("5. Update User Role / Status");
            System.out.println("6. Delete User Account");
            System.out.println("7. Logout");
            System.out.print("Choose an option (1-7): ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    printUserTable(userDao.getAllUsers(), "ALL SYSTEM USERS");
                    break;
                case "2":
                    printUserTable(userDao.getPendingUsers(), "PENDING USER APPROVALS");
                    break;
                case "3":
                    handleApproval(sc);
                    break;
                case "4":
                    handleAddUser(sc);
                    break;
                case "5":
                    handleUpdateUser(sc, currentAdmin);
                    break;
                case "6":
                    handleDeleteUser(sc, currentAdmin);
                    break;
                case "7":
                    System.out.println("Logging out from Admin dashboard...");
                    return;
                default:
                    System.out.println("Invalid choice! Please select between 1 and 7.");
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

        printUserTable(pendingUsers, "PENDING APPROVALS");
        int targetId = readIntInput(sc, "Enter User ID to approve (Enter 0 to cancel): ");

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

    private static void handleAddUser(Scanner sc) {
        System.out.print("Enter username: ");
        String username = sc.nextLine().trim();
        System.out.print("Enter password: ");
        String password = sc.nextLine().trim();

        String role = readValidRole(sc);
        String status = readValidStatus(sc);

        boolean success = userDao.registerUser(username, password, role, status);
        if (success) {
            System.out.println("Success! New user '" + username + "' added with role " + role + " and status " + status + ".");
        } else {
            System.out.println("Failed to add user. Username might already exist.");
        }
    }

    private static void handleUpdateUser(Scanner sc, User currentAdmin) {
        List<User> allUsers = userDao.getAllUsers();
        printUserTable(allUsers, "ALL USERS");

        int targetId = readIntInput(sc, "Enter User ID to update (Enter 0 to cancel): ");
        if (targetId == 0) {
            System.out.println("Update action cancelled.");
            return;
        }

        // Self-Edit Restriction Guardrail
        if (targetId == currentAdmin.id()) {
            System.out.println("WARNING: Modifying the currently active admin account is restricted.");
            return;
        }

        String newRole = readValidRole(sc);
        String newStatus = readValidStatus(sc);

        boolean success = userDao.updateUserRoleAndStatus(targetId, newRole, newStatus);
        if (success) {
            System.out.println("Success! User ID " + targetId + " updated to Role: " + newRole + ", Status: " + newStatus);
        } else {
            System.out.println("Failed to update user. Please verify the User ID.");
        }
    }

    private static void handleDeleteUser(Scanner sc, User currentAdmin) {
        List<User> allUsers = userDao.getAllUsers();
        printUserTable(allUsers, "ALL USERS");

        int targetId = readIntInput(sc, "Enter User ID to delete (Enter 0 to cancel): ");
        if (targetId == 0) {
            System.out.println("Delete action cancelled.");
            return;
        }

        // Self-Edit Restriction Guardrail
        if (targetId == currentAdmin.id()) {
            System.out.println("WARNING: Deleting the currently active admin account is restricted.");
            return;
        }

        boolean success = userDao.deleteUser(targetId);
        if (success) {
            System.out.println("Success! User ID " + targetId + " has been deleted.");
        } else {
            System.out.println("Failed to delete user. Please verify the User ID.");
        }
    }

    // Input Validation Wrappers
    private static int readIntInput(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine();
            try {
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid numeric ID (or 0 to cancel).");
            }
        }
    }

    private static String readValidRole(Scanner sc) {
        while (true) {
            System.out.print("Enter role (ADMIN, STAFF, MEMBER, TEACHER, STUDENT): ");
            String role = sc.nextLine().trim().toUpperCase();
            if (role.equals("ADMIN") || role.equals("STAFF") || role.equals("MEMBER") || role.equals("TEACHER") || role.equals("STUDENT")) {
                return role;
            }
            System.out.println("Invalid role! Please choose a recognized system role.");
        }
    }

    private static String readValidStatus(Scanner sc) {
        while (true) {
            System.out.print("Enter status (APPROVED, PENDING): ");
            String status = sc.nextLine().trim().toUpperCase();
            if (status.equals("APPROVED") || status.equals("PENDING")) {
                return status;
            }
            System.out.println("Invalid status! Please enter either APPROVED or PENDING.");
        }
    }
}