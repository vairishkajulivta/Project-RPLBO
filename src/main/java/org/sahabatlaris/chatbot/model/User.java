package org.sahabatlaris.chatbot.model;

public class User {
    private String username;
    private String password;
    private boolean isAdmin;

    public User() {}
    public User(String username, String password, boolean isAdmin) {
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
    }

    public String getUsername() { return username; }
    public void setUsername(String v) { this.username = v; }
    public String getPassword() { return password; }
    public void setPassword(String v) { this.password = v; }
    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean v) { this.isAdmin = v; }
}
