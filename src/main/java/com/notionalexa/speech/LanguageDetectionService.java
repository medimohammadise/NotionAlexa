package com.notionalexa.speech;

import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

/**
 * Detects the language of text and provides Alexa SSML language codes.
 *
 * <p>Uses Java's built-in {@link Character.UnicodeScript} API to identify
 * non-Latin scripts (Japanese, Chinese, Korean, Arabic, Cyrillic, etc.) with
 * 100% reliability and zero extra dependencies. For Latin-script languages
 * (English, German, French, Spanish, etc.) it falls back to a lightweight
 * scored character-frequency heuristic.</p>
 */
@Service
public class LanguageDetectionService {

    /** Minimum heuristic score required to confidently label a Latin-script language. */
    private static final int MIN_CONFIDENCE_THRESHOLD = 2;

    // --- German ---
    private static final char[]   DE_CHARS = {'ä', 'ö', 'ü', 'Ä', 'Ö', 'Ü'};
    private static final String[] DE_WORDS = {
        // Core function words (original)
        "der ", "die ", "das ", "und ", "nicht ", "ich ", "sie ", "mit ",
        // Conjunctions and connectives
        "auch ", "aber ", "oder ", "weil ", "wenn ", "noch ", "schon ", "doch ",
        // Common prepositions (safe: don't appear in English/other-language words with trailing space)
        "auf ", "aus ", "bei ", "seit ", "vom ", "zum ", "zur ", "beim ", "ohne ",
        // Common verbs
        "gibt ", "kann ", "haben ", "werden ",
        // Common adverbs and determiners
        "sehr ", "viel ", "jetzt ", "heute ", "keine ", "guten "
    };

    // --- French ---
    private static final char[]   FR_DISTINCTIVE_CHARS = {'ç', 'œ', 'æ'};
    private static final char[]   FR_ACCENT_CHARS      = {'é', 'è', 'ê', 'à', 'â', 'î', 'ô', 'û'};
    private static final String[] FR_WORDS             = {"les ", "des ", "une ", "est ", "que ", "dans ", "pas ", "sur "};

    // --- Spanish ---
    private static final char[]   ES_ACCENT_CHARS = {'á', 'í', 'ó', 'ú', 'ü'};
    private static final String[] ES_WORDS        = {"los ", "las ", "una ", "del ", "con ", "para ", "por ", "que "};

    // --- Italian ---
    private static final char[]   IT_ACCENT_CHARS = {'à', 'è', 'ì', 'ò', 'ù'};
    private static final String[] IT_WORDS        = {"gli ", "dello ", "della ", "degli ", "delle ", "sono ", "nella "};

    // --- Dutch ---
    // Note: "een " and "van " excluded — they appear as substrings in common English
    // words ("been", "seen") and as standalone English words ("a van").
    private static final String[] NL_WORDS = {"het ", "dat ", "zijn ", "heeft ", "naar "};

    // --- Portuguese ---
    // Note: "com " excluded (URL/informal English), "dos " excluded ("dos and don'ts")
    private static final String[] PT_WORDS = {"não ", "uma ", "das ", "para ", "que "};

    /**
     * Detect the language of the given text.
     *
     * @param text text to detect language for
     * @return ISO 639-1 language code (e.g. "de", "en", "ja")
     */
    public String detectLanguage(String text) {
        if (text == null || text.isBlank()) {
            return "en";
        }

        // Count meaningful (non-whitespace, non-digit) characters per Unicode script
        Map<Character.UnicodeScript, Integer> scriptCounts = new EnumMap<>(Character.UnicodeScript.class);
        for (int i = 0; i < text.length(); i++) {
            int cp = text.codePointAt(i);
            if (!Character.isWhitespace(cp) && !Character.isDigit(cp)) {
                Character.UnicodeScript script = Character.UnicodeScript.of(cp);
                if (script != Character.UnicodeScript.COMMON
                        && script != Character.UnicodeScript.INHERITED
                        && script != Character.UnicodeScript.UNKNOWN) {
                    scriptCounts.merge(script, 1, Integer::sum);
                }
            }
            if (Character.isSupplementaryCodePoint(cp)) {
                i++; // skip the low surrogate
            }
        }

        if (scriptCounts.isEmpty()) {
            return "en";
        }

        // Find the dominant script
        Character.UnicodeScript dominant = scriptCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(Character.UnicodeScript.LATIN);

        boolean hasHiragana = scriptCounts.containsKey(Character.UnicodeScript.HIRAGANA);
        boolean hasKatakana = scriptCounts.containsKey(Character.UnicodeScript.KATAKANA);

        return switch (dominant) {
            case HIRAGANA, KATAKANA -> "ja";
            case HAN -> (hasHiragana || hasKatakana) ? "ja" : "zh";
            case HANGUL -> "ko";
            case ARABIC -> "ar";
            case CYRILLIC -> "ru";
            case GREEK -> "el";
            case HEBREW -> "he";
            case THAI -> "th";
            case DEVANAGARI -> "hi";
            default -> detectLatinLanguage(text);
        };
    }

