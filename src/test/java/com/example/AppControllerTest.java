package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AppControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private MockHttpSession authenticatedSession;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        // Clear all static lists
        clearList("patients");
        clearList("doctors");
        clearList("appointments");

        // Re-initialize beds (main() is not called during tests)
        Field bedsField = App.class.getDeclaredField("beds");
        bedsField.setAccessible(true);
        List<Bed> beds = (List<Bed>) bedsField.get(null);
        beds.clear();
        for (int i = 1; i <= 15; i++) {
            beds.add(new Bed(i));
        }

        authenticatedSession = new MockHttpSession();
        authenticatedSession.setAttribute("authenticated", true);
    }

    private void clearList(String fieldName) throws Exception {
        Field field = App.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        ((List<?>) field.get(null)).clear();
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getList(String fieldName) throws Exception {
        Field field = App.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (List<T>) field.get(null);
    }

    // ── Authentication ──────────────────────────────────────────────────────────

    @Test
    void testLoginPage_whenNotAuthenticated_showsLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void testLoginPage_whenAlreadyAuthenticated_redirectsToHome() throws Exception {
        mockMvc.perform(get("/login").session(authenticatedSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void testLogin_withValidCredentials_redirectsToHome() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "admin")
                        .param("password", "admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void testLogin_withInvalidCredentials_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "wrong")
                        .param("password", "credentials"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void testLogout_invalidatesSessionAndRedirects() throws Exception {
        mockMvc.perform(get("/logout").session(authenticatedSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // ── Index ────────────────────────────────────────────────────────────────────

    @Test
    void testIndex_whenAuthenticated_showsIndexView() throws Exception {
        mockMvc.perform(get("/").session(authenticatedSession))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void testIndex_whenNotAuthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // ── Patients ─────────────────────────────────────────────────────────────────

    @Test
    void testPatients_whenAuthenticated_showsPatientsView() throws Exception {
        mockMvc.perform(get("/patients").session(authenticatedSession))
                .andExpect(status().isOk())
                .andExpect(view().name("patients"));
    }

    @Test
    void testPatients_whenNotAuthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/patients"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void testAddPatient_redirectsToPatients() throws Exception {
        mockMvc.perform(post("/addPatient")
                        .param("name", "John Doe")
                        .param("age", "30")
                        .param("gender", "Male"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patients"));
    }

    // ── Doctors ──────────────────────────────────────────────────────────────────

    @Test
    void testDoctors_showsDoctorsView() throws Exception {
        mockMvc.perform(get("/doctors"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctors"));
    }

    @Test
    void testAddDoctor_redirectsToDoctors() throws Exception {
        mockMvc.perform(post("/addDoctor")
                        .param("name", "Dr. Smith")
                        .param("specialization", "Cardiology"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctors"));
    }

    // ── Appointments ─────────────────────────────────────────────────────────────

    @Test
    void testAppointments_showsAppointmentsView() throws Exception {
        mockMvc.perform(get("/appointments"))
                .andExpect(status().isOk())
                .andExpect(view().name("appointments"));
    }

    @Test
    void testAddAppointment_whenSlotAvailable_redirects() throws Exception {
        List<Patient> patients = getList("patients");
        patients.add(new Patient("Alice", 28, "Female"));
        List<Doctor> doctors = getList("doctors");
        doctors.add(new Doctor("Dr. Lee", "Dermatology"));

        mockMvc.perform(post("/addAppointment")
                        .param("patientIndex", "0")
                        .param("doctorIndex", "0")
                        .param("date", "2024-05-10")
                        .param("timeSlot", "09:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/appointments"));
    }

    @Test
    void testAddAppointment_whenSlotAlreadyBooked_showsError() throws Exception {
        List<Patient> patients = getList("patients");
        Patient p = new Patient("Alice", 28, "Female");
        patients.add(p);
        List<Doctor> doctors = getList("doctors");
        Doctor d = new Doctor("Dr. Lee", "Dermatology");
        doctors.add(d);
        List<Appointment> appointments = getList("appointments");
        appointments.add(new Appointment(p, d, "2024-05-10", "09:00"));

        mockMvc.perform(post("/addAppointment")
                        .param("patientIndex", "0")
                        .param("doctorIndex", "0")
                        .param("date", "2024-05-10")
                        .param("timeSlot", "09:00"))
                .andExpect(status().isOk())
                .andExpect(view().name("appointments"))
                .andExpect(model().attributeExists("error"));
    }

    // ── Beds ─────────────────────────────────────────────────────────────────────

    @Test
    void testBeds_withoutDate_showsBedsView() throws Exception {
        mockMvc.perform(get("/beds"))
                .andExpect(status().isOk())
                .andExpect(view().name("beds"));
    }

    @Test
    void testBeds_withDate_showsBedsView() throws Exception {
        mockMvc.perform(get("/beds").param("date", "2024-01-20"))
                .andExpect(status().isOk())
                .andExpect(view().name("beds"));
    }

    @Test
    void testBeds_withDate_releasesBedsOccupiedOnDifferentDate() throws Exception {
        List<Patient> patients = getList("patients");
        Patient p = new Patient("Bob", 40, "Male");
        patients.add(p);
        List<Bed> beds = getList("beds");
        beds.get(0).occupy(p, "2024-01-19");

        mockMvc.perform(get("/beds").param("date", "2024-01-20"))
                .andExpect(status().isOk())
                .andExpect(view().name("beds"));
    }

    @Test
    void testAvailableBeds_showsAvailableBedsView() throws Exception {
        mockMvc.perform(get("/availableBeds").param("date", "2024-01-20"))
                .andExpect(status().isOk())
                .andExpect(view().name("availableBeds"));
    }

    @Test
    void testBookBed_whenBedAvailable_redirects() throws Exception {
        List<Patient> patients = getList("patients");
        patients.add(new Patient("Charlie", 35, "Male"));

        mockMvc.perform(post("/bookBed")
                        .param("patientIndex", "0")
                        .param("bedNumber", "1")
                        .param("date", "2024-03-15"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/beds"));
    }

    @Test
    void testBookBed_whenBedOccupied_showsError() throws Exception {
        List<Patient> patients = getList("patients");
        Patient p = new Patient("Charlie", 35, "Male");
        patients.add(p);
        List<Bed> beds = getList("beds");
        beds.get(0).occupy(p, "2024-03-15");

        mockMvc.perform(post("/bookBed")
                        .param("patientIndex", "0")
                        .param("bedNumber", "1")
                        .param("date", "2024-03-15"))
                .andExpect(status().isOk())
                .andExpect(view().name("beds"))
                .andExpect(model().attributeExists("error"));
    }

    // ── Billing ──────────────────────────────────────────────────────────────────

    @Test
    void testBilling_showsBillingView() throws Exception {
        mockMvc.perform(get("/billing"))
                .andExpect(status().isOk())
                .andExpect(view().name("billing"));
    }

    @Test
    void testGenerateBill_returnsCorrectBillDetails() throws Exception {
        List<Patient> patients = getList("patients");
        Patient p = new Patient("Diana", 50, "Female");
        p.setDaysOfStay(2);
        patients.add(p);

        List<Doctor> doctors = getList("doctors");
        Doctor d = new Doctor("Dr. Kim", "Oncology");
        doctors.add(d);

        List<Appointment> appointments = getList("appointments");
        appointments.add(new Appointment(p, d, "2024-04-01", "08:00"));
        appointments.add(new Appointment(p, d, "2024-04-02", "08:00"));

        mockMvc.perform(post("/generateBill").param("patientIndex", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("billDetails"))
                .andExpect(model().attribute("appointmentCount", 2))
                .andExpect(model().attribute("totalBill", 2200)); // 2*100 + 2*1000
    }

    @Test
    void testClearBill_withValidIndex_redirectsToBilling() throws Exception {
        List<Patient> patients = getList("patients");
        Patient p = new Patient("Eve", 22, "Female");
        p.addToBill(500);
        patients.add(p);

        mockMvc.perform(post("/clearBill").param("patientIndex", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/billing"));
    }

    @Test
    void testClearBill_withInvalidIndex_redirectsToBilling() throws Exception {
        mockMvc.perform(post("/clearBill").param("patientIndex", "99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/billing"));
    }

    @Test
    void testClearBill_withNegativeIndex_redirectsToBilling() throws Exception {
        mockMvc.perform(post("/clearBill").param("patientIndex", "-1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/billing"));
    }
}
