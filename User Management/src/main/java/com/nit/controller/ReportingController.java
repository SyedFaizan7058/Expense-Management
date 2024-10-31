package com.nit.controller;

import com.nit.entity.Expenses;
import com.nit.entity.User;
import com.nit.repository.ExpenseRepository;
import com.nit.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class ReportingController {

    //Dependency injection (i.e Field injection)
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    // Reporting Page
    @GetMapping("/reporting")
    public String getReportingPage(Model model, Principal principal) {

        //Taking authentication reference from context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        //taking currently logged-in users username
        String name = authentication.getName();

        //Fetching user by its username in database
        User loggedInUser = userRepository.findByUsername(name);

        //If user is admin
        if (loggedInUser.getRole().equalsIgnoreCase("admin")) {

            //Counting all users present in database
            long totalUsers = userRepository.count();

            //Finding total expense for current month of all users
            BigDecimal totalExpenseCurrentMonth = expenseRepository.findTotalExpenseForCurrentMonth(LocalDate.now().getMonthValue());

            // If no users, set average expense to 0
            BigDecimal averageExpense = BigDecimal.ZERO;
            if (totalUsers > 0) {
                averageExpense = totalExpenseCurrentMonth.divide(
                        BigDecimal.valueOf(totalUsers), 2, RoundingMode.HALF_UP);
            }

            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalExpenseCurrentMonth", totalExpenseCurrentMonth);
            model.addAttribute("averageExpense", averageExpense);
        } else {
            //If role is user
            BigDecimal userTotalExpense = expenseRepository.findTotalExpenseForUser(name, LocalDate.now().getMonthValue());
            BigDecimal userAverageExpense = expenseRepository.findAverageExpenseForUser(name, LocalDate.now().getMonthValue());

            model.addAttribute("userTotalExpense", userTotalExpense);
            model.addAttribute("userAverageExpense", userAverageExpense);
        }

        return "reporting";
    }

    // List all expenses for the current month
    @GetMapping("/monthly-expenses")
    public String getMonthlyExpenses(Model model) {

        //Taking authentication reference from context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        //taking currently logged-in users username
        String name = authentication.getName();
        User loggedInUser = userRepository.findByUsername(name);

        if (loggedInUser.getRole().equalsIgnoreCase("admin")) {
            //Finding all expenses list for current month
            List<Expenses> monthlyExpenses = expenseRepository.findAllForCurrentMonth(LocalDate.now().getMonthValue());
            model.addAttribute("expenses", monthlyExpenses);

        }else{
            //If it is user
            List<Expenses> userMonthlyExpenses = expenseRepository.findAllForUserAndCurrentMonth(loggedInUser.getId(), LocalDate.now().getMonthValue());
            model.addAttribute("expenses", userMonthlyExpenses);
        }
        return "monthly-expenses";
    }

}