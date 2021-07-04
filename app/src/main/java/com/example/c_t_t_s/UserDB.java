package com.example.c_t_t_s;

public class UserDB {
    String UserId;
    String UserFirstName;
    String UserlastName;
    String UserEmail;
    String UserPhone;
    String UserUserName;
    String Userpassword;
    String UserRePassword;
    String UserAge;
    String UserSex;

    public UserDB(){}

    public UserDB(String userId, String userFirstName, String userlastName, String userEmail, String userPhone, String userUserName, String userpassword, String userRePassword, String userAge, String userSex) {
        UserId = userId;
        UserFirstName = userFirstName;
        UserlastName = userlastName;
        UserEmail = userEmail;
        UserPhone = userPhone;
        UserUserName = userUserName;
        Userpassword = userpassword;
        UserRePassword = userRePassword;
        UserAge = userAge;
        UserSex = userSex;
    }

    public String getUserId() {
        return UserId;
    }

    public String getUserFirstName() {
        return UserFirstName;
    }

    public String getUserlastName() {
        return UserlastName;
    }

    public String getUserEmail() {
        return UserEmail;
    }

    public String getUserPhone() {
        return UserPhone;
    }

    public String getUserUserName() {
        return UserUserName;
    }

    public String getUserpassword() {
        return Userpassword;
    }

    public String getUserRePassword() {
        return UserRePassword;
    }

    public String getUserAge() {
        return UserAge;
    }

    public String getUserSex() {
        return UserSex;
    }

}


