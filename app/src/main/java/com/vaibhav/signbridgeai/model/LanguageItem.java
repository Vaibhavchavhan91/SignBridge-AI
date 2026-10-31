package com.vaibhav.signbridgeai.model;

/**
 * Model representing a country and language configuration for speech recognition & TTS.
 */
public class LanguageItem {
    private final String code;
    private final String languageName;
    private final String englishName;
    private final String countryName;
    private final String flagEmoji;
    private final String region;

    public LanguageItem(String code, String languageName, String englishName, String countryName, String flagEmoji, String region) {
        this.code = code;
        this.languageName = languageName;
        this.englishName = englishName;
        this.countryName = countryName;
        this.flagEmoji = flagEmoji;
        this.region = region;
    }

    public String getCode() {
        return code;
    }

    public String getLanguageName() {
        return languageName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getFlagEmoji() {
        return flagEmoji != null ? flagEmoji : "🌐";
    }

    public String getRegion() {
        return region;
    }

    public String getFullDisplayName() {
        return flagEmoji + " " + languageName + " (" + countryName + ")";
    }

    public String getShortBadge() {
        return flagEmoji + " " + countryName;
    }
}
