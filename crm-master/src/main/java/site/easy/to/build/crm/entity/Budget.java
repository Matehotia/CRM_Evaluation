package site.easy.to.build.crm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import site.easy.to.build.crm.customValidations.FutureDate;
import site.easy.to.build.crm.customValidations.contract.StartDateBeforeEndDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_budget")
@StartDateBeforeEndDate
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_id")
    private int budgetId;

    @NotBlank(message = "Budget name is required")
    @Column(name = "budget_name")
    private String budgetName;

    @NotNull(message = "Amount is required")
    @Digits(integer = 10, fraction = 2, message = "Amount must be a valid number with up to 2 decimal places")
    @DecimalMin(value = "0.00", inclusive = true, message = "Amount must be ≥ 0.00")
    @DecimalMax(value = "9999999.99", inclusive = true, message = "Amount must be ≤ 9999999.99")
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @NotBlank(message = "Start Date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date format must be yyyy-MM-dd")
    @FutureDate
    @Column(name = "start_date", nullable = false)
    private String startDate;

    @NotBlank(message = "End Date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date format must be yyyy-MM-dd")
    @FutureDate
    @Column(name = "end_date", nullable = false)
    private String endDate;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Budget() {
    }

    public Budget(String budgetName, BigDecimal amount, String startDate, String endDate, Customer customer, LocalDateTime createdAt) {
        this.budgetName = budgetName;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.customer = customer;
        this.createdAt = createdAt;
    }

    public int getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(int budgetId) {
        this.budgetId = budgetId;
    }

    public String getBudgetName() {
        return budgetName;
    }

    public void setBudgetName(String budgetName) {
        this.budgetName = budgetName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
