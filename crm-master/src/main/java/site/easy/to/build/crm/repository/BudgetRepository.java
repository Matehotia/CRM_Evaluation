package site.easy.to.build.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.Customer;

import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Integer> {
    Budget findByBudgetId(int budgetId);

    List<Budget> findByCustomerCustomerId(int customerId);

    void deleteAllByCustomer(Customer customer);
}
