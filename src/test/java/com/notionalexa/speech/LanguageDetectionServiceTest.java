package com.notionalexa.speech;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that {@link LanguageDetectionService} correctly identifies the language of
 * typical German sentences, including those that lack umlauts or ß, so that
 * {@link SpeechService#toSentenceTaggedSsml} can tag them with the right Alexa voice.
 */
class LanguageDetectionServiceTest {

    private LanguageDetectionService service;

    @BeforeEach
    void setUp() {
        service = new LanguageDetectionService();
    }

    // ---- German sentences WITH umlauts (should always have been detected) ----

    @Test
    void detectLanguage_germanWithUmlauts_returnsGerman() {
        assertThat(service.detectLanguage("Das Wetter ist schön heute.")).isEqualTo("de");
    }

    // ---- German sentences WITHOUT umlauts (previously misdetected as English) ----

    @Test
    void detectLanguage_gutenMorgen_returnsGerman() {
        assertThat(service.detectLanguage("Guten Morgen!")).isEqualTo("de");
    }

    @Test
    void detectLanguage_germanConjunction_returnsGerman() {
        assertThat(service.detectLanguage("Das ist aber sehr wichtig.")).isEqualTo("de");
    }

    @Test
    void detectLanguage_germanVerbGibt_returnsGerman() {
        assertThat(service.detectLanguage("Es gibt viele Moeglichkeiten.")).isEqualTo("de");
    }

    @Test
    void detectLanguage_germanKann_returnsGerman() {
        assertThat(service.detectLanguage("Wir können das machen.")).isEqualTo("de");
    }

    @Test
    void detectLanguage_germanHeute_returnsGerman() {
        assertThat(service.detectLanguage("Heute ist ein guter Tag.")).isEqualTo("de");
    }

    @Test
    void detectLanguage_germanOder_returnsGerman() {
        assertThat(service.detectLanguage("Kaffee oder Tee?")).isEqualTo("de");
    }

    @Test
    void detectLanguage_germanWeil_returnsGerman() {
        assertThat(service.detectLanguage("Das geht nicht, weil es falsch ist.")).isEqualTo("de");
    }

    @Test
    void detectLanguage_germanAuch_returnsGerman() {
        assertThat(service.detectLanguage("Das ist auch korrekt.")).isEqualTo("de");
    }

    // ---- English sentences should NOT be detected as German ----

    @Test
    void detectLanguage_plainEnglish_returnsEnglish() {
        assertThat(service.detectLanguage("This is a note about project planning.")).isEqualTo("en");
    }

    @Test
    void detectLanguage_shortEnglish_returnsEnglish() {
        assertThat(service.detectLanguage("Meeting tomorrow at 10am.")).isEqualTo("en");
    }

    // ---- SSML language code mapping ----

    @Test
    void getSsmlLanguageCode_german_returnsDeDE() {
        assertThat(service.getSsmlLanguageCode("de")).isEqualTo("de-DE");
    }

    @Test
    void getSsmlLanguageCode_japanese_returnsJaJP() {
        assertThat(service.getSsmlLanguageCode("ja")).isEqualTo("ja-JP");
    }

    @Test
    void getSsmlLanguageCode_unknown_returnsEnUS() {
        assertThat(service.getSsmlLanguageCode("xx")).isEqualTo("en-US");
    }

    // ---- Non-Latin script detection ----

    @Test
    void detectLanguage_japaneseHiragana_returnsJapanese() {
        assertThat(service.detectLanguage("これは日本語のテキストです。")).isEqualTo("ja");
    }

    @Test
    void detectLanguage_arabicScript_returnsArabic() {
        assertThat(service.detectLanguage("هذا نص باللغة العربية")).isEqualTo("ar");
    }
}
