package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PatientTest {

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient("John Doe", 30, "Male");
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals("John Doe", patient.getName());
        assertEquals(30, patient.getAge());
        assertEquals("Male", patient.getGender());
        assertEquals(0, patient.getDaysOfStay());
        assertEquals(0, patient.getTotalBill());
    }

    @Test
    void testSetDaysOfStay() {
        patient.setDaysOfStay(5);
        assertEquals(5, patient.getDaysOfStay());
    }

    @Test
    void testAddToBill_singleAmount() {
        patient.addToBill(100);
        assertEquals(100, patient.getTotalBill());
    }

    @Test
    void testAddToBill_accumulated() {
        patient.addToBill(100);
        patient.addToBill(500);
        assertEquals(600, patient.getTotalBill());
    }

    @Test
    void testSetTotalBill() {
        patient.setTotalBill(1500);
        assertEquals(1500, patient.getTotalBill());
    }

    @Test
    void testClearBill_resetsBillAndStay() {
        patient.addToBill(500);
        patient.setDaysOfStay(3);
        patient.clearBill();
        assertEquals(0, patient.getTotalBill());
        assertEquals(0, patient.getDaysOfStay());
    }

    @Test
    void testHasPaidBill_whenBillIsZero_returnsTrue() {
        assertTrue(patient.hasPaidBill());
    }

    @Test
    void testHasPaidBill_whenBillIsNonZero_returnsFalse() {
        patient.addToBill(100);
        assertFalse(patient.hasPaidBill());
    }

    @Test
    void testHasPaidBill_afterClear_returnsTrue() {
        patient.addToBill(200);
        patient.clearBill();
        assertTrue(patient.hasPaidBill());
    }

    @Test
    void testToString() {
        assertEquals("John Doe (30, Male)", patient.toString());
    }
}