    /**
     * Heuristic detection for Latin-script languages based on scored distinctive
     * characters and high-frequency function words.
     */
    private String detectLatinLanguage(String text) {
        String lower = text.toLowerCase();

        int deScore = 0;
        int frScore = 0;
        int esScore = 0;
        int itScore = 0;
        int nlScore = 0;
        int ptScore = 0;

        // German
        for (char c : DE_CHARS) {
            if (text.indexOf(c) >= 0) deScore += 3;
        }
        if (lower.contains("ß")) deScore += 4;
        for (String w : DE_WORDS) {
            if (lower.contains(w)) deScore += 2;
        }

        // French
        for (char c : FR_DISTINCTIVE_CHARS) {
            if (text.indexOf(c) >= 0) frScore += 3;
        }
        for (char c : FR_ACCENT_CHARS) {
            if (text.indexOf(c) >= 0) frScore += 2;
        }
        for (String w : FR_WORDS) {
            if (lower.contains(w)) frScore += 2;
        }

        // Spanish
        if (lower.contains("ñ")) esScore += 4;
        for (char c : ES_ACCENT_CHARS) {
            if (text.indexOf(c) >= 0) esScore += 2;
        }
        if (lower.startsWith("¿") || lower.startsWith("¡")) esScore += 4;
        for (String w : ES_WORDS) {
            if (lower.contains(w)) esScore += 2;
        }

        // Italian
        for (char c : IT_ACCENT_CHARS) {
            if (text.indexOf(c) >= 0) itScore += 1;
        }
        for (String w : IT_WORDS) {
            if (lower.contains(w)) itScore += 3;
        }

        // Dutch
        for (String w : NL_WORDS) {
            if (lower.contains(w)) nlScore += 2;
        }

        // Portuguese
        if (lower.contains("ã") || lower.contains("õ")) ptScore += 4;
        for (String w : PT_WORDS) {
            if (lower.contains(w)) ptScore += 2;
        }

        int maxScore = java.util.stream.IntStream.of(deScore, frScore, esScore, itScore, nlScore, ptScore)
                .max().orElse(0);

        if (maxScore < MIN_CONFIDENCE_THRESHOLD) {
            return "en";
        }
        if (deScore == maxScore) return "de";
        if (frScore == maxScore) return "fr";
        if (esScore == maxScore) return "es";
        if (itScore == maxScore) return "it";
        if (nlScore == maxScore) return "nl";
        if (ptScore == maxScore) return "pt";
        return "en";
    }

    /**
     * Maps an ISO 639-1 language code to the SSML {@code xml:lang} attribute
     * value supported by Alexa's {@code <lang>} tag.
     *
     * @param languageCode ISO 639-1 language code
     * @return SSML language code accepted by Alexa
     */
    public String getSsmlLanguageCode(String languageCode) {
        return switch (languageCode.toLowerCase()) {
            case "de" -> "de-DE";
            case "fr" -> "fr-FR";
            case "es" -> "es-ES";
            case "it" -> "it-IT";
            case "ja" -> "ja-JP";
            case "pt" -> "pt-BR";
            case "nl" -> "nl-NL";
            case "zh" -> "zh-CN";
            case "ko" -> "ko-KR";
            case "ar" -> "ar-AE";
            case "hi" -> "hi-IN";
            case "ru" -> "ru-RU";
            case "el" -> "el-GR";
            default -> "en-US";
        };
    }
}
