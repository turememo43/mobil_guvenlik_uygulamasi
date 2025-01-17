package com.mehmetture.mgu1;

public class Child {
    private String email;
    private String role;
    private String uid; // UID alanı

    // Boş constructor (Firebase için gerekli)
    public Child() {}

    // Parametreli constructor
    public Child(String email, String role, String uid) {
        this.email = email;
        this.role = role;
        this.uid = uid;
    }

    // Getter ve Setter metodları
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
}
