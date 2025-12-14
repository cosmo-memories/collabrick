package nz.ac.canterbury.seng302.homehelper.integration.service;

import nz.ac.canterbury.seng302.homehelper.entity.renovation.Budget;
import nz.ac.canterbury.seng302.homehelper.entity.renovation.Renovation;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.model.renovation.ExpenseCategory;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.BudgetRepository;
import nz.ac.canterbury.seng302.homehelper.repository.renovation.RenovationRepository;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.renovation.BudgetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class BudgetServiceIntegrationTests {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private RenovationRepository renovationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetService budgetService;

    private Renovation renovation;
    private Renovation renovation2;
    private Budget budget;
    private Budget budget2;
    private User testUser;

    @BeforeEach
    void setUp() {
        renovation = new Renovation("Bathroom Remodel", "Remodeling bathroom");
        testUser = new User("John", "Smith", "john.smith@gmail.com", "password", "password");
        userRepository.save(testUser);
        renovation.setOwner(testUser);
        renovation = renovationRepository.save(renovation);
        renovationRepository.save(renovation);
        budget = renovation.getBudget();

        renovation2 = new Renovation("Kitchen Remodel", "Remodeling kitchen");
        renovation2.setOwner(testUser);
        renovation2 = renovationRepository.save(renovation2);
        budgetService.updateBudget(renovation2.getBudget().getId(), new Budget(
                new BigDecimal("200.00"),
                new BigDecimal("300.00"),
                new BigDecimal("400.00"),
                new BigDecimal("500.00"),
                new BigDecimal("600.00"),
                new BigDecimal("700.00"),
                new BigDecimal("800.00"),
                new BigDecimal("900.00")
        ));

    }

    @Test
    void givenExistingBudgetId_whenIdIsFound_thenBudgetIsReturned() {
        Optional<Budget> found = budgetService.findById(budget.getId());
        assertTrue(found.isPresent());
        assertEquals(budget.getMiscellaneousBudget(), found.get().getMiscellaneousBudget());
        assertEquals(budget.getMaterialBudget(), found.get().getMaterialBudget());
        assertEquals(budget.getLabourBudget(), found.get().getLabourBudget());
        assertEquals(budget.getEquipmentBudget(), found.get().getEquipmentBudget());
        assertEquals(budget.getProfessionalServiceBudget(), found.get().getProfessionalServiceBudget());
        assertEquals(budget.getPermitBudget(), found.get().getPermitBudget());
        assertEquals(budget.getCleanupBudget(), found.get().getCleanupBudget());
        assertEquals(budget.getDeliveryBudget(), found.get().getDeliveryBudget());
        assertEquals(budget.getRenovation(), found.get().getRenovation());
    }

    @Test
    void givenNoBudgetId_whenNotFound_thenEmptyIsReturned() {
        long nonExistentBudgetId = 100L;
        Optional<Budget> result = budgetService.findById(nonExistentBudgetId);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenRenovationIdWithBudget_whenRenovationIdIsFound_thenBudgetIsReturned() {
        Optional<Budget> found = budgetService.findByRenovationId(renovation.getId());
        assertTrue(found.isPresent());
        assertEquals(budget.getId(), found.get().getId());
        assertEquals(budget.getMiscellaneousBudget(), found.get().getMiscellaneousBudget());
        assertEquals(budget.getMaterialBudget(), found.get().getMaterialBudget());
        assertEquals(budget.getLabourBudget(), found.get().getLabourBudget());
        assertEquals(budget.getEquipmentBudget(), found.get().getEquipmentBudget());
        assertEquals(budget.getProfessionalServiceBudget(), found.get().getProfessionalServiceBudget());
        assertEquals(budget.getPermitBudget(), found.get().getPermitBudget());
        assertEquals(budget.getCleanupBudget(), found.get().getCleanupBudget());
        assertEquals(budget.getDeliveryBudget(), found.get().getDeliveryBudget());
        assertEquals(budget.getRenovation(), found.get().getRenovation());
    }

    @Test
    void givenNoRenovationId_whenNotFound_thenEmptyIsReturned() {
        long nonExistentRenovationId = 100L;
        Optional<Budget> result = budgetService.findByRenovationId(nonExistentRenovationId);
        assertTrue(result.isEmpty());
    }

    @Test
    void givenExistingBudget_whenUpdateBudgetLabourCategory_thenLabourCategoryAmountIsUpdated() {
        BigDecimal newAmount = new BigDecimal("999.99");
        Optional<Budget> updated = budgetService.updateBudgetCategory(budget.getId(), ExpenseCategory.LABOUR, newAmount);

        assertTrue(updated.isPresent());
        assertEquals(newAmount, updated.get().getLabourBudget());

        assertEquals(budget.getId(), updated.get().getId());
        assertEquals(budget.getMiscellaneousBudget(), updated.get().getMiscellaneousBudget());
        assertEquals(budget.getMaterialBudget(), updated.get().getMaterialBudget());
        assertEquals(budget.getEquipmentBudget(), updated.get().getEquipmentBudget());
        assertEquals(budget.getProfessionalServiceBudget(), updated.get().getProfessionalServiceBudget());
        assertEquals(budget.getPermitBudget(), updated.get().getPermitBudget());
        assertEquals(budget.getCleanupBudget(), updated.get().getCleanupBudget());
        assertEquals(budget.getDeliveryBudget(), updated.get().getDeliveryBudget());
        assertEquals(budget.getRenovation(), updated.get().getRenovation());

        // Checks if the renovation has the new budget
        Renovation refreshedRenovation = renovationRepository.findById(renovation.getId()).orElseThrow();
        Budget refreshedBudget = refreshedRenovation.getBudget();

        assertEquals(BigDecimal.ZERO, refreshedBudget.getMiscellaneousBudget());
        assertEquals(BigDecimal.ZERO, refreshedBudget.getMaterialBudget());
        assertEquals(newAmount, refreshedBudget.getLabourBudget());
        assertEquals(BigDecimal.ZERO, refreshedBudget.getEquipmentBudget());
        assertEquals(BigDecimal.ZERO, refreshedBudget.getProfessionalServiceBudget());
        assertEquals(BigDecimal.ZERO, refreshedBudget.getPermitBudget());
        assertEquals(BigDecimal.ZERO, refreshedBudget.getCleanupBudget());
        assertEquals(BigDecimal.ZERO, refreshedBudget.getDeliveryBudget());
        assertEquals(renovation.getId(), refreshedBudget.getRenovation().getId());
    }

    @Test
    void givenExistingBudget_whenUpdateBudget_thenAllFieldsAreUpdated() {
        Budget updatedBudget = new Budget(
                new BigDecimal("100.00"),
                new BigDecimal("200.00"),
                new BigDecimal("300.00"),
                new BigDecimal("400.00"),
                new BigDecimal("500.00"),
                new BigDecimal("600.00"),
                new BigDecimal("700.00"),
                new BigDecimal("800.00")
        );

        updatedBudget.setRenovation(renovation);
        Optional<Budget> updated = budgetService.updateBudget(budget.getId(), updatedBudget);

        assertTrue(updated.isPresent());
        assertEquals(new BigDecimal("100.00"), updated.get().getMiscellaneousBudget());
        assertEquals(new BigDecimal("200.00"), updated.get().getMaterialBudget());
        assertEquals(new BigDecimal("300.00"), updated.get().getLabourBudget());
        assertEquals(new BigDecimal("400.00"), updated.get().getEquipmentBudget());
        assertEquals(new BigDecimal("500.00"), updated.get().getProfessionalServiceBudget());
        assertEquals(new BigDecimal("600.00"), updated.get().getPermitBudget());
        assertEquals(new BigDecimal("700.00"), updated.get().getCleanupBudget());
        assertEquals(new BigDecimal("800.00"), updated.get().getDeliveryBudget());

        // Checks if the renovation has the new budget
        Renovation refreshedRenovation = renovationRepository.findById(renovation.getId()).orElseThrow();
        Budget refreshedBudget = refreshedRenovation.getBudget();

        assertEquals(new BigDecimal("100.00"), refreshedBudget.getMiscellaneousBudget());
        assertEquals(new BigDecimal("200.00"), refreshedBudget.getMaterialBudget());
        assertEquals(new BigDecimal("300.00"), refreshedBudget.getLabourBudget());
        assertEquals(new BigDecimal("400.00"), refreshedBudget.getEquipmentBudget());
        assertEquals(new BigDecimal("500.00"), refreshedBudget.getProfessionalServiceBudget());
        assertEquals(new BigDecimal("600.00"), refreshedBudget.getPermitBudget());
        assertEquals(new BigDecimal("700.00"), refreshedBudget.getCleanupBudget());
        assertEquals(new BigDecimal("800.00"), refreshedBudget.getDeliveryBudget());
        assertEquals(renovation.getId(), refreshedBudget.getRenovation().getId());
    }

    @Test
    void givenNonExistentBudgetId_whenUpdateBudgetCategory_thenEmptyOptionalIsReturned() {
        Optional<Budget> updated = budgetService.updateBudgetCategory(100L, ExpenseCategory.MATERIAL, BigDecimal.TEN);
        assertFalse(updated.isPresent());
    }

    @Test
    void givenNonExistentBudgetId_whenUpdateBudget_thenEmptyOptionalIsReturned() {
        Budget zeroBudget = new Budget(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        Optional<Budget> updated = budgetService.updateBudget(100L, zeroBudget);
        assertFalse(updated.isPresent());
    }

    @Test
    void givenRenovation_whenGetBudget_thenBudgetIsReturnedAndLinkedCorrectly() {
        Budget renovation2Budget = renovation2.getBudget();

        assertNotNull(renovation2Budget);
        assertEquals(renovation2.getId(), renovation2Budget.getRenovation().getId());

        assertEquals(new BigDecimal("200.00"), renovation2Budget.getMiscellaneousBudget());
        assertEquals(new BigDecimal("300.00"), renovation2Budget.getMaterialBudget());
        assertEquals(new BigDecimal("400.00"), renovation2Budget.getLabourBudget());
        assertEquals(new BigDecimal("500.00"), renovation2Budget.getEquipmentBudget());
        assertEquals(new BigDecimal("600.00"), renovation2Budget.getProfessionalServiceBudget());
        assertEquals(new BigDecimal("700.00"), renovation2Budget.getPermitBudget());
        assertEquals(new BigDecimal("800.00"), renovation2Budget.getCleanupBudget());
        assertEquals(new BigDecimal("900.00"), renovation2Budget.getDeliveryBudget());
    }
}
