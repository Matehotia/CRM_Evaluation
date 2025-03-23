package site.easy.to.build.crm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ExceptionHandler;

import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.entity.UserProfile;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.CustomerLoginInfo;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.Contract;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.entity.File;
import site.easy.to.build.crm.repository.UserRepository;
import site.easy.to.build.crm.repository.UserProfileRepository;
import site.easy.to.build.crm.repository.CustomerRepository;
import site.easy.to.build.crm.repository.CustomerLoginInfoRepository;
import site.easy.to.build.crm.repository.LeadRepository;
import site.easy.to.build.crm.repository.ContractRepository;
import site.easy.to.build.crm.repository.TicketRepository;
import site.easy.to.build.crm.repository.FileRepository;
import site.easy.to.build.crm.repository.RoleRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.math.BigDecimal;
import java.util.Map;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.function.Consumer;

@Controller
@RequestMapping("/employee/csv")
public class CsvImportController {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final CustomerRepository customerRepository;
    private final CustomerLoginInfoRepository customerLoginInfoRepository;
    private final LeadRepository leadRepository;
    private final ContractRepository contractRepository;
    private final TicketRepository ticketRepository;
    private final FileRepository fileRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public CsvImportController(UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            CustomerRepository customerRepository,
            CustomerLoginInfoRepository customerLoginInfoRepository,
            LeadRepository leadRepository,
            ContractRepository contractRepository,
            TicketRepository ticketRepository,
            FileRepository fileRepository,
            RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.customerRepository = customerRepository;
        this.customerLoginInfoRepository = customerLoginInfoRepository;
        this.leadRepository = leadRepository;
        this.contractRepository = contractRepository;
        this.ticketRepository = ticketRepository;
        this.fileRepository = fileRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/importCsv")
    public String showImportPage(Model model) {
        model.addAttribute("types", Arrays.asList("users", "customers", "leads", "contracts", "tickets"));
        model.addAttribute("title", "Import CSV");
        return "csv/import";
    }

    @GetMapping("/import")
    public String redirectToImportCsv() {
        return "redirect:/employee/csv/importCsv";
    }

    @PostMapping("/importCsv")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
            @RequestParam("type") String type,
            RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Veuillez sélectionner un fichier");
            return "redirect:/employee/csv/importCsv";
        }

        int importCount = 0;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            // Skip header
            br.readLine();
            
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;  // Ignorer les lignes vides
                
