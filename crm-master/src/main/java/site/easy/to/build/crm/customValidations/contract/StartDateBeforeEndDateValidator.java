package site.easy.to.build.crm.customValidations.contract;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import site.easy.to.build.crm.entity.Contract;
import site.easy.to.build.crm.entity.Budget;

import java.time.LocalDate;

public class StartDateBeforeEndDateValidator implements ConstraintValidator<StartDateBeforeEndDate, Object> {

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        String start = null;
        String end = null;

        if (obj instanceof Contract contract) {
            start = contract.getStartDate();
            end = contract.getEndDate();
        } else if (obj instanceof Budget budget) {
            start = budget.getStartDate();
            end = budget.getEndDate();
        }

        if (start == null || end == null || start.isEmpty() || end.isEmpty()) {
            return true;
        }

        try {
            LocalDate startDate = LocalDate.parse(start);
            LocalDate endDate = LocalDate.parse(end);
            return startDate.isBefore(endDate);
        } catch (Exception e) {
            return false;
        }
    }
}
