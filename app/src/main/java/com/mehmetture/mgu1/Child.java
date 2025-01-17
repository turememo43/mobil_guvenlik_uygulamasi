package com.mehmetture.mgu1;

public class Child {
    private String email;
    private String role;
    private String uid;
    private String token;

    // Yapıcı metod
    public Child(String email, String role, String uid, String token) {
        this.email = email;
        this.role = role;
        this.uid = uid;
        this.token = token;
    }

    // Get ve Set metodları
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
