package com.example.c_t_t_s;
public class AccountDB {

    private String fName;
    private String lName;
    private String email;
    private String phone;
    private String Birthdate;
    private String username;
    private String gender;
    private String account;
    private String image;

    public AccountDB(){}

    public AccountDB(String fName, String lName, String email, String phone, String Birthdate, String username, String gender, String account, String image) {
        this.fName = fName;
        this.lName = lName;
        this.email = email;
        this.phone = phone;
        this.Birthdate = Birthdate;
        this.username = username;
        this.gender = gender;
        this.account = account;
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getBirthdate() {
        return Birthdate;
    }

    public void setBirthdate(String birthdate) {
        Birthdate = birthdate;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getlName() {
        return lName;
    }

    public void setlName(String lName) {
        this.lName = lName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }
}
