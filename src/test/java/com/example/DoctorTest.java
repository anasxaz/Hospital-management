package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DoctorTest {

    private Doctor doctor;

    @BeforeEach
    void setUp() {
        doctor = new Doctor("Dr. Smith", "Cardiology");
    }

    @Test
    void testToString() {
        assertEquals("Dr. Smith (Cardiology)", doctor.toString());
    }

    @Test
    void testEquals_sameInstance() {
        assertEquals(doctor, doctor);
    }

    @Test
    void testEquals_equalObjects() {
        Doctor other = new Doctor("Dr. Smith", "Cardiology");
        assertEquals(doctor, other);
    }

    @Test
    void testEquals_differentName() {
        Doctor other = new Doctor("Dr. Jones", "Cardiology");
        assertNotEquals(doctor, other);
    }

    @Test
    void testEquals_differentSpecialization() {
        Doctor other = new Doctor("Dr. Smith", "Neurology");
        assertNotEquals(doctor, other);
    }

    @Test
    void testEquals_null() {
        assertNotEquals(null, doctor);
    }

    @Test
    void testEquals_differentClass() {
        assertNotEquals("not a doctor", doctor);
    }

    @Test
    void testHashCode_equalObjects_haveSameHash() {
        Doctor other = new Doctor("Dr. Smith", "Cardiology");
        assertEquals(doctor.hashCode(), other.hashCode());
    }

    @Test
    void testHashCode_differentObjects_haveDifferentHash() {
        Doctor other = new Doctor("Dr. Jones", "Neurology");
        assertNotEquals(doctor.hashCode(), other.hashCode());
    }
}
