package com.scc.model;

public record User(int id, String username, String passwordHash, String role, String status) {}
