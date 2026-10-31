package com.vaibhav.signbridgeai.util;

import com.vaibhav.signbridgeai.model.LanguageItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registry of supported worldwide languages and countries for speech recognition and TTS.
 */
public class LanguageHelper {

    private static final List<LanguageItem> LANGUAGES = new ArrayList<>();

    public static final String REGION_ALL = "All Regions";
    public static final String REGION_AMERICAS = "Americas";
    public static final String REGION_ASIA = "Asia & Pacific";
    public static final String REGION_EUROPE = "Europe";
    public static final String REGION_ME_AFRICA = "Middle East & Africa";

    static {
        // Americas
        LANGUAGES.add(new LanguageItem("en-US", "English", "English", "United States", "🇺🇸", REGION_AMERICAS));
        LANGUAGES.add(new LanguageItem("es-MX", "Español", "Spanish", "Mexico", "🇲🇽", REGION_AMERICAS));
        LANGUAGES.add(new LanguageItem("pt-BR", "Português", "Portuguese", "Brazil", "🇧🇷", REGION_AMERICAS));
        LANGUAGES.add(new LanguageItem("en-CA", "English", "English", "Canada", "🇨🇦", REGION_AMERICAS));
        LANGUAGES.add(new LanguageItem("fr-CA", "Français", "French", "Canada", "🇨🇦", REGION_AMERICAS));

        // Asia & Pacific
        LANGUAGES.add(new LanguageItem("en-IN", "English", "English", "India", "🇮🇳", REGION_ASIA));
        LANGUAGES.add(new LanguageItem("hi-IN", "हिन्दी", "Hindi", "India", "🇮🇳", REGION_ASIA));
        LANGUAGES.add(new LanguageItem("mr-IN", "मराठी", "Marathi", "India", "🇮🇳", REGION_ASIA));
        LANGUAGES.add(new LanguageItem("bn-IN", "বাংলা", "Bengali", "India", "🇮🇳", REGION_ASIA));
        LANGUAGES.add(new LanguageItem("ta-IN", "தமிழ்", "Tamil", "India", "🇮🇳", REGION_ASIA));
        LANGUAGES.add(new LanguageItem("te-IN", "తెలుగు", "Telugu", "India", "🇮🇳", REGION_ASIA));
        LANGUAGES.add(new LanguageItem("gu-IN", "ગુજરાતી", "Gujarati", "India", "🇮🇳", REGION_ASIA));
        LANGUAGES.add(new LanguageItem("kn-IN", "ಕನ್ನಡ", "Kannada", "India", "🇮🇳", REGION_ASIA));
        LANGUAGES.add(new LanguageItem("ja-JP", "日本語", "Japanese", "Japan", "🇯🇵", REGION_ASIA));
        LANGUAGES.add(new LanguageItem("ko-KR", "한국어", "Korean", "South Korea", "🇰🇷", REGION_ASIA));
        LANGUAGES.add(new LanguageItem("zh-CN", "中文 (简体)", "Chinese Simplified", "China", "🇨🇳", REGION_ASIA));
        LANGUAGES.add(new LanguageItem("zh-TW", "繁體中文", "Chinese Traditional", "Taiwan", "🇹🇼", REGION_ASIA));
        LANGUAGES.add(new LanguageItem("id-ID", "Bahasa Indonesia", "Indonesian", "Indonesia", "🇮🇩", REGION_ASIA));
        LANGUAGES.add(new LanguageItem("vi-VN", "Tiếng Việt", "Vietnamese", "Vietnam", "🇻🇳", REGION_ASIA));
        LANGUAGES.add(new LanguageItem("fil-PH", "Filipino", "Tagalog", "Philippines", "🇵🇭", REGION_ASIA));
        LANGUAGES.add(new LanguageItem("en-AU", "English", "English", "Australia", "🇦🇺", REGION_ASIA));

        // Europe
        LANGUAGES.add(new LanguageItem("en-GB", "English", "English", "United Kingdom", "🇬🇧", REGION_EUROPE));
        LANGUAGES.add(new LanguageItem("fr-FR", "Français", "French", "France", "🇫🇷", REGION_EUROPE));
        LANGUAGES.add(new LanguageItem("es-ES", "Español", "Spanish", "Spain", "🇪🇸", REGION_EUROPE));
        LANGUAGES.add(new LanguageItem("de-DE", "Deutsch", "German", "Germany", "🇩🇪", REGION_EUROPE));
        LANGUAGES.add(new LanguageItem("it-IT", "Italiano", "Italian", "Italy", "🇮🇹", REGION_EUROPE));
        LANGUAGES.add(new LanguageItem("pt-PT", "Português", "Portuguese", "Portugal", "🇵🇹", REGION_EUROPE));
        LANGUAGES.add(new LanguageItem("ru-RU", "Русский", "Russian", "Russia", "🇷🇺", REGION_EUROPE));
        LANGUAGES.add(new LanguageItem("nl-NL", "Nederlands", "Dutch", "Netherlands", "🇳🇱", REGION_EUROPE));
        LANGUAGES.add(new LanguageItem("pl-PL", "Polski", "Polish", "Poland", "🇵🇱", REGION_EUROPE));
        LANGUAGES.add(new LanguageItem("sv-SE", "Svenska", "Swedish", "Sweden", "🇸🇪", REGION_EUROPE));

        // Middle East & Africa
        LANGUAGES.add(new LanguageItem("ar-SA", "العربية", "Arabic", "Saudi Arabia", "🇸🇦", REGION_ME_AFRICA));
        LANGUAGES.add(new LanguageItem("ar-AE", "العربية", "Arabic", "United Arab Emirates", "🇦🇪", REGION_ME_AFRICA));
        LANGUAGES.add(new LanguageItem("tr-TR", "Türkçe", "Turkish", "Turkey", "🇹🇷", REGION_ME_AFRICA));
    }

