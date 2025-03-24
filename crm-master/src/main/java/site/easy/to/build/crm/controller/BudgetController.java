package site.easy.to.build.crm.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.service.budget.BudgetService;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.service.user.UserService;
import site.easy.to.build.crm.util.AuthenticationUtils;
import site.easy.to.build.crm.util.AuthorizationUtil;

@Controller
@RequestMapping("/employee/budget")
public class BudgetController {

    private final BudgetService budgetService;
    private final CustomerService customerService;
    private final AuthenticationUtils authenticationUtils;
    private final UserService userService;

    public BudgetController(BudgetService budgetService, CustomerService customerService,
                            AuthenticationUtils authenticationUtils, UserService userService) {
        this.budgetService = budgetService;
        this.customerService = customerService;
        this.authenticationUtils = authenticationUtils;
        this.userService = userService;
    }

    @GetMapping("/create-budget")
    public String showCreateForm(Model model, Authentication authentication) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User user = userService.findById(userId);
        if (user.isInactiveUser()) {
            return "error/account-inactive";
        }

        List<Customer> customers = AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER")
                ? customerService.findAll()
                : customerService.findByUserId(userId);

        model.addAttribute("budget", new Budget());
        model.addAttribute("customers", customers);
        return "budget/create-budget";
    }

    @PostMapping("/create-budget")
    public String createBudget(@ModelAttribute("budget") @Valid Budget budget, BindingResult result,
                               @RequestParam("customerId") int customerId,
                               Authentication authentication, Model model) {
        if (result.hasErrors()) {
            int userId = authenticationUtils.getLoggedInUserId(authentication);
            List<Customer> customers = AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER")
                    ? customerService.findAll()
                    : customerService.findByUserId(userId);
            model.addAttribute("customers", customers);
            return "budget/create-budget";
        }
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User employee = userService.findById(userId);
        if (employee.isInactiveUser()) {
            return "error/account-inactive";
        }

        Customer customer = customerService.findByCustomerId(customerId);
        if (customer == null || (customer.getUser().getId() != userId && !AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER"))) {
            return "error/access-denied";
        }

        budget.setCustomer(customer);
        budget.setCreatedAt(LocalDateTime.now());

        budgetService.save(budget);
        return "budget/create-budget";
    }


    @GetMapping("/manager/all-budgets")
    public String listAllBudgets(Model model) {
        List<Budget> budgets = budgetService.findAll();
        List<Customer> customers = customerService.findAll();
        model.addAttribute("budgets", budgets);
        model.addAttribute("customers", customers);
        return "budget/list-budgets";
    }

}
