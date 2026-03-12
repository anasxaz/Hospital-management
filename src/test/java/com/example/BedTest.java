package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BedTest {

    private Bed bed;
    private Patient patient;

    @BeforeEach
    void setUp() {
        bed = new Bed(1);
        patient = mock(Patient.class);
    }

    @Test
    void testConstructor_initialState() {
        assertEquals(1, bed.getBedNumber());
        assertFalse(bed.isOccupied());
        assertNull(bed.getOccupiedDate());
        assertNull(bed.getPatient());
    }

    @Test
    void testGetBedNumber_multipleInstances() {
        Bed bed5 = new Bed(5);
        Bed bed10 = new Bed(10);
        assertEquals(5, bed5.getBedNumber());
        assertEquals(10, bed10.getBedNumber());
    }

    @Test
    void testOccupy_setsOccupiedState() {
        bed.occupy(patient, "2024-01-15");

        assertTrue(bed.isOccupied());
        assertEquals("2024-01-15", bed.getOccupiedDate());
        assertSame(patient, bed.getPatient());
    }

    @Test
    void testRelease_clearsOccupiedState() {
        bed.occupy(patient, "2024-01-15");
        bed.release();

        assertFalse(bed.isOccupied());
        assertNull(bed.getOccupiedDate());
        assertNull(bed.getPatient());
    }

    @Test
    void testOccupy_thenRelease_thenOccupyAgain() {
        Patient secondPatient = mock(Patient.class);

        bed.occupy(patient, "2024-01-15");
        bed.release();
        bed.occupy(secondPatient, "2024-02-20");

        assertTrue(bed.isOccupied());
        assertEquals("2024-02-20", bed.getOccupiedDate());
        assertSame(secondPatient, bed.getPatient());
    }

    @Test
    void testIsOccupied_beforeAndAfterOccupy() {
        assertFalse(bed.isOccupied());
        bed.occupy(patient, "2024-03-10");
        assertTrue(bed.isOccupied());
    }
}
