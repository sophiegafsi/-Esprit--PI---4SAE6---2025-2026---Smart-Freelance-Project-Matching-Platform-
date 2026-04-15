package tn.esprit.messagerie.services;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ContentValidatorServiceTest {

    private final ContentValidatorService validator = new ContentValidatorService();

    @Test
    void testPhoneNumbers() {
        assertTrue(validator.containsForbiddenContent("Call me at +216 99 123 456"));
        assertTrue(validator.containsForbiddenContent("My number: 99123456"));
        assertTrue(validator.containsForbiddenContent("00216 99123456"));
        assertTrue(validator.containsForbiddenContent("99-123-456"));
        assertFalse(validator.containsForbiddenContent("The year is 2024"));
    }

    @Test
    void testEmails() {
        assertTrue(validator.containsForbiddenContent("Email me: example@gmail.com"));
        assertTrue(validator.containsForbiddenContent("contact@company.fr"));
        assertTrue(validator.containsForbiddenContent("john.doe123@yahoo.com"));
        assertFalse(validator.containsForbiddenContent("just a message with no email"));
    }

    @Test
    void testUrls() {
        assertTrue(validator.containsForbiddenContent("Check this: https://wa.me/123456"));
        assertTrue(validator.containsForbiddenContent("Go to www.telegram.com/user"));
        assertTrue(validator.containsForbiddenContent("Pay here: http://paypal.me/john"));
        assertTrue(validator.containsForbiddenContent("Visit site.com"));
        assertFalse(validator.containsForbiddenContent("I'll see you at 5pm"));
    }

    @Test
    void testForbiddenPhrases() {
        assertTrue(validator.containsForbiddenContent("Pay me outside the platform"));
        assertTrue(validator.containsForbiddenContent("Contact me on WhatsApp for more details"));
        assertTrue(validator.containsForbiddenContent("Let's move to Telegram"));
        assertTrue(validator.containsForbiddenContent("Send payment directly to my bank"));
        assertTrue(validator.containsForbiddenContent("Avoid platform fees by paying direct"));
        assertFalse(validator.containsForbiddenContent("I will send the files soon"));
    }

    @Test
    void testMasking() {
        assertEquals("Call me at *****", validator.maskForbiddenContent("Call me at +216 99 123 456"));
        assertEquals("Email: *****", validator.maskForbiddenContent("Email: example@gmail.com"));
        assertEquals("Go to *****", validator.maskForbiddenContent("Go to https://wa.me/123456"));
        assertEquals("***** platform", validator.maskForbiddenContent("Pay me outside platform"));
        assertEquals("I will send the files", validator.maskForbiddenContent("I will send the files"));
    }
}
