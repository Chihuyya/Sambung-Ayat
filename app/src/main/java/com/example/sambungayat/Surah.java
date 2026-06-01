package com.example.sambungayat;

public class Surah {
    private final int id;
    private final int surahNumber;
    private final String name;
    private final String nameArabic;
    private final int totalVerses;
    private final String status;
    private final int progress; // Tambahkan field progress
    private boolean isSelected = false;

    public Surah(int id, int surahNumber, String name, String nameArabic, int totalVerses, String status, int progress) {
        this.id = id;
        this.surahNumber = surahNumber;
        this.name = name;
        this.nameArabic = nameArabic;
        this.totalVerses = totalVerses;
        this.status = status;
        this.progress = progress;
    }

    public int getId() { return id; }
    public int getSurahNumber() { return surahNumber; }
    
    public String getName() { 
        return (name == null || name.trim().isEmpty()) ? "Surah " + surahNumber : name; 
    }
    
    public String getNameArabic() { 
        return (nameArabic == null || nameArabic.trim().isEmpty()) ? "" : nameArabic; 
    }

    public int getTotalVerses() { return totalVerses; }
    public String getStatus() { return status; }
    public int getProgress() { return progress; }
    
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}