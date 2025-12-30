package com.aplikasi.bioskopku.model;

public class User {
    public String username;
    public String email;
    public String role;
    public int balance;

    // Field Baru
    public String fullName;
    public String address;
    public String age;

    public User() {
        // Constructor kosong wajib untuk Firebase
    }

    public User(String username, String email, String role, int balance) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.balance = balance;
    }
}