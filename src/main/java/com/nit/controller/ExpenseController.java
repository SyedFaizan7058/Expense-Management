package com.nit.controller;

import com.nit.entity.Expenses;
import com.nit.entity.User;
import com.nit.repository.ExpenseRepository;
import com.nit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ExpenseController {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    //Endpoint to load the expense page
    @GetMapping("/expense")
    public String expenses(Model model) {
        return "expense";
    }

    //Endpoint to load the expense form
    @GetMapping("/addExpense")
    public String addExpense(Model model) {
        model.addAttribute("expenseObj", new Expenses());
        return "newExpense";
    }

    // Endpoint to handle the form submission for adding an expense
    @PostMapping("/addExpense")
    public String addExpense(@ModelAttribute Expenses expense, Model model) {

        model.addAttribute("expenseObj", expense);
        // Taking authentication reference from context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        //taking currently logged-in users username
        String name = authentication.getName();

        //Finding user by username
        User byUsername = userRepository.findByUsername(name);

        //Setting user object in the expense entity
        expense.setUser(byUsername);

        //Saving the expense into the database
        Expenses savedExpense = expenseRepository.save(expense);
        model.addAttribute("msg","Expense added successfully");

        return "newExpense";
    }

    @GetMapping("/list")
    public String viewExpense(Model model) {
        model.addAttribute("expenseObj", new Expenses());

        //Taking authentication reference from context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        //taking currently logged-in users username
        String name = authentication.getName();

        User byUsername = userRepository.findByUsername(name);
        System.out.println(byUsername.getUsername());
        List<Expenses> list;
        if(byUsername.getRole().equalsIgnoreCase("admin")){
            list = expenseRepository.findAll();
        }else {
            //Finding user by username
            list = expenseRepository.findByUser_Username(name);
        }
        model.addAttribute("expenses", list);
        return "viewExpenses";
    }

}
