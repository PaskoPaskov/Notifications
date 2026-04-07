package bg.pаskov.scanner.model;

import bg.paskov.scanner.model.Advertisement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AdvertisementTest {

    @Test
    @DisplayName("Should be equal if IDs match, regardless of title or link")
    void testEquality() {
        Advertisement ad1 = new Advertisement("Title A", "Link A", "ID123");
        Advertisement ad2 = new Advertisement("Title B", "Link B", "ID123");

       Assertions.assertEquals(ad1, ad2, "Ads with same ID must be equal");
    }

    @Test
    @DisplayName("Should have same hash code if IDs match")
    void testHashCode() {
        Advertisement ad1 = new Advertisement("Title A", "Link A", "ID123");
        Advertisement ad2 = new Advertisement("Title B", "Link B", "ID123");

        Assertions.assertEquals(ad1.hashCode(), ad2.hashCode());
    }

    @Test
    @DisplayName("Should NOT be equal if IDs are different")
    void testNotEqual() {
        Advertisement ad1 = new Advertisement("Title A", "Link A", "ID123");
        Advertisement ad2 = new Advertisement("Title B", "Link B", "ID153");

        Assertions.assertNotEquals(ad1, ad2, "Ads with different IDs should not be equal");
    }

}