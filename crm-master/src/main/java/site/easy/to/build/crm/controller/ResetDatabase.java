package site.easy.to.build.crm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import site.easy.to.build.crm.service.reset.DatabaseResetter;

@Controller
@RequestMapping("/database/settings")
public class ResetDatabase {

    @Autowired
    private DatabaseResetter databaseResetter;

    @GetMapping("/reset-database")
    public String resetDatabase() {
        databaseResetter.resetDatabase();
        return "redirect:/login"; 
    }
}