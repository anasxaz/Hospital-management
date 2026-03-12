package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppointmentTest {

    private Patient patient;
    private Doctor doctor;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        patient = mock(Patient.class);
        doctor = mock(Doctor.class);
        appointment = new Appointment(patient, doctor, "2024-01-15", "10:00");
    }

    @Test
    void testGetPatient() {
        assertSame(patient, appointment.getPatient());
    }

    @Test
    void testGetDoctor() {
        assertSame(doctor, appointment.getDoctor());
    }

    @Test
    void testGetDate() {
        assertEquals("2024-01-15", appointment.getDate());
    }

    @Test
    void testGetTimeSlot() {
        assertEquals("10:00", appointment.getTimeSlot());
    }

    @Test
    void testToString_containsDateAndTimeSlot() {
        when(patient.toString()).thenReturn("John (30, Male)");
        when(doctor.toString()).thenReturn("Dr. Smith (Cardiology)");

        String result = appointment.toString();
        assertTrue(result.contains("2024-01-15"));
        assertTrue(result.contains("10:00"));
    }

    @Test
    void testConstructor_withRealObjects() {
        Patient realPatient = new Patient("Alice", 25, "Female");
        Doctor realDoctor = new Doctor("Dr. Brown", "Neurology");
        Appointment appt = new Appointment(realPatient, realDoctor, "2024-06-01", "14:30");

        assertEquals(realPatient, appt.getPatient());
        assertEquals(realDoctor, appt.getDoctor());
        assertEquals("2024-06-01", appt.getDate());
        assertEquals("14:30", appt.getTimeSlot());
    }
}
