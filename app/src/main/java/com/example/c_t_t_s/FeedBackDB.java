package com.example.c_t_t_s;

public class FeedBackDB {
    private String Feedback;
    private String Email;

    FeedBackDB(){}

    public FeedBackDB(String feedbackID, String feedback, String email) {
        Feedback = feedback;
        Email = email;
    }

    public String getFeedback() {
        return Feedback;
    }
    public void setFeedback(String feedback) {
        Feedback = feedback;
    }
    public String getEmail() {
        return Email;
    }
    public void setEmail(String email) {
        Email = email;
    }

}