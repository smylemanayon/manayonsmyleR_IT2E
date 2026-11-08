package com.scc.ui;

import com.scc.dao.*;
import com.scc.model.User;
import java.util.List;
import java.util.Scanner;

public class VoterMenu {
    private final Scanner scanner;
    private final User user;
    private final CandidateDao candidateDao = new CandidateDao();
    private final VoteDao voteDao = new VoteDao();
    private final ActivityLogDao logs = new ActivityLogDao();

    public VoterMenu(Scanner scanner, User user) {
        this.scanner = scanner;
        this.user = user;
    }

    public void start() {
        while (true) {
            System.out.println("\n========== VOTER MENU ==========");
            System.out.println("Welcome, " + user.username());
            System.out.println("1. View candidates");
            System.out.println("2. Cast vote");
            System.out.println("3. View my voting status");
            System.out.println("4. Logout");
            System.out.print("Choose an option: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> showCandidates();
                case "2" -> castVote();
                case "3" -> showStatus();
                case "4" -> { return; }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void showCandidates() {
        List<CandidateDao.Candidate> candidates = candidateDao.getAllCandidates();
        System.out.println("\nID | Candidate | Position | Party");
        for (CandidateDao.Candidate c : candidates) {
            System.out.printf("%d | %s | %s | %s%n", c.id(), c.name(), c.position(), c.party());
        }
        if (candidates.isEmpty()) System.out.println("No candidates available.");
    }

    private void castVote() {
        if (voteDao.hasVoted(user.id())) {
            System.out.println("Your vote has already been recorded. You cannot vote again.");
            return;
        }
        showCandidates();
        System.out.print("Enter candidate ID to vote for: ");
        try {
            int candidateId = Integer.parseInt(scanner.nextLine().trim());
            boolean exists = candidateDao.getAllCandidates().stream()
                    .anyMatch(c -> c.id() == candidateId);
            if (!exists) {
                System.out.println("Invalid candidate ID.");
                return;
            }
            if (voteDao.castVote(user.id(), candidateId)) {
                logs.logAction(user.id(), "VOTE_CAST",
                        "Voter ID " + user.id() + " cast a vote for candidate ID " + candidateId + ".");
                System.out.println("Your vote has been recorded successfully.");
            } else {
                System.out.println("Vote could not be recorded. You may already have voted.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        }
    }

    private void showStatus() {
        System.out.println(voteDao.hasVoted(user.id())
                ? "Status: Your vote is recorded."
                : "Status: You have not voted yet.");
    }
}
