package model;
import java.io.Serializable;
 
public class User implements Serializable{
    private static final long serialVersionUID = 20210811010L;
    private int id;
    private String username;
    private String password;
    private String fullname;
    private String position;
    private String note;
     
    public User() {
        super();
    }

    public User(String username, String password, String fullname, String note, String position) {
        super();
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.position = position;
        this.note = note;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFullname() {
        return fullname;
    }

    public String getPosition() {
        return position;
    }

    public String getNote() {
        return note;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setPosition(String position) {
        this.position = position;
    }
 
    
}