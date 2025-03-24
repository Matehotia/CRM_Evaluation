package site.easy.to.build.crm.service.reset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@Service
public class DatabaseResetter {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    public void resetDatabase() {
        String[] sqlCommands = {
            "SET FOREIGN_KEY_CHECKS = 0;",
            // "DELETE FROM email_template;",
            // "DELETE FROM contract_settings;",
            // "DELETE FROM trigger_contract;",
            "DELETE FROM trigger_ticket;",
            // "DELETE FROM trigger_lead;",
            // "DELETE FROM customer;",
            // "DELETE FROM customer_login_info;",
            // "DELETE FROM employee;",
            "SET FOREIGN_KEY_CHECKS = 1;",
        };

        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {

            for (String sql : sqlCommands) {
                statement.execute(sql);
            }

            System.out.println("Database reset successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}