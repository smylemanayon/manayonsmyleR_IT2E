package com.scc.ui;

import com.scc.dao.*;
import com.scc.model.ActivityLog;
import com.scc.model.User;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final Scanner scanner;
    private final User admin;
    private final UserDao userDao = new UserDao();
    private final CandidateDao candidateDao = new CandidateDao();
    private final VoteDao voteDao = new VoteDao();
    private final ActivityLogDao logs = new ActivityLogDao();

    public AdminMenu(Scanner scanner, User admin) {
        this.scanner = scanner;
        this.admin = admin;
    }

    public void start() {
        while (true) {
            System.out.println("\n========== ADMIN MENU ==========");
            System.out.println("1. View registered users");
            System.out.println("2. Approve voter account");
            System.out.println("3. Update user role");
            System.out.println("4. Add candidate");
            System.out.println("5. View candidates");
            System.out.println("6. View election results");
            System.out.println("7. View System Audit Logs");
            System.out.println("8. Filter Audit Logs by User ID");
            System.out.println("9. Clear Historical Audit Logs");
            System.out.println("10. Logout");
            System.out.print("Choose an option: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> viewUsers();
                case "2" -> approveUser();
                case "3" -> updateRole();
                case "4" -> addCandidate();
                case "5" -> viewCandidates();
                case "6" -> viewResults();
                case "7" -> displayLogs(logs.getAllLogs());
                case "8" -> filterLogs();
                case "9" -> clearLogs();
                case "10" -> { return; }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void viewUsers() {
        System.out.println("\nID | Username | Role | Status");
        for (User u : userDao.getAllUsers()) {
            System.out.printf("%d | %s | %s | %s%n", u.id(), u.username(), u.role(), u.status());
        }
    }

    private void approveUser() {
        viewUsers();
        System.out.print("Enter voter User ID to approve: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            if (userDao.approveUser(id)) {
                logs.logAction(admin.id(), "USER_APPROVED",
                        "Admin approved account for User ID " + id + ".");
                System.out.println("Account approved.");
            } else System.out.println("Approval failed. Check the User ID and account role.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid User ID.");
        }
    }

    private void updateRole() {
        viewUsers();
        System.out.print("Enter User ID: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("New role (VOTER/ADMIN): ");
            String role = scanner.nextLine().trim().toUpperCase();
            if (!role.equals("VOTER") && !role.equals("ADMIN")) {
                System.out.println("Role must be VOTER or ADMIN.");
                return;
            }
            if (userDao.updateRole(id, role)) {
                logs.logAction(admin.id(), "USER_UPDATED",
                        "Admin changed role of User ID " + id + " to " + role + ".");
                System.out.println("Role updated.");
            } else System.out.println("Role update failed. Check the User ID.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid User ID.");
        }
    }

    private void addCandidate() {
        System.out.print("Candidate name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Position: ");
        String position = scanner.nextLine().trim();
        System.out.print("Party: ");
        String party = scanner.nextLine().trim();
        if (name.isBlank() || position.isBlank()) {
            System.out.println("Candidate name and position are required.");
            return;
        }
        if (party.isBlank()) party = "Independent";
        if (candidateDao.addCandidate(name, position, party)) {
            logs.logAction(admin.id(), "CANDIDATE_CREATED",
                    "Admin added candidate " + name + " for " + position + ".");
            System.out.println("Candidate added.");
        }
    }

    private void viewCandidates() {
        System.out.println("\nID | Candidate | Position | Party");
        for (CandidateDao.Candidate c : candidateDao.getAllCandidates()) {
            System.out.printf("%d | %s | %s | %s%n", c.id(), c.name(), c.position(), c.party());
        }
    }

    private void viewResults() {
        System.out.println("\n========== ELECTION RESULTS ==========");
        System.out.println("ID | Candidate | Position | Party | Votes");
        for (String[] row : voteDao.getResults()) {
            System.out.printf("%s | %s | %s | %s | %s%n",
                    row[0], row[1], row[2], row[3], row[4]);
        }
        logs.logAction(admin.id(), "RESULTS_VIEWED", "Admin viewed election results.");
    }

    private void filterLogs() {
        System.out.print("Enter User ID to filter: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            displayLogs(logs.getLogsByUserId(id));
        } catch (NumberFormatException e) {
            System.out.println("Invalid User ID.");
        }
    }

    private void displayLogs(List<ActivityLog> entries) {
        System.out.println("\n================ SYSTEM AUDIT LOGS ================");
        System.out.printf("%-5s | %-7s | %-18s | %-35s | %s%n",
                "ID", "UserID", "Action", "Description", "Timestamp");
        System.out.println("-----------------------------------------------------------------------------------------------");
        if (entries.isEmpty()) {
            System.out.println("No activity logs found.");
            return;
        }
        for (ActivityLog log : entries) {
            System.out.printf("%-5d | %-7d | %-18s | %-35s | %s%n",
                    log.id(), log.userId(), log.actionType(),
                    shorten(log.description(), 35), log.timestamp());
        }
    }

    private String shorten(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 3) + "...";
    }

    private void clearLogs() {
        System.out.print("Are you sure you want to clear ALL historical logs? (Y/N): ");
        String answer = scanner.nextLine().trim();
        if (answer.equalsIgnoreCase("Y")) {
            if (logs.clearAllLogs()) {
                System.out.println("Historical audit logs cleared.");
                // This event is recorded after the clear so the action itself remains auditable.
                logs.logAction(admin.id(), "AUDIT_LOGS_CLEARED",
                        "Admin cleared historical audit logs.");
            }
        } else {
            System.out.println("Clear operation cancelled.");
        }
    }
}
