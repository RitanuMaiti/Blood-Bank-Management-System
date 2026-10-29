package bloodbank;

import bloodbank.service.MatchingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class BloodCompatibilityTest {

    @Test
    @DisplayName("O- is universal donor for cellular components")
    void testUniversalCellularDonor() {
        String[] allRecipients = {"O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+"};
        for (String recipient : allRecipients) {
            assertTrue(MatchingService.isCompatible("O-", recipient, "Red Blood Cells"),
                    "O- should be compatible with " + recipient + " for Red Blood Cells");
            assertTrue(MatchingService.isCompatible("O-", recipient, "Packed Red Cells"),
                    "O- should be compatible with " + recipient + " for Packed Red Cells");
        }
    }


    @Test
    @DisplayName("Whole blood requires an exact ABO/Rh match under the conservative policy")
    void testWholeBloodRequiresExactMatch() {
        assertTrue(MatchingService.isCompatible("O-", "O-", "Whole Blood"));
        assertTrue(MatchingService.isCompatible("AB+", "AB+", "Whole Blood"));
        assertFalse(MatchingService.isCompatible("O-", "A+", "Whole Blood"),
                "Do not assume O- whole blood is universal without a validated low-titer protocol");
        assertFalse(MatchingService.isCompatible("AB+", "AB-", "Whole Blood"));
    }

    @Test
    @DisplayName("AB+ is universal recipient for cellular components")
    void testUniversalCellularRecipient() {
        String[] allDonors = {"O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+"};
        for (String donor : allDonors) {
            assertTrue(MatchingService.isCompatible(donor, "AB+", "Red Blood Cells"),
                    donor + " should be cellular compatible with AB+");
        }
    }

    @ParameterizedTest
    @CsvSource({
            "O+, O-, false",
            "A+, O+, false",
            "B+, A+, false",
            "AB+, A+, false",
            "A-, B+, false",
            "B-, A-, false",
            "AB-, A+, false"
    })
    @DisplayName("Incompatible cellular combinations must be rejected")
    void testIncompatibleCellular(String donor, String recipient, boolean expected) {
        assertEquals(expected, MatchingService.isCompatible(donor, recipient, "Red Blood Cells"));
    }

    @Test
    @DisplayName("AB is universal donor for Plasma (Fresh Frozen Plasma)")
    void testUniversalPlasmaDonor() {
        String[] allRecipients = {"O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+"};
        for (String recipient : allRecipients) {
            assertTrue(MatchingService.isCompatible("AB+", recipient, "Fresh Frozen Plasma"),
                    "AB+ plasma should be compatible with recipient " + recipient);
            assertTrue(MatchingService.isCompatible("AB-", recipient, "Plasma"),
                    "AB- plasma should be compatible with recipient " + recipient);
        }
    }

    @Test
    @DisplayName("O is universal recipient for Plasma")
    void testUniversalPlasmaRecipient() {
        String[] allDonors = {"O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+"};
        for (String donor : allDonors) {
            assertTrue(MatchingService.isCompatible(donor, "O+", "Fresh Frozen Plasma"),
                    donor + " plasma should be compatible with O+ recipient");
            assertTrue(MatchingService.isCompatible(donor, "O-", "Plasma"),
                    donor + " plasma should be compatible with O- recipient");
        }
    }

    @Test
    @DisplayName("O plasma to non-O recipient must be rejected (contains anti-A & anti-B antibodies)")
    void testDangerousPlasmaRejection() {
        assertFalse(MatchingService.isCompatible("O+", "AB+", "Fresh Frozen Plasma"),
                "O+ plasma contains anti-A/anti-B and must be rejected for AB+ recipient");
        assertFalse(MatchingService.isCompatible("O-", "A+", "Plasma"),
                "O- plasma contains anti-A and must be rejected for A+ recipient");
        assertFalse(MatchingService.isCompatible("O+", "B+", "Fresh Frozen Plasma"),
                "O+ plasma contains anti-B and must be rejected for B+ recipient");
    }

    @Test
    @DisplayName("Platelets require exact ABO/Rh match under standard conservative protocol")
    void testPlateletMatching() {
        assertTrue(MatchingService.isCompatible("A+", "A+", "Platelets"));
        assertTrue(MatchingService.isCompatible("O-", "O-", "Platelets"));
        assertFalse(MatchingService.isCompatible("O-", "A+", "Platelets"),
                "Different group platelets should be rejected under conservative protocol");
        assertFalse(MatchingService.isCompatible("A+", "B+", "Platelets"));
    }

    @Test
    @DisplayName("Component normalization handles aliases correctly")
    void testComponentNormalization() {
        assertEquals("Red Blood Cells", MatchingService.normalizeComponent("Packed Red Cells"));
        assertEquals("Red Blood Cells", MatchingService.normalizeComponent("red blood cells"));
        assertEquals("Fresh Frozen Plasma", MatchingService.normalizeComponent("Plasma"));
        assertEquals("Fresh Frozen Plasma", MatchingService.normalizeComponent("fresh frozen plasma"));
        assertEquals("Platelets", MatchingService.normalizeComponent("platelets"));
        assertEquals("Whole Blood", MatchingService.normalizeComponent("Whole Blood"));

        assertTrue(MatchingService.componentsMatch("Packed Red Cells", "Red Blood Cells"));
        assertTrue(MatchingService.componentsMatch("Plasma", "Fresh Frozen Plasma"));
    }

    @Test
    @DisplayName("Null and invalid parameters safely return false")
    void testNullAndInvalidInputs() {
        assertFalse(MatchingService.isCompatible(null, "O+", "Whole Blood"));
        assertFalse(MatchingService.isCompatible("O+", null, "Whole Blood"));
        assertFalse(MatchingService.isCompatible("O+", "O+", null));
        assertFalse(MatchingService.isCompatible("INVALID", "O+", "Whole Blood"));
    }
}