    public static List<LanguageItem> getAllLanguages() {
        return Collections.unmodifiableList(LANGUAGES);
    }

    public static List<LanguageItem> getPopularLanguages() {
        List<LanguageItem> list = new ArrayList<>();
        list.add(getLanguageByCode("en-US"));
        list.add(getLanguageByCode("en-IN"));
        list.add(getLanguageByCode("hi-IN"));
        list.add(getLanguageByCode("mr-IN"));
        list.add(getLanguageByCode("en-GB"));
        list.add(getLanguageByCode("es-ES"));
        list.add(getLanguageByCode("fr-FR"));
        list.add(getLanguageByCode("ja-JP"));
        return list;
    }

    public static LanguageItem getLanguageByCode(String code) {
        if (code == null) return LANGUAGES.get(0);
        for (LanguageItem item : LANGUAGES) {
            if (item.getCode().equalsIgnoreCase(code)) {
                return item;
            }
        }
        // Fallback default English US
        return LANGUAGES.get(0);
    }

    public static List<LanguageItem> search(String query, String regionFilter) {
        List<LanguageItem> result = new ArrayList<>();
        String q = query != null ? query.trim().toLowerCase() : "";
        boolean filterRegion = regionFilter != null && !REGION_ALL.equalsIgnoreCase(regionFilter);

        for (LanguageItem item : LANGUAGES) {
            if (filterRegion && !item.getRegion().equalsIgnoreCase(regionFilter)) {
                continue;
            }

            if (q.isEmpty()) {
                result.add(item);
                continue;
            }

            boolean match = item.getCountryName().toLowerCase().contains(q) ||
                    item.getLanguageName().toLowerCase().contains(q) ||
                    item.getEnglishName().toLowerCase().contains(q) ||
                    item.getCode().toLowerCase().contains(q);

            if (match) {
                result.add(item);
            }
        }
        return result;
    }

    public static String[] getRegions() {
        return new String[]{
                REGION_ALL,
                REGION_AMERICAS,
                REGION_ASIA,
                REGION_EUROPE,
                REGION_ME_AFRICA
        };
    }
}
