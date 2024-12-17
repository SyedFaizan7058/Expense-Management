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
import java.util.*;
import java.util.stream.Collectors;

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

        LocalDate now = LocalDate.now();
        LocalDate quarterStart = now.with(now.getMonth().firstMonthOfQuarter());
        LocalDate halfYearStart = now.getMonthValue() <= 6 ? LocalDate.of(now.getYear(), 1, 1) : LocalDate.of(now.getYear(), 7, 1);
        LocalDate yearStart = LocalDate.of(now.getYear(), 1, 1);

        //If user is admin
        if (loggedInUser.getRole().equalsIgnoreCase("admin")) {

            //Counting all users present in database
            long totalUsers = userRepository.count();

            //Finding total expense for current month of all users
            BigDecimal totalExpenseCurrentMonth = expenseRepository.findTotalExpenseForCurrentMonth(now.getMonthValue());

            // If no users, set average expense to 0
            BigDecimal averageExpense = BigDecimal.ZERO;
            if (totalUsers > 0) {
                averageExpense = totalExpenseCurrentMonth.divide(
                        BigDecimal.valueOf(totalUsers), 2, RoundingMode.HALF_UP);
            }

            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalExpenseCurrentMonth", totalExpenseCurrentMonth);
            model.addAttribute("averageExpense", averageExpense);

            //for current month
            List<Object[]> currentMonthData = expenseRepository.findCurrentMonthExpensesGroupedByWeek();
            model.addAttribute("currentMonthData",currentMonthData);

            //for quarterly
            List<Object[]> quarterlyData = expenseRepository.findQuarterYearExpensesForCurrentYear();
            model.addAttribute("quarterlyData",quarterlyData);

            //for half year data
            List<Object[]> halfYearExpenses = expenseRepository.findHalfYearExpensesForCurrentYear();
            // Process the result into a format that can be displayed in the view
            Map<String, Double> halfYearData = new LinkedHashMap<>();
            for (Object[] data : halfYearExpenses) {
                String halfYear = (String) data[0];
                Double amount = (Double) data[1];
                halfYearData.put(halfYear, amount);
            }
            model.addAttribute("halfYearData",halfYearData);

            //for full year
            List<Object[]> monthlyExpenses = expenseRepository.findMonthlyExpensesForCurrentYear();

            // Prepare a map to store the month and corresponding expense sum
            Map<String, Double> fullYearData = new HashMap<>();

            // Mapping month numbers to their names
            String[] monthNames = {
                    "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
            };

            // Process the results from the query into the model
            for (Object[] data : monthlyExpenses) {
                Integer month = (Integer) data[0]; // Month number (1-12)
                Double amount = (Double) data[1];  // Total amount for that month
                fullYearData.put(monthNames[month - 1], amount); // Mapping month number to its name
            }
            model.addAttribute("fullYearData",fullYearData);

        } else {
            //If role is user
            BigDecimal userTotalExpense = expenseRepository.findTotalExpenseForUser(name, LocalDate.now().getMonthValue());
            BigDecimal userAverageExpense = expenseRepository.findAverageExpenseForUser(name, LocalDate.now().getMonthValue());

            model.addAttribute("userTotalExpense", userTotalExpense);
            model.addAttribute("userAverageExpense", userAverageExpense);

            //for current month
            List<Object[]> currentMonthData = expenseRepository.findCurrentMonthExpensesGroupedByWeek(loggedInUser.getUsername());
            model.addAttribute("currentMonthData",currentMonthData);

            //for quarterly
            List<Object[]> quarterlyData = expenseRepository.findQuarterYearExpensesForCurrentYear(loggedInUser.getUsername());
            model.addAttribute("quarterlyData",quarterlyData);

            //for half year data
            List<Object[]> halfYearExpenses = expenseRepository.findHalfYearExpensesForCurrentYear(loggedInUser.getUsername());
            // Process the result into a format that can be displayed in the view
            Map<String, Double> halfYearData = new LinkedHashMap<>();
            for (Object[] data : halfYearExpenses) {
                String halfYear = (String) data[0];
                Double amount = (Double) data[1];
                halfYearData.put(halfYear, amount);
            }
            model.addAttribute("halfYearData",halfYearData);

            //for full year
            List<Object[]> monthlyExpenses = expenseRepository.findMonthlyExpensesForCurrentYear(loggedInUser.getUsername());

            // Prepare a map to store the month and corresponding expense sum
            Map<String, Double> fullYearData = new HashMap<>();

            // Mapping month numbers to their names
            String[] monthNames = {
                    "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
            };

            // Process the results from the query into the model
            for (Object[] data : monthlyExpenses) {
                Integer month = (Integer) data[0]; // Month number (1-12)
                Double amount = (Double) data[1];  // Total amount for that month
                fullYearData.put(monthNames[month - 1], amount); // Mapping month number to its name
            }
            model.addAttribute("fullYearData",fullYearData);
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