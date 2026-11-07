package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.MembershipDao;
import com.scc.dao.UserDao;
import com.scc.model.ActivityLog;
import com.scc.model.Membership;
import com.scc.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private static final UserDao userDao = new UserDao();
    private static final ActivityLogDao logDao = new ActivityLogDao();
    private static final MembershipDao membershipDao = new MembershipDao();

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
            System.out.println("7. Record Membership Payment / Fee");
            System.out.println("8. View Payment History");
            System.out.println("9. View System Audit Logs");
            System.out.println("10. Logout");
            System.out.print("Choose an option (1-10): ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    printUserTable(userDao.getAllUsers(), "ALL SYSTEM USERS");
                    break;
                case "2":
                    printUserTable(userDao.getPendingUsers(), "PENDING USER APPROVALS");
                    break;
                case "3":
                    handleApproval(sc, currentAdmin);
                    break;
                case "4":
                    handleAddUser(sc, currentAdmin);
                    break;
                case "5":
                    handleUpdateUser(sc, currentAdmin);
                    break;
                case "6":
                    handleDeleteUser(sc, currentAdmin);
                    break;
                case "7":
                    handleRecordPayment(sc, currentAdmin);
                    break;
                case "8":
                    printPaymentTable(membershipDao.getAllPayments(), "ALL RECORDED PAYMENTS");
                    break;
                case "9":
                    showAuditLogMenu(sc);
                    break;
                case "10":
                    logDao.logAction(currentAdmin.id(), "LOGOUT", "User " + currentAdmin.username() + " (ID: " + currentAdmin.id() + ") logged out.");
                    System.out.println("Logging out from Admin dashboard...");
                    return;
                default:
                    System.out.println("Invalid choice! Please select between 1 and 10.");
            }
        }
    }

    private static void handleRecordPayment(Scanner sc, User currentAdmin) {
        printUserTable(userDao.getAllUsers(), "SELECT USER FOR PAYMENT");
        int targetUserId = readIntInput(sc, "Enter User ID paying fee (0 to cancel): ");
        if (targetUserId == 0) return;

        System.out.println("Select Plan Type: [1] Daily Pass | [2] Monthly | [3] Quarterly | [4] Yearly");
        String planChoice = sc.nextLine().trim();
        String planType = "MONTHLY";
        int durationDays = 30;
        double defaultFee = 500.00;

        switch (planChoice) {
            case "1":
                planType = "DAILY";
                durationDays = 1;
                defaultFee = 50.00;
                break;
            case "2":
                planType = "MONTHLY";
                durationDays = 30;
                defaultFee = 500.00;
                break;
            case "3":
                planType = "QUARTERLY";
                durationDays = 90;
                defaultFee = 1350.00;
                break;
            case "4":
                planType = "YEARLY";
                durationDays = 365;
                defaultFee = 5000.00;
                break;
            default:
                System.out.println("Invalid choice! Defaulting to Monthly plan.");
        }

        System.out.print("Enter Amount Paid in PHP (Default: " + defaultFee + "): ");
        String feeInput = sc.nextLine().trim();
        double amountPaid = feeInput.isEmpty() ? defaultFee : Double.parseDouble(feeInput);

        LocalDate startDate = LocalDate.now();
        LocalDate expiryDate = startDate.plusDays(durationDays);

        boolean success = membershipDao.recordPayment(
                targetUserId,
                planType,
                startDate.toString(),
                expiryDate.toString(),
                amountPaid,
                currentAdmin.id()
        );

        if (success) {
            System.out.printf("Success! Payment of PHP %.2f recorded for User ID %d. Plan expires on %s.%n", amountPaid, targetUserId, expiryDate);
        } else {
            System.out.println("Failed to record payment.");
        }
    }

    private static void printPaymentTable(List<Membership> payments, String title) {
        System.out.println("\n--- " + title + " ---");
        if (payments.isEmpty()) {
            System.out.println("No payment records found.");
            return;
        }

        String format = "%-5s | %-8s | %-12s | %-12s | %-12s | %-12s%n";
        System.out.printf(format, "ID", "User ID", "Plan", "Start Date", "Expiry Date", "Amount (PHP)");
        System.out.println("-------------------------------------------------------------------------");
        for (Membership m : payments) {
            System.out.printf(format, m.membershipId(), m.userId(), m.planType(), m.startDate(), m.expiryDate(), String.format("%.2f", m.amountPaid()));
        }
        System.out.println("-------------------------------------------------------------------------");
        System.out.println("Total Records: " + payments.size());
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

    private static void handleApproval(Scanner sc, User currentAdmin) {
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
            logDao.logAction(currentAdmin.id(), "USER_APPROVED", "Admin " + currentAdmin.id() + " approved account for User ID " + targetId);
            System.out.println("Success! User ID " + targetId + " status updated to 'APPROVED'.");
        } else {
            System.out.println("Failed to approve user. Please verify the User ID.");
        }
    }

    private static void handleAddUser(Scanner sc, User currentAdmin) {
        System.out.print("Enter username: ");
        String username = sc.nextLine().trim();
        System.out.print("Enter password: ");
        String password = sc.nextLine().trim();

        String role = readValidRole(sc);
        String status = readValidStatus(sc);

        boolean success = userDao.registerUser(username, password, role, status);
        if (success) {
            logDao.logAction(currentAdmin.id(), "USER_CREATED", "Admin " + currentAdmin.id() + " created user " + username + " with role " + role);
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

        if (targetId == currentAdmin.id()) {
            System.out.println("WARNING: Modifying the currently active admin account is restricted.");
            return;
        }

        String newRole = readValidRole(sc);
        String newStatus = readValidStatus(sc);

        boolean success = userDao.updateUserRoleAndStatus(targetId, newRole, newStatus);
        if (success) {
            logDao.logAction(currentAdmin.id(), "USER_UPDATED", "Admin " + currentAdmin.id() + " changed User " + targetId + " to role " + newRole + ", status " + newStatus);
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

        if (targetId == currentAdmin.id()) {
            System.out.println("WARNING: Deleting the currently active admin account is restricted.");
            return;
        }

        boolean success = userDao.deleteUser(targetId);
        if (success) {
            logDao.logAction(currentAdmin.id(), "USER_DELETED", "Admin " + currentAdmin.id() + " deleted User ID " + targetId);
            System.out.println("Success! User ID " + targetId + " has been deleted.");
        } else {
            System.out.println("Failed to delete user. Please verify the User ID.");
        }
    }

    private static void showAuditLogMenu(Scanner sc) {
        while (true) {
            System.out.println("\n========= SYSTEM AUDIT LOGS =========");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Clear All Logs");
            System.out.println("0. Back");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    printLogTable(logDao.getAllLogs(), "ALL SYSTEM AUDIT LOGS");
                    break;
                case "2":
                    int targetUserId = readIntInput(sc, "Enter User ID (0 to cancel): ");
                    if (targetUserId != 0) {
                        printLogTable(logDao.getLogsByUserId(targetUserId), "LOGS FOR USER ID: " + targetUserId);
                    }
                    break;
                case "3":
                    System.out.print("Are you sure you want to clear all logs? (y/n): ");
                    if (sc.nextLine().trim().equalsIgnoreCase("y")) {
                        logDao.clearAllLogs();
                        System.out.println("All audit logs cleared.");
                    }
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    private static void printLogTable(List<ActivityLog> logs, String title) {
        System.out.println("\n--- " + title + " ---");
        if (logs.isEmpty()) {
            System.out.println("No log records found.");
            return;
        }

        String format = "%-5s | %-8s | %-18s | %-45s | %-20s%n";
        System.out.printf(format, "ID", "User ID", "Action Type", "Description", "Timestamp");
        System.out.println("-------------------------------------------------------------------------------------------------------------");
        for (ActivityLog l : logs) {
            System.out.printf(format, l.activityId(), l.userId(), l.action(), l.details(), l.timestamp());
        }
        System.out.println("-------------------------------------------------------------------------------------------------------------");
        System.out.println("Total: " + logs.size());
    }

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