package com.example.sambungayat;

public class Config {
    /**
     * PENTING:
     * 1. Jika pakai EMULATOR: Gunakan "http://10.0.2.2/API_sambung_ayat/"
     * 2. Jika pakai HP ASLI: Gunakan IP Laptop Anda (cek cmd: ipconfig), 
     *    contoh: "http://192.168.1.15/API_sambung_ayat/"
     *    Pastikan HP dan Laptop terhubung ke WiFi yang sama.
     */
    public static final String BASE_URL = "http://10.0.2.2/API_sambung_ayat/";

    public static final String URL_GET_AYAT = BASE_URL + "get_ayat.php?surah_id=";
    public static final String URL_LOGIN = BASE_URL + "login.php";
    public static final String URL_REGISTER = BASE_URL + "register.php";
    public static final String URL_SUBMIT_SCORE = BASE_URL + "submit_score.php";
    public static final String URL_GET_SURAHS = BASE_URL + "get_surahs.php";
    public static final String URL_UPDATE_PROGRESS = BASE_URL + "update_progress.php";
    public static final String URL_LEADERBOARD = BASE_URL + "leaderboard.php";
    public static final String URL_GET_PROFILE = BASE_URL + "get_profile.php";
    public static final String URL_GET_ACHIEVEMENTS = BASE_URL + "get_achievements.php";
    public static final String URL_UPDATE_PROFILE = BASE_URL + "update_profile.php";
}