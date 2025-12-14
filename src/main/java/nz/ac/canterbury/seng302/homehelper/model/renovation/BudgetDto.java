package nz.ac.canterbury.seng302.homehelper.model.renovation;

/**
 * Data Transfer Object representing the different budget categories that can be updated.
 *
 * @param miscellaneousBudget       Budget for miscellaneous expenses.
 * @param materialBudget            Budget for materials.
 * @param labourBudget              Budget for labour costs.
 * @param equipmentBudget           Budget for equipment expenses.
 * @param professionalServiceBudget Budget for professional services.
 * @param permitBudget              Budget for permits.
 * @param cleanupBudget             Budget for cleanup costs.
 * @param deliveryBudget            Budget for delivery costs.
 */
public record BudgetDto(
        String miscellaneousBudget,
        String materialBudget,
        String labourBudget,
        String equipmentBudget,
        String professionalServiceBudget,
        String permitBudget,
        String cleanupBudget,
        String deliveryBudget
) {

    /**
     * Builder for creating instances of BudgetDto.
     */
    public static class Builder {
        private String miscellaneousBudget = "0";
        private String materialBudget = "0";
        private String labourBudget = "0";
        private String equipmentBudget = "0";
        private String professionalServiceBudget = "0";
        private String permitBudget = "0";
        private String cleanupBudget = "0";
        private String deliveryBudget = "0";

        /**
         * Sets the miscellaneous budget.
         *
         * @param miscellaneousBudget budget value as a string
         * @return this builder instance
         */
        public Builder withMiscellaneousBudget(String miscellaneousBudget) {
            this.miscellaneousBudget = miscellaneousBudget;
            return this;
        }

        /**
         * Sets the material budget.
         *
         * @param materialBudget budget value as a string
         * @return this builder instance
         */
        public Builder withMaterialBudget(String materialBudget) {
            this.materialBudget = materialBudget;
            return this;
        }

        /**
         * Sets the labour budget.
         *
         * @param labourBudget budget value as a string
         * @return this builder instance
         */
        public Builder withLabourBudget(String labourBudget) {
            this.labourBudget = labourBudget;
            return this;
        }

        /**
         * Sets the equipment budget.
         *
         * @param equipmentBudget budget value as a string
         * @return this builder instance
         */
        public Builder withEquipmentBudget(String equipmentBudget) {
            this.equipmentBudget = equipmentBudget;
            return this;
        }

        /**
         * Sets the professional service budget.
         *
         * @param professionalServiceBudget budget value as a string
         * @return this builder instance
         */
        public Builder withProfessionalServiceBudget(String professionalServiceBudget) {
            this.professionalServiceBudget = professionalServiceBudget;
            return this;
        }

        /**
         * Sets the permit budget.
         *
         * @param permitBudget budget value as a string
         * @return this builder instance
         */
        public Builder withPermitBudget(String permitBudget) {
            this.permitBudget = permitBudget;
            return this;
        }

        /**
         * Sets the cleanup budget.
         *
         * @param cleanupBudget budget value as a string
         * @return this builder instance
         */
        public Builder withCleanupBudget(String cleanupBudget) {
            this.cleanupBudget = cleanupBudget;
            return this;
        }

        /**
         * Sets the delivery budget.
         *
         * @param deliveryBudget budget value as a string
         * @return this builder instance
         */
        public Builder withDeliveryBudget(String deliveryBudget) {
            this.deliveryBudget = deliveryBudget;
            return this;
        }

        /**
         * Builds a new BudgetDto instance using the current builder state.
         *
         * @return a new BudgetDto containing the set budget values
         */
        public BudgetDto build() {
            return new BudgetDto(
                    miscellaneousBudget,
                    materialBudget,
                    labourBudget,
                    equipmentBudget,
                    professionalServiceBudget,
                    permitBudget,
                    cleanupBudget,
                    deliveryBudget
            );
        }
    }
}
