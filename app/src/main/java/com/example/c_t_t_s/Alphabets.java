package com.example.c_t_t_s;

public class Alphabets {

    private String alphabetName;
    private String alphabetUrl;

    public Alphabets() {}

    public Alphabets(String alphabetName, String alphabetUrl) {
        if(alphabetName.trim().equals(""))
        {
            alphabetName="No name";
        }
        this.alphabetName = alphabetName;
        this.alphabetUrl = alphabetUrl;
    }

    public String getAlphabetName() {
        return alphabetName;
    }

    public void setAlphabetName(String alphabetName) {
        this.alphabetName = alphabetName;
    }

    public String getAlphabetUrl() {
        return alphabetUrl;
    }

    public void setAlphabetUrl(String alphabetUrl) {
        this.alphabetUrl = alphabetUrl;
    }
}
