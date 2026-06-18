package com.hp.jetadvantage.link.logdaemon.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UuidValidatorTest {

    @Test
    public void isValidUuid_withValidUuid_returnsTrue() {
        assertTrue(UuidValidator.isValidUuid("12345678-1234-1234-1234-123456789012"));
    }

    @Test
    public void isValidUuid_withUppercaseHex_returnsTrue() {
        assertTrue(UuidValidator.isValidUuid("ABCDEF01-2345-6789-ABCD-EF0123456789"));
    }

    @Test
    public void isValidUuid_withMixedCaseHex_returnsTrue() {
        assertTrue(UuidValidator.isValidUuid("aB12cD34-eF56-7890-Ab12-Cd34Ef567890"));
    }

    @Test
    public void isValidUuid_withNull_returnsFalse() {
        assertFalse(UuidValidator.isValidUuid(null));
    }

    @Test
    public void isValidUuid_withEmpty_returnsFalse() {
        assertFalse(UuidValidator.isValidUuid(""));
    }

    @Test
    public void isValidUuid_withPathTraversal_returnsFalse() {
        assertFalse(UuidValidator.isValidUuid("../../../etc/passwd"));
    }

    @Test
    public void isValidUuid_withShortString_returnsFalse() {
        assertFalse(UuidValidator.isValidUuid("1234"));
    }

    @Test
    public void isValidUuid_withMissingDashes_returnsFalse() {
        assertFalse(UuidValidator.isValidUuid("12345678123412341234123456789012"));
    }

    @Test
    public void isValidUuid_withExtraDashes_returnsFalse() {
        assertFalse(UuidValidator.isValidUuid("1234-5678-1234-1234-1234-123456789012"));
    }

    @Test
    public void isValidUuid_withNonHexCharacters_returnsFalse() {
        assertFalse(UuidValidator.isValidUuid("1234567g-1234-1234-1234-123456789012"));
    }

    @Test
    public void isValidUuid_withSpaces_returnsFalse() {
        assertFalse(UuidValidator.isValidUuid(" 12345678-1234-1234-1234-123456789012"));
    }

    @Test
    public void isValidUuid_withTrailingNewline_returnsFalse() {
        assertFalse(UuidValidator.isValidUuid("12345678-1234-1234-1234-123456789012\n"));
    }

    @Test
    public void isValidUuid_withAllKeyword_returnsFalse() {
        assertFalse(UuidValidator.isValidUuid("all"));
    }

}
