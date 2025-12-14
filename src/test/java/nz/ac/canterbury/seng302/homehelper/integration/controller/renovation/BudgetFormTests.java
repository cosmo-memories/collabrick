package nz.ac.canterbury.seng302.homehelper.integration.controller.renovation;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Budget;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.BudgetRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.ExpenseRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.renovation.BudgetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
@ExtendWith(MockitoExtension.class)
@SpringBootTest
//@Rollback
public class BudgetFormTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private RenovationRepository renovationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetService budgetService;

    private Renovation renovation;
    private User user;
    @Autowired
    private ExpenseRepository expenseRepository;

    @BeforeEach
    void setUp() {
        budgetRepository.deleteAll();
        renovationRepository.deleteAll();
        userRepository.deleteAll();

        renovation = new Renovation("Test Name", "Test Description");
        user = new User("Test", "User", "test@email.com", "password", "password");
        userRepository.save(user);
        renovation.setOwner(user);
        renovation = renovationRepository.save(renovation);
        renovationRepository.save(renovation);
    }

    @Test
    void checkRenovationHasBudget() {
        budgetService.updateBudget(renovation.getBudget().getId(), new Budget(
                new BigDecimal("200.00"),
                new BigDecimal("300.00"),
                new BigDecimal("400.00"),
                new BigDecimal("500.00"),
                new BigDecimal("600.00"),
                new BigDecimal("700.00"),
                new BigDecimal("800.00"),
                new BigDecimal("900.00")
        ));
        assertEquals(new BigDecimal("4400.00"), renovation.getBudget().getBudgetSum());
    }

    @Test
    void viewBudgetForm_NoExpensesExist_AutofilledWithZeroes() throws Exception {
        MvcResult result = mockMvc.perform(get("/renovation/{id}/editBudget", renovation.getId())
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();

        // Regex by ChatGPT
        assertTrue(content.matches("(?s).*<input(?=[^>]*\\bname=\"miscellaneousBudget\")(?=[^>]*\\bvalue=\"0\")[^>]*>.*"));
        assertTrue(content.matches("(?s).*<input(?=[^>]*\\bname=\"materialBudget\")(?=[^>]*\\bvalue=\"0\")[^>]*>.*"));
        assertTrue(content.matches("(?s).*<input(?=[^>]*\\bname=\"labourBudget\")(?=[^>]*\\bvalue=\"0\")[^>]*>.*"));
        assertTrue(content.matches("(?s).*<input(?=[^>]*\\bname=\"equipmentBudget\")(?=[^>]*\\bvalue=\"0\")[^>]*>.*"));
        assertTrue(content.matches("(?s).*<input(?=[^>]*\\bname=\"professionalServiceBudget\")(?=[^>]*\\bvalue=\"0\")[^>]*>.*"));
        assertTrue(content.matches("(?s).*<input(?=[^>]*\\bname=\"permitBudget\")(?=[^>]*\\bvalue=\"0\")[^>]*>.*"));
        assertTrue(content.matches("(?s).*<input(?=[^>]*\\bname=\"cleanupBudget\")(?=[^>]*\\bvalue=\"0\")[^>]*>.*"));
        assertTrue(content.matches("(?s).*<input(?=[^>]*\\bname=\"deliveryBudget\")(?=[^>]*\\bvalue=\"0\")[^>]*>.*"));
    }

    @Test
    void viewBudgetForm_SomeExpensesExist_AutofilledWithCorrectValues() throws Exception {
        budgetService.updateBudget(renovation.getBudget().getId(), new Budget(
                new BigDecimal("200.00"),
                BigDecimal.ZERO,
                new BigDecimal("400.00"),
                new BigDecimal("500.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("900.00")
        ));

        MvcResult result = mockMvc.perform(get("/renovation/{id}/editBudget", renovation.getId())
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();

        assertTrue(content.matches("(?s).*<input(?=[^>]*\\bname=\"miscellaneousBudget\")(?=[^>]*\\bvalue=\"200.00\")[^>]*>.*"));
        assertTrue(content.matches("(?s).*<input(?=[^>]*\\bname=\"materialBudget\")(?=[^>]*\\bvalue=\"0\")[^>]*>.*"));
        assertTrue(content.matches("(?s).*<input(?=[^>]*\\bname=\"labourBudget\")(?=[^>]*\\bvalue=\"400.00\")[^>]*>.*"));
        assertTrue(content.matches("(?s).*<input(?=[^>]*\\bname=\"equipmentBudget\")(?=[^>]*\\bvalue=\"500.00\")[^>]*>.*"));
        assertTrue(content.matches("(?s).*<input(?=[^>]*\\bname=\"professionalServiceBudget\")(?=[^>]*\\bvalue=\"0\")[^>]*>.*"));
        assertTrue(content.matches("(?s).*<input(?=[^>]*\\bname=\"permitBudget\")(?=[^>]*\\bvalue=\"0\")[^>]*>.*"));
        assertTrue(content.matches("(?s).*<input(?=[^>]*\\bname=\"cleanupBudget\")(?=[^>]*\\bvalue=\"0\")[^>]*>.*"));
        assertTrue(content.matches("(?s).*<input(?=[^>]*\\bname=\"deliveryBudget\")(?=[^>]*\\bvalue=\"900.00\")[^>]*>.*"));
    }

    @Test
    void viewBudgetForm_AllExpensesExist_AutofilledWithCorrectValues() throws Exception {
        budgetService.updateBudget(renovation.getBudget().getId(), new Budget(
                new BigDecimal("200.00"),
                new BigDecimal("300.00"),
                new BigDecimal("400.00"),
                new BigDecimal("500.00"),
                new BigDecimal("600.00"),
                new BigDecimal("700.00"),
                new BigDecimal("800.00"),
                new BigDecimal("900.00")
        ));

        MvcResult result = mockMvc.perform(get("/renovation/{id}/editBudget", renovation.getId())
                        .with(csrf())
                        .with(user(String.valueOf(user.getId())).password(user.getPassword()).roles("USER")))
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();

        assertTrue(content.matches("(?s).*<input(?=[^>]*\\bname=\"miscellaneousBudget\")(?=[^>]*\\bvalue=\"200.00\")[^>]*>.*"));
        assertTrue(content.matches("(?s).*<input(?=[^>]*\\bname=\"materialBudget\")(?=[^>]*\\bvalue=\"300.00\")[^>]*>.*"));
        assertTrue(content.matches("(?s).*<input(?=[^>]*\\bname=\"labourBudget\")(?=[^>]*\\bvalue=\"400.00\")[^>]*>.*"));
        assertTrue(content.matches("(?s).*<input(?=[^>]*\\bname=\"equipmentBudget\")(?=[^>]*\\bvalue=\"500.00\")[^>]*>.*"));
        assertTrue(content.matches("(?s).*<input(?=[^>]*\\bname=\"professionalServiceBudget\")(?=[^>]*\\bvalue=\"600.00\")[^>]*>.*"));
        assertTrue(content.matches("(?s).*<input(?=[^>]*\\bname=\"permitBudget\")(?=[^>]*\\bvalue=\"700.00\")[^>]*>.*"));
        assertTrue(content.matches("(?s).*<input(?=[^>]*\\bname=\"cleanupBudget\")(?=[^>]*\\bvalue=\"800.00\")[^>]*>.*"));
        assertTrue(content.matches("(?s).*<input(?=[^>]*\\bname=\"deliveryBudget\")(?=[^>]*\\bvalue=\"900.00\")[^>]*>.*"));
    }


}
