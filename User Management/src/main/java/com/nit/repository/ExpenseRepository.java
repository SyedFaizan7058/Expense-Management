package com.nit.repository;

import com.nit.entity.Expenses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository extends JpaRepository<Expenses, Long> {

    public List<Expenses> findByUser_Username(String username);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expenses e WHERE MONTH(e.expenseDate) = :month")
    BigDecimal findTotalExpenseForCurrentMonth(int month);

    // Query to list all expenses for the current month
    @Query("SELECT e FROM Expenses e WHERE MONTH(e.expenseDate) = :month")
    List<Expenses> findAllForCurrentMonth(int month);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expenses e WHERE e.user.username = :username AND MONTH(e.expenseDate) = :month")
    BigDecimal findTotalExpenseForUser(@Param("username") String username, @Param("month") int month);

    // Average expense for a specific user for the current month
    @Query("SELECT COALESCE(AVG(e.amount), 0) FROM Expenses e WHERE e.user.username = :username AND MONTH(e.expenseDate) = :month")
    BigDecimal findAverageExpenseForUser(@Param("username") String username, @Param("month") int month);

    @Query("SELECT e FROM Expenses e WHERE e.user.id = :userId AND MONTH(e.expenseDate) = :month")
    List<Expenses> findAllForUserAndCurrentMonth(@Param("userId") Long userId, @Param("month") int month);

}
