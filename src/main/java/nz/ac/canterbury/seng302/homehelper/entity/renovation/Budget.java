package nz.ac.canterbury.seng302.homehelper.entity.renovation;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Represents a budget for a renovation project.
 * Stores allocated amounts for various categories..
 */
@Entity
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_id")
    private long id;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "renovation_id", nullable = false)
    private Renovation renovation;

    @Column
    private BigDecimal miscellaneousBudget;

    @Column
    private BigDecimal materialBudget;

    @Column
    private BigDecimal labourBudget;

    @Column
    private BigDecimal equipmentBudget;

    @Column
    private BigDecimal professionalServiceBudget;

    @Column
    private BigDecimal permitBudget;

    @Column
    private BigDecimal cleanupBudget;

    @Column
    private BigDecimal deliveryBudget;

    /**
     * JPA constructor
     */
    protected Budget() {

    }

    /**
     * Creates a new Budget instance.
     *
     * @param MiscellaneousBudget       the budget for miscellaneous costs
     * @param MaterialBudget            the budget for materials
     * @param LabourBudget              the budget for labour
     * @param EquipmentBudget           the budget for equipment
     * @param ProfessionalServiceBudget the budget for professional services
     * @param PermitBudget              the budget for permits
     * @param CleanupBudget             the budget for cleanup
     * @param DeliveryBudget            the budget for delivery
     */
    public Budget(BigDecimal MiscellaneousBudget, BigDecimal MaterialBudget,
                  BigDecimal LabourBudget, BigDecimal EquipmentBudget, BigDecimal ProfessionalServiceBudget,
                  BigDecimal PermitBudget, BigDecimal CleanupBudget, BigDecimal DeliveryBudget) {
        this.miscellaneousBudget = MiscellaneousBudget;
        this.materialBudget = MaterialBudget;
        this.labourBudget = LabourBudget;
        this.equipmentBudget = EquipmentBudget;
        this.professionalServiceBudget = ProfessionalServiceBudget;
        this.permitBudget = PermitBudget;
        this.cleanupBudget = CleanupBudget;
        this.deliveryBudget = DeliveryBudget;
    }

    /**
     * Gets the unique identifier of this budget.
     *
     * @return the budget ID
     */
    public long getId() {
        return id;
    }

    /**
     * Gets the renovation associated with this budget.
     *
     * @return the associated renovation
     */
    public Renovation getRenovation() {
        return renovation;
    }

    /**
     * Sets the renovation associated with this budget.
     *
     * @param renovation the renovation to associate
     */
    public void setRenovation(Renovation renovation) {
        this.renovation = renovation;
    }

    /**
     * Gets the miscellaneous budget allocation.
     *
     * @return the miscellaneous budget
     */
    public BigDecimal getMiscellaneousBudget() {
        return miscellaneousBudget;
    }

    /**
     * Sets the miscellaneous budget allocation.
     *
     * @param miscellaneousBudget the miscellaneous budget to set
     */
    public void setMiscellaneousBudget(BigDecimal miscellaneousBudget) {
        this.miscellaneousBudget = miscellaneousBudget;
    }

    /**
     * Gets the material budget allocation.
     *
     * @return the material budget
     */
    public BigDecimal getMaterialBudget() {
        return materialBudget;
    }

    /**
     * Sets the material budget allocation.
     *
     * @param materialBudget the material budget to set
     */
    public void setMaterialBudget(BigDecimal materialBudget) {
        this.materialBudget = materialBudget;
    }

    /**
     * Gets the labour budget allocation.
     *
     * @return the labour budget
     */
    public BigDecimal getLabourBudget() {
        return labourBudget;
    }

    /**
     * Sets the labour budget allocation.
     *
     * @param labourBudget the labour budget to set
     */
    public void setLabourBudget(BigDecimal labourBudget) {
        this.labourBudget = labourBudget;
    }

    /**
     * Gets the equipment budget allocation.
     *
     * @return the equipment budget
     */
    public BigDecimal getEquipmentBudget() {
        return equipmentBudget;
    }

    /**
     * Sets the equipment budget allocation.
     *
     * @param equipmentBudget the equipment budget to set
     */
    public void setEquipmentBudget(BigDecimal equipmentBudget) {
        this.equipmentBudget = equipmentBudget;
    }

    /**
     * Gets the professional service budget allocation.
     *
     * @return the professional service budget
     */
    public BigDecimal getProfessionalServiceBudget() {
        return professionalServiceBudget;
    }

    /**
     * Sets the professional service budget allocation.
     *
     * @param professionalServiceBudget the professional service budget to set
     */
    public void setProfessionalServiceBudget(BigDecimal professionalServiceBudget) {
        this.professionalServiceBudget = professionalServiceBudget;
    }

    /**
     * Gets the permit budget allocation.
     *
     * @return the permit budget
     */
    public BigDecimal getPermitBudget() {
        return permitBudget;
    }

    /**
     * Sets the permit budget allocation.
     *
     * @param permitBudget the permit budget to set
     */
    public void setPermitBudget(BigDecimal permitBudget) {
        this.permitBudget = permitBudget;
    }

    /**
     * Gets the cleanup budget allocation.
     *
     * @return the cleanup budget
     */
    public BigDecimal getCleanupBudget() {
        return cleanupBudget;
    }

    /**
     * Sets the cleanup budget allocation.
     *
     * @param cleanupBudget the cleanup budget to set
     */
    public void setCleanupBudget(BigDecimal cleanupBudget) {
        this.cleanupBudget = cleanupBudget;
    }

    /**
     * Gets the delivery budget allocation.
     *
     * @return the delivery budget
     */
    public BigDecimal getDeliveryBudget() {
        return deliveryBudget;
    }

    /**
     * Sets the delivery budget allocation.
     *
     * @param deliveryBudget the delivery budget to set
     */
    public void setDeliveryBudget(BigDecimal deliveryBudget) {
        this.deliveryBudget = deliveryBudget;
    }

    /**
     * Gets the combined sum of all the budget categories of this budget
     *
     * @return the sum of the budget categories in BigDecimal
     */
    public BigDecimal getBudgetSum() {
        BigDecimal sum = BigDecimal.ZERO;
        return sum.add(getMiscellaneousBudget()).add(getMaterialBudget()).add(getLabourBudget())
                .add(getEquipmentBudget()).add(getProfessionalServiceBudget()).add(getPermitBudget())
                .add(getCleanupBudget()).add(getDeliveryBudget());
    }
}
