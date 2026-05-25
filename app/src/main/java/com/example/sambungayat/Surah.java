package com.example.sambungayat;

public class Surah {
    private final int id;
    private final int surahNumber;
    private final String name;
    private final int totalVerses;
    private final String status; // 'locked', 'unlocked', 'passed'

    public Surah(int id, int surahNumber, String name, int totalVerses, String status) {
        this.id = id;
        this.surahNumber = surahNumber;
        this.name = name;
        this.totalVerses = totalVerses;
        this.status = status;
    }

    public int getId() { return id; }
    public int getSurahNumber() { return surahNumber; }
    public String getName() { return name; }
    public int getTotalVerses() { return totalVerses; }
    public String getStatus() { return status; }
}