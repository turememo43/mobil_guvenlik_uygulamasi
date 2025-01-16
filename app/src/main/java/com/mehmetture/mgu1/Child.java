package com.mehmetture.mgu1;

public class Child {
    private String email;
    private String role;
    private String token;

    public Child(String email, String role, String token) {
        this.email = email;
        this.role = role;
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }
}
