package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.User;
import java.util.Optional;
import java.util.Scanner;

public class MainMenu {
    private final Scanner scanner = new Scanner(System.in);
    private final UserDao userDao = new UserDao();
    private final ActivityLogDao logs = new ActivityLogDao();

    public void start() {
        while (true) {
            System.out.println("\n==============================================");
            System.out.println(" ELECTRONIC VOTING AND AUTOMATED VOTE COUNTING");
            System.out.println("==============================================");
            System.out.println("1. Log in");
            System.out.println("2. Register as voter");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> login();
                case "2" -> register();
                case "3" -> {
                    System.out.println("Thank you for using the system!");
                    return;
                }
                default -> System.out.println("Invalid option. Please choose 1-3.");
            }
        }
    }

    private void register() {
        System.out.println("\n--- VOTER REGISTRATION ---");
        System.out.print("Create username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Create password: ");
        String password = scanner.nextLine();

        if (username.isBlank() || password.isBlank()) {
            System.out.println("Username and password are required.");
            return;
        }
        if (password.length() < 4) {
            System.out.println("Password must contain at least 4 characters.");
            return;
        }
        if (userDao.register(username, password)) {
            System.out.println("Registration successful. Wait for admin approval before logging in.");
        } else {
            System.out.println("Registration failed. Username may already exist.");
        }
    }

    private void login() {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        Optional<User> result = userDao.login(username, password);
        if (result.isEmpty()) {
            System.out.println("Incorrect username or password.");
            return;
        }

        User user = result.get();
        if (!"ACTIVE".equalsIgnoreCase(user.status())) {
            System.out.println("Your account is PENDING. Please wait for administrator approval.");
            return;
        }

        logs.logAction(user.id(), "LOGIN", "User " + user.username() + " logged in.");
        if ("ADMIN".equalsIgnoreCase(user.role())) {
            new AdminMenu(scanner, user).start();
        } else if ("VOTER".equalsIgnoreCase(user.role())) {
            new VoterMenu(scanner, user).start();
        } else {
            System.out.println("This account has no supported menu role.");
        }
        logs.logAction(user.id(), "LOGOUT", "User " + user.username() + " logged out.");
    }
}
