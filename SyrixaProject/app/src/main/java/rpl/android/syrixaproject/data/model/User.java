package rpl.android.syrixaproject.data.model;

public class User {
    private String uid;
    private String username;
    private String email;
    private String profilePicture;

    public User() {
    }

    public User(String uid, String username, String email, String profilePicture) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.profilePicture = profilePicture;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
}
