package site.easy.to.build.crm.service.budget; 


import java.util.List;

import org.springframework.stereotype.Service;

import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.repository.BudgetRepository;

@Service
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;

    public BudgetServiceImpl(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    @Override
    public Budget findByBudgetId(int id) {
        return budgetRepository.findByBudgetId(id);
    }

    @Override
    public List<Budget> findAll() {
        return budgetRepository.findAll();
    }

    @Override
    public List<Budget> getCustomerBudgets(int customerId) {
        List<Budget> budgets = budgetRepository.findByCustomerCustomerId(customerId);
        System.out.println("Budgets trouvés pour le client avec ID: " + customerId + " - Nombre de budgets: " + budgets.size());
        return budgets;
    }

    @Override
    public Budget save(Budget budget) {
        return budgetRepository.save(budget);
    }

    @Override
    public void delete(Budget budget) {
        budgetRepository.delete(budget);
    }

    @Override
    public void deleteAllByCustomer(Customer customer) {
        budgetRepository.deleteAllByCustomer(customer);
    }

}
