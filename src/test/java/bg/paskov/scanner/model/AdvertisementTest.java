package bg.paskov.scanner.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdvertisementTest {
    private Advertisement ad1;

    @BeforeEach
    void setUp() {
         ad1 = new Advertisement("Title A", "Link A", "ID123");

    }

    @Test
    void  ad1ShouldNotBeNull() {
        assertNotNull(ad1);
    }

    @Test
    void testEquality() {
        Advertisement ad2 = new Advertisement("Title B", "Link B", "ID123");

        assertEquals(ad1, ad2, "Ads with same ID must be equal");
    }

    @Test
    void testHashCode() {
        Advertisement ad2 = new Advertisement("Title B", "Link B", "ID123");

        assertEquals(ad1.hashCode(), ad2.hashCode());
    }

    @Test
    void testNotEqual() {
        Advertisement ad2 = new Advertisement("Title B", "Link B", "ID153");

        assertNotEquals(ad1, ad2, "Ads with different IDs should not be equal");
    }

    @Test
    void testReflexive() {
        Advertisement ad = new Advertisement("Title", "Link", "ID123");
        assertEquals(ad, ad);
    }

}