package com.example.sambungayat;

public class LeaderboardUser {
    private int rank;
    private String name;
    private int score;
    private String info; // Misalnya: "Waktu main: 2023-10-01"

    public LeaderboardUser(int rank, String name, int score, String info) {
        this.rank = rank;
        this.name = name;
        this.score = score;
        this.info = info;
    }

    public int getRank() { return rank; }
    public String getName() { return name; }
    public int getScore() { return score; }
    public String getInfo() { return info; }
}