                try {
                    String[] data = parseCsvLine(line);
                    switch (type) {
                        case "users":
                            processUserData(data);
                            importCount++;
                            break;
                        case "customers":
                            processCustomerData(data);
                            importCount++;
                            break;
                        case "leads":
                            processLeadData(data);
                            importCount++;
                            break;
                        case "contracts":
                            processContractData(data);
                            importCount++;
                            break;
                        case "tickets":
                            processTicketData(data);
                            importCount++;
                            break;
                    }
                } catch (Exception e) {
                    String errorMessage = String.format("Erreur à la ligne %d: %s\nContenu de la ligne: %s", 
                        (importCount + 2), e.getMessage(), line);
                    redirectAttributes.addFlashAttribute("error", errorMessage);
                    return "redirect:/employee/csv/importCsv";
                }
            }
            
            if (importCount == 0) {
                redirectAttributes.addFlashAttribute("warning", 
                    "Aucune donnée n'a été importée du fichier " + file.getOriginalFilename());
            } else {
                redirectAttributes.addFlashAttribute("success", 
                    "Import réussi : " + importCount + " éléments importés du fichier " + file.getOriginalFilename());
            }
            
        } catch (Exception e) {
            e.printStackTrace(); // Pour le debugging
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de l'import: " + e.getMessage());
        }
        
        return "redirect:/employee/csv/importCsv";
    }

    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentValue = new StringBuilder();
        
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(currentValue.toString().trim());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }
        result.add(currentValue.toString().trim());
        
        return result.toArray(new String[0]);
    }

    private void processUserData(String[] data) {
        if (data.length < 3) {
            throw new IllegalArgumentException("Format incorrect : l'utilisateur nécessite au moins 3 colonnes");
        }

        String username = data[0].trim();
        String email = data[1].trim();
        String password = data[2].trim(); // Idéalement, encodez le mot de passe

        // Validation de base
        if (username.isEmpty() || email.isEmpty()) {
            throw new IllegalArgumentException("Le nom d'utilisateur et l'email sont obligatoires");
        }

        // Vérifier si l'utilisateur existe déjà
        List<User> existingUsers = userRepository.findByUsername(username);
        User user;

        if (!existingUsers.isEmpty()) {
            user = existingUsers.get(0);
        } else {
            user = new User();
        }

        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password); // Dans un cas réel, encodez le mot de passe

        if (data.length > 3) {
            try {
                LocalDate hireDate = LocalDate.parse(data[3].trim(),
                        DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                user.setHireDate(hireDate);
            } catch (DateTimeParseException e) {
                // Ignorer si le format de date est incorrect
            }
        }

        if (data.length > 4) {
            user.setStatus(data[4].trim());
        }

        // Sauvegarde de l'utilisateur
        user = userRepository.save(user);

        // Si nous avons des informations de profil, créons-le
        if (data.length > 5) {
            UserProfile profile = userProfileRepository.findByUserId(user.getId());
            if (profile == null) {
                profile = new UserProfile();
                profile.setUser(user);
            }

            if (data.length > 5 && !data[5].trim().isEmpty())
                profile.setFirstName(data[5].trim());
            if (data.length > 6 && !data[6].trim().isEmpty())
                profile.setLastName(data[6].trim());
            if (data.length > 7 && !data[7].trim().isEmpty())
                profile.setPhone(data[7].trim());
            if (data.length > 8 && !data[8].trim().isEmpty())
                profile.setDepartment(data[8].trim());

            userProfileRepository.save(profile);
        }
    }

    private void processCustomerData(String[] data) {
        if (data.length < 3) {
            throw new IllegalArgumentException("Format incorrect : le client nécessite au moins email, nom et téléphone");
        }

        try {
            System.out.println("\n=== Traitement du client ===");
            
            String email = data[13].trim(); // L'email est à l'index 13
            String name = data[1].trim().replaceAll("^\"|\"$", "");
            String phone = data[2].trim().replaceAll("^\"|\"$", "");

            System.out.println("Email: " + email);
            System.out.println("Nom: " + name);
            System.out.println("Téléphone: " + phone);

            // Vérification du format email
            if (!isValidEmail(email)) {
                throw new IllegalArgumentException(
                    String.format("Format d'email invalide : '%s'. Format attendu: exemple@gmail.com", email));
            }

            // Vérifier si le client existe déjà
            Customer existingCustomer = customerRepository.findByEmail(email);
            if (existingCustomer != null) {
                throw new IllegalArgumentException("Un client avec cet email existe déjà : " + email);
            }

            Customer customer = new Customer();
            customer.setEmail(email);
            customer.setName(name);
            customer.setPhone(phone);
            User user = new User();
            user.setId(52);
            customer.setUser(user);

            // Ajout des champs optionnels
            if (!data[3].trim().isEmpty()) customer.setAddress(data[3].trim().replaceAll("^\"|\"$", ""));
            if (!data[6].trim().isEmpty()) customer.setCountry(data[6].trim().replaceAll("^\"|\"$", ""));
            if (!data[9].trim().isEmpty()) customer.setPosition(data[9].trim().replaceAll("^\"|\"$", ""));
            if (!data[8].trim().isEmpty()) customer.setDescription(data[8].trim().replaceAll("^\"|\"$", ""));
            if (!data[4].trim().isEmpty()) customer.setCity(data[4].trim().replaceAll("^\"|\"$", ""));
            if (!data[5].trim().isEmpty()) customer.setState(data[5].trim().replaceAll("^\"|\"$", ""));

            Customer savedCustomer = customerRepository.save(customer);
            System.out.println("Client sauvegardé: " + savedCustomer.getEmail());

        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Format de données incorrect. Vérifiez le nombre de colonnes dans votre CSV.");
        } catch (Exception e) {
            throw new IllegalArgumentException("Erreur lors du traitement du client: " + e.getMessage());
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@gmail\\.com$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    private void processLeadData(String[] data) {
        if (data.length < 5) {
            throw new IllegalArgumentException("Format incorrect : le lead nécessite au moins 5 colonnes");
        }

        String customerEmail = data[0].trim();
        String name = data[1].trim();
        String phone = data[2].trim();
        String status = data[3].trim();
        String employeeUsername = data[4].trim();

        // Trouver le client associé
        Customer customer = customerRepository.findByEmail(customerEmail);
        if (customer == null) {
            throw new IllegalArgumentException("Client non trouvé: " + customerEmail);
        }

        // Trouver l'employé assigné
        List<User> employees = userRepository.findByUsername(employeeUsername);
        if (employees.isEmpty()) {
            throw new IllegalArgumentException("Employé non trouvé: " + employeeUsername);
        }
        User employee = employees.get(0);

        // Créer ou mettre à jour le lead
        Lead lead = new Lead();
        lead.setCustomer(customer);
        lead.setName(name);
        lead.setPhone(phone);
        lead.setStatus(status);
        lead.setEmployee(employee);
        lead.setCreatedAt(LocalDateTime.now());

        leadRepository.save(lead);
    }

    private void processContractData(String[] data) {
        if (data.length < 7) {
            throw new IllegalArgumentException("Format incorrect : le contrat nécessite au moins 7 colonnes");
        }

        String customerEmail = data[0].trim();
        String subject = data[1].trim();
        String description = data[2].trim();
        String status = data[3].trim();

        String startDateStr;
        String endDateStr;
        BigDecimal amount = null;

        try {
            LocalDate startDate = LocalDate.parse(data[4].trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            LocalDate endDate = LocalDate.parse(data[5].trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            startDateStr = startDate.toString();
            endDateStr = endDate.toString();
            amount = new BigDecimal(data[6].trim().replace(",", "."));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Format de date invalide. Format attendu: dd/MM/yyyy");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Format de montant invalide");
        }

        // Trouver le client associé
        Customer customer = customerRepository.findByEmail(customerEmail);
        if (customer == null) {
            throw new IllegalArgumentException("Client non trouvé: " + customerEmail);
        }

        // Créer un nouveau contrat
        Contract contract = new Contract();
        contract.setCustomer(customer);
        contract.setSubject(subject);
        contract.setDescription(description);
        contract.setStatus(status);
        contract.setStartDate(startDateStr);
        contract.setEndDate(endDateStr);
        contract.setAmount(amount);
        contract.setCreatedAt(LocalDateTime.now());

        contractRepository.save(contract);
    }

    private void processTicketData(String[] data) {
        if (data.length < 5) {
            throw new IllegalArgumentException("Format incorrect : le ticket nécessite au moins 5 colonnes");
        }

        String customerEmail = data[0].trim();
        String subject = data[1].trim();
        String description = data[2].trim();
        String status = data[3].trim();
        String priority = data[4].trim();

        // Trouver le client associé
        Customer customer = customerRepository.findByEmail(customerEmail);
        if (customer == null) {
            throw new IllegalArgumentException("Client non trouvé: " + customerEmail);
        }

        // Créer un nouveau ticket
        Ticket ticket = new Ticket();
        ticket.setCustomer(customer);
        ticket.setSubject(subject);
        ticket.setDescription(description);
        ticket.setStatus(status);
        ticket.setPriority(priority);
        ticket.setCreatedAt(LocalDateTime.now());

        // Si un manager est spécifié (colonne 6)
        if (data.length > 5 && !data[5].trim().isEmpty()) {
            String managerUsername = data[5].trim();
            User manager = userRepository.findByEmail(managerUsername);
            if (manager != null) {
                ticket.setManager(manager);
            }
        }

        // Si un employé est spécifié (colonne 7)
        if (data.length > 6 && !data[6].trim().isEmpty()) {
            String employeeUsername = data[6].trim();
            User employee = userRepository.findByEmail(employeeUsername);
            if (employee != null) {
                ticket.setEmployee(employee);
            }
        }

        ticketRepository.save(ticket);
    }

    @ExceptionHandler(Exception.class)
    public String handleError(Exception ex, Model model) {
        model.addAttribute("error", "Une erreur s'est produite : " + ex.getMessage());
        return "csv/import";
    }
}