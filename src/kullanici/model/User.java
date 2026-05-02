package kullanici.model;

public class User {
    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private String avatarPath;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }

    @Override
    public String toString() {
        return " 👤  " + username;
    }
}