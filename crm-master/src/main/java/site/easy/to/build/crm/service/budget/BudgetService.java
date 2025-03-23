package site.easy.to.build.crm.service.budget;

import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.Customer;

import java.util.List;

public interface BudgetService {
    Budget findByBudgetId(int id);
    List<Budget> findAll();
    List<Budget> getCustomerBudgets(int customerId);
    Budget save(Budget budget);
    void delete(Budget budget);
    void deleteAllByCustomer(Customer customer);
}
