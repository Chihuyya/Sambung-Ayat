package com.example.sambungayat;

public class Config {
    public static final String IP_SERVER = "192.168.1.11";
    public static final String BASE_URL = "http://" + IP_SERVER + "/api_sambung_ayat/";

    public static final String URL_GET_AYAT = BASE_URL + "get_ayat.php?surah_id=";
    public static final String URL_GET_SOAL = BASE_URL + "get_soal.php?surah_id=";
    public static final String URL_LOGIN = BASE_URL + "login.php";
    public static final String URL_REGISTER = BASE_URL + "register.php";
    public static final String URL_SUBMIT_SCORE = BASE_URL + "submit_score.php";
    public static final String URL_GET_SURAHS = BASE_URL + "get_surah.php";
    public static final String URL_UPDATE_PROGRESS = BASE_URL + "update_progress.php";
    public static final String URL_LEADERBOARD = BASE_URL + "get_leaderboard.php";
    public static final String URL_GET_PROFILE = BASE_URL + "get_profile.php";
    public static final String URL_GET_ACHIEVEMENTS = BASE_URL + "get_achievements.php";
    public static final String URL_UPDATE_PROFILE = BASE_URL + "update_profile.php";
    public static final String URL_LOGIN_GOOGLE = BASE_URL + "login_google.php";
    public static final String URL_UPDATE_PERFECT = BASE_URL + "update_perfect.php";
    public static final String URL_RESET_PASSWORD = BASE_URL + "reset_password.php";
}