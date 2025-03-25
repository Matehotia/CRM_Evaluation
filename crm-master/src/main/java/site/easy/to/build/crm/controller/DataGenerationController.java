package site.easy.to.build.crm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Controller
@RequestMapping("/employee")
public class DataGenerationController {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DataGenerationController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/generate-data")
    public String showGenerateDataPage(Model model) {
        model.addAttribute("title", "Generate Sample Data");
        return "data/generate";
    }

    @PostMapping("/generate-data")
    public String generateData(@RequestParam("entityType") String entityType,
                               @RequestParam("count") Integer count,
                               RedirectAttributes redirectAttributes) {
        
        if (count <= 0 || count > 1000) {
            redirectAttributes.addFlashAttribute("error", "Le nombre d'enregistrements doit être compris entre 1 et 1000");
            return "redirect:/employee/generate-data";
        }

        try {
            switch (entityType) {
                case "customers":
                    generateCustomerData(count);
                    redirectAttributes.addFlashAttribute("success", 
                        count + " client(s) et informations de connexion générés avec succès");
                    break;
                case "budgets":
                    generateBudgetData(count);
                    redirectAttributes.addFlashAttribute("success", 
                        count + " budget(s) générés avec succès");
                    break;
                case "tickets":
                    generateTicketData(count);
                    redirectAttributes.addFlashAttribute("success", 
                        count + " ticket(s) générés avec succès");
                    break;
                default:
                    redirectAttributes.addFlashAttribute("error", "Type d'entité non pris en charge");
                    return "redirect:/employee/generate-data";
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la génération des données: " + e.getMessage());
        }

        return "redirect:/employee/generate-data";
    }

    private void generateCustomerData(int count) {
        try {
            // Tableaux de données pour la génération aléatoire
            List<String> firstNames = Arrays.asList("John", "Jane", "Michael", "Sarah", "David", "Emma", "Robert", "Lisa", 
                    "William", "Emily", "James", "Sophia", "Charles", "Olivia", "Thomas", "Aya", "Daniel", "Isabella", 
                    "Matthew", "Mia", "Richard", "Charlotte", "Joseph", "Amelia", "Christopher", "Elizabeth", "Andrew", 
                    "Sofia", "Paul", "Grace");
            
            List<String> lastNames = Arrays.asList("Smith", "Johnson", "Williams", "Brown", "Jones", "Miller", "Davis", 
                    "Garcia", "Rodriguez", "Wilson", "Martinez", "Anderson", "Taylor", "Thomas", "Hernandez", "Moore", 
                    "Martin", "Jackson", "Thompson", "White", "Lopez", "Lee", "Gonzalez", "Harris", "Clark", "Lewis", 
                    "Robinson", "Walker", "Perez", "Hall");
            
            List<String> domains = Arrays.asList("gmail.com");
            
            List<String> cities = Arrays.asList("Paris", "Lyon", "Marseille", "Lille", "Toulouse", "Nice", "Bordeaux", 
                    "Nantes", "Strasbourg", "Montpellier", "Rennes", "Reims", "Saint-Etienne", "Toulon", "Angers", 
                    "Grenoble", "Dijon", "Nimes", "Aix-en-Provence", "Nancy");
            
            List<String> states = Arrays.asList("Ile-de-France", "Auvergne-Rhone-Alpes", "Provence-Alpes-Cote-Azur", 
                    "Hauts-de-France", "Occitanie", "Grand Est", "Nouvelle-Aquitaine", "Pays de la Loire", "Bretagne", 
                    "Normandie", "Bourgogne-Franche-Comte", "Centre-Val de Loire", "Corse");
            
            List<String> countries = Arrays.asList("France");
            
            List<String> positions = Arrays.asList("CEO", "CTO", "CFO", "COO", "Manager", "Director", "VP", 
                    "Sales Representative", "Marketing Specialist", "HR Manager", "Software Engineer", "Product Manager", 
                    "Project Manager", "Business Analyst", "Financial Analyst", "Account Manager", "Customer Support", 
                    "Consultant", "Technician", "Administrator");
            
            List<String> descriptions = Arrays.asList("Client VIP", "Nouveau client", "Client fidele", "Client potentiel", 
                    "Client de longue date", "Client recommande", "Client entreprise", "Client particulier", 
                    "Client international", "Client local");
            
            Random random = new Random();
            
            // Préparer les insertions
            final String insertLoginInfoQuery = "INSERT INTO customer_login_info (username, password, token, password_set) " +
                    "VALUES (?, NULL, ?, 0)";
                    
            final String insertCustomerQuery = "INSERT INTO customer (name, phone, email, address, city, state, country, " +
                    "user_id, description, position, twitter, facebook, youtube, created_at, profile_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            final int userId = 52; // ID utilisateur par défaut
            
            for (int i = 0; i < count; i++) {
                // Générer des données aléatoires
                String firstName = firstNames.get(random.nextInt(firstNames.size()));
                String lastName = lastNames.get(random.nextInt(lastNames.size()));
                String email = (firstName.toLowerCase() + "." + lastName.toLowerCase() + 
                        random.nextInt(1000) + "@" + domains.get(random.nextInt(domains.size()))).replace(" ", "");
                String token = UUID.randomUUID().toString();
                
                // Insérer dans customer_login_info
                KeyHolder keyHolder = new GeneratedKeyHolder();
                jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(insertLoginInfoQuery, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, email);
                    ps.setString(2, token);
                    return ps;
                }, keyHolder);
                
                // Récupérer l'ID généré
                int customerLoginId = keyHolder.getKey().intValue();
                
                // Générer la date de création
                Calendar cal = Calendar.getInstance();
                cal.set(2023, 0, 1); // 1er janvier 2023
                cal.add(Calendar.DATE, random.nextInt(365)); // Ajouter entre 0 et 364 jours
                java.util.Date createdDate = cal.getTime();
                java.sql.Timestamp createdDateTime = new java.sql.Timestamp(createdDate.getTime());
                
                // Insérer dans customer
                jdbcTemplate.update(insertCustomerQuery,
                        firstName + " " + lastName, // name
                        "0" + (100000000 + random.nextInt(900000000)), // phone
                        email, // email
                        (1 + random.nextInt(100)) + " rue " + (char)(65 + random.nextInt(26)) + (char)(65 + random.nextInt(26)), // address
                        cities.get(random.nextInt(cities.size())), // city
                        states.get(random.nextInt(states.size())), // state
                        countries.get(random.nextInt(countries.size())), // country
                        userId, // user_id
                        descriptions.get(random.nextInt(descriptions.size())), // description
                        positions.get(random.nextInt(positions.size())), // position
                        random.nextBoolean() ? "@" + firstName.toLowerCase() + lastName.toLowerCase() : null, // twitter
                        random.nextBoolean() ? "https://facebook.com/" + firstName.toLowerCase() + "." + lastName.toLowerCase() : null, // facebook
                        random.nextDouble() > 0.7 ? "https://youtube.com/user/" + firstName.toLowerCase() + lastName.toLowerCase() : null, // youtube
                        createdDateTime, // created_at
                        customerLoginId // profile_id
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la génération des données: " + e.getMessage(), e);
        }
    }

    private void generateBudgetData(int count) {
        try {
            // Récupérer les IDs de tous les clients existants
            List<Integer> customerIds = jdbcTemplate.queryForList("SELECT customer_id FROM customer", Integer.class);
            
            if (customerIds.isEmpty()) {
                throw new RuntimeException("Aucun client trouvé. Veuillez générer des clients avant de générer des budgets.");
            }
            
            // Tableaux de données pour la génération aléatoire
            List<String> budgetNames = Arrays.asList(
                    "Budget marketing", "Budget développement", "Budget opérationnel", 
                    "Budget formation", "Budget événementiel", "Budget publicitaire",
                    "Budget recrutement", "Budget investissement", "Budget innovation",
                    "Budget maintenance", "Budget expansion", "Budget recherche");
            
            List<String> currencies = Arrays.asList("EUR", "USD", "GBP", "CHF");
            
            Random random = new Random();
            
            // Préparer l'insertion
            final String insertBudgetQuery = "INSERT INTO budgets (customer_id, budget_name, budget_amount, currency, " +
                    "start_date, end_date, alert_threshold_percentage) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            for (int i = 0; i < count; i++) {
                // Sélectionner un client aléatoire
                int customerId = customerIds.get(random.nextInt(customerIds.size()));
                
                // Générer un nom de budget aléatoire
                String budgetName = budgetNames.get(random.nextInt(budgetNames.size()));
                if (random.nextBoolean()) {
                    budgetName += " " + (2023 + random.nextInt(3)); // Ajouter une année aléatoire
                }
                
                // Générer un montant entre 1000 et 100000
                BigDecimal budgetAmount = new BigDecimal(1000 + random.nextInt(99000))
                        .add(new BigDecimal(random.nextInt(100) / 100.0))
                        .setScale(2, RoundingMode.HALF_UP);
                
                // Devise
                String currency = currencies.get(random.nextInt(currencies.size()));
                
                // Dates de début et de fin
                Calendar startCal = Calendar.getInstance();
                startCal.set(2023, random.nextInt(12), 1 + random.nextInt(28));
                java.sql.Date startDate = new java.sql.Date(startCal.getTimeInMillis());
                
                Calendar endCal = (Calendar) startCal.clone();
                endCal.add(Calendar.MONTH, 3 + random.nextInt(9)); // Durée de 3 à 12 mois
                java.sql.Date endDate = new java.sql.Date(endCal.getTimeInMillis());
                
                // Seuil d'alerte (entre 70% et 95%)
                BigDecimal alertThreshold = new BigDecimal(70 + random.nextInt(25))
                        .add(new BigDecimal(random.nextInt(100) / 100.0))
                        .setScale(2, RoundingMode.HALF_UP);
                
                // Insérer le budget
                jdbcTemplate.update(insertBudgetQuery,
                        customerId,
                        budgetName,
                        budgetAmount,
                        currency,
                        startDate,
                        endDate,
                        alertThreshold
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la génération des budgets: " + e.getMessage(), e);
        }
    }

    private void generateTicketData(int count) {
        try {
            // Recuperer les IDs de tous les clients existants
            List<Integer> customerIds = jdbcTemplate.queryForList("SELECT customer_id FROM customer", Integer.class);
            
            if (customerIds.isEmpty()) {
                throw new RuntimeException("Aucun client trouve. Veuillez generer des clients avant de generer des tickets.");
            }
            
            // Recuperer les IDs des employes et managers
            List<Integer> employeeIds = jdbcTemplate.queryForList(
                "SELECT id FROM users WHERE id != 52", Integer.class); // exclure l'ID 52 qui est souvent utilise comme ID par defaut
            
            // Tableaux de donnees pour la generation aleatoire
            List<String> subjects = Arrays.asList(
                "Probleme de connexion", "Question sur la facturation", "Demande d'assistance technique",
                "Suggestion d'amelioration", "Rapport de bug", "Demande d'information",
                "Reclamation", "Suivi de commande", "Probleme d'installation",
                "Question sur le produit", "Demande de remboursement", "Probleme de performance"
            );
            
            List<String> descriptions = Arrays.asList(
                "Le client rencontre des difficultes et demande de l'aide.",
                "Une assistance urgente est requise pour resoudre ce probleme.",
                "Le client souhaite obtenir plus d'informations sur cette fonctionnalite.",
                "Il s'agit d'une demande recurrente qui necessite une attention particuliere.",
                "Le client signale un dysfonctionnement qui affecte son utilisation quotidienne.",
                "Une solution rapide est demandee pour maintenir la satisfaction du client.",
                "Cette demande fait suite a une precedente conversation non resolue.",
                "Le client est un VIP et sa demande doit etre traitee en priorite.",
                "Ce ticket est lie a une mise a jour recente du systeme.",
                "Le client suggere une amelioration pour le prochain developpement."
            );
            
            // Utiliser les valeurs acceptees par les regex dans l'entite Ticket.java
            List<String> statuses = Arrays.asList("open", "assigned", "on-hold", "in-progress", "resolved", "closed", "reopened", "pending-customer-response", "escalated", "archived");
            List<String> priorities = Arrays.asList("low", "medium", "high", "urgent", "critical");
            
            Random random = new Random();
            
            // Preparer l'insertion
            final String insertTicketQuery = "INSERT INTO trigger_ticket (subject, description, status, priority, " +
                    "customer_id, manager_id, employee_id, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            for (int i = 0; i < count; i++) {
                // Selectionner un client aleatoire
                int customerId = customerIds.get(random.nextInt(customerIds.size()));
                
                // Generer des donnees aleatoires pour le ticket
                String subject = subjects.get(random.nextInt(subjects.size()));
                String description = descriptions.get(random.nextInt(descriptions.size()));
                String status = statuses.get(random.nextInt(statuses.size()));
                String priority = priorities.get(random.nextInt(priorities.size()));
                
                // Assignation aleatoire d'un manager OU d'un employe (pas les deux)
                Integer managerId = null;
                Integer employeeId = null;
                
                if (!employeeIds.isEmpty()) {
                    // Decider aleatoirement si on assigne un manager ou un employe
                    boolean assignManager = random.nextBoolean();
                    
                    if (assignManager) {
                        managerId = employeeIds.get(random.nextInt(employeeIds.size()));
                        employeeId = managerId;
                    } else {
                        employeeId = employeeIds.get(random.nextInt(employeeIds.size()));
                    }
                }
                
                // Generer la date de creation
                Calendar cal = Calendar.getInstance();
                cal.set(2023, 0, 1); // 1er janvier 2023
                cal.add(Calendar.DATE, random.nextInt(365)); // Ajouter entre 0 et 364 jours
                cal.add(Calendar.HOUR, random.nextInt(24)); // Ajouter des heures
                cal.add(Calendar.MINUTE, random.nextInt(60)); // Ajouter des minutes
                java.util.Date createdDate = cal.getTime();
                java.sql.Timestamp createdDateTime = new java.sql.Timestamp(createdDate.getTime());
                
                // Inserer le ticket
                jdbcTemplate.update(insertTicketQuery,
                    subject,
                    description,
                    status,
                    priority,
                    customerId,
                    managerId,
                    employeeId,
                    createdDateTime
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la generation des tickets: " + e.getMessage(), e);
        }
    }
} 