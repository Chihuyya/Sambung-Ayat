package com.example.sambungayat;

public class Surah {
    private int id;
    private String nameId;
    private String nameArabic;
    private int totalVerses;

    public Surah(int id, String nameId, String nameArabic, int totalVerses) {
        this.id = id;
        this.nameId = nameId;
        this.nameArabic = nameArabic;
        this.totalVerses = totalVerses;
    }

    public int getId() { return id; }
    public String getNameId() { return nameId; }
    public String getNameArabic() { return nameArabic; }
    public int getTotalVerses() { return totalVerses; }
}