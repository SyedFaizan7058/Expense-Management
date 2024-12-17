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

    // for chart as admin
    @Query("""
                SELECT 
                    WEEK(e.expenseDate) AS weekNumber, 
                    SUM(e.amount) AS totalExpense
                FROM Expenses e
                WHERE MONTH(e.expenseDate) = MONTH(CURRENT_DATE)
                  AND YEAR(e.expenseDate) = YEAR(CURRENT_DATE)
                GROUP BY WEEK(e.expenseDate)
            """)
    List<Object[]> findCurrentMonthExpensesGroupedByWeek();

    @Query("SELECT QUARTER(e.expenseDate), SUM(e.amount) " + "FROM Expenses e " + "WHERE YEAR(e.expenseDate) = YEAR(CURRENT_DATE) "
            + "GROUP BY QUARTER(e.expenseDate) " + "ORDER BY QUARTER(e.expenseDate)")
    List<Object[]> findQuarterYearExpensesForCurrentYear();

    @Query("SELECT CASE " +
            "WHEN MONTH(e.expenseDate) BETWEEN 1 AND 6 THEN 'First Half' " +
            "WHEN MONTH(e.expenseDate) BETWEEN 7 AND 12 THEN 'Second Half' " +
            "END AS halfYear, " +
            "SUM(e.amount) " +
            "FROM Expenses e " +
            "WHERE YEAR(e.expenseDate) = YEAR(CURRENT_DATE) " +
            "GROUP BY halfYear ")
    List<Object[]> findHalfYearExpensesForCurrentYear();

    @Query("SELECT MONTH(e.expenseDate) AS month, SUM(e.amount) AS totalAmount " +
            "FROM Expenses e " +
            "WHERE YEAR(e.expenseDate) = YEAR(CURRENT_DATE) " +
            "GROUP BY MONTH(e.expenseDate) " +
            "ORDER BY MONTH(e.expenseDate)")
    List<Object[]> findMonthlyExpensesForCurrentYear();

    // for chart as user
    @Query("""
                SELECT 
                    WEEK(e.expenseDate) AS weekNumber, 
                    SUM(e.amount) AS totalExpense
                FROM Expenses e
                WHERE MONTH(e.expenseDate) = MONTH(CURRENT_DATE)
                  AND YEAR(e.expenseDate) = YEAR(CURRENT_DATE)
                  AND e.user.username =:username
                GROUP BY WEEK(e.expenseDate)
            """)
    List<Object[]> findCurrentMonthExpensesGroupedByWeek(@Param("username") String username);

    @Query("SELECT QUARTER(e.expenseDate), SUM(e.amount) " +
            "FROM Expenses e " +
            "WHERE YEAR(e.expenseDate) = YEAR(CURRENT_DATE) " +
            "AND e.user.username = :username " + // Filter by username
            "GROUP BY QUARTER(e.expenseDate) " +
            "ORDER BY QUARTER(e.expenseDate)")
    List<Object[]> findQuarterYearExpensesForCurrentYear(@Param("username") String username);

    @Query("SELECT CASE " +
            "WHEN MONTH(e.expenseDate) BETWEEN 1 AND 6 THEN 'First Half' " +
            "WHEN MONTH(e.expenseDate) BETWEEN 7 AND 12 THEN 'Second Half' " +
            "END AS halfYear, " +
            "SUM(e.amount) " +
            "FROM Expenses e " +
            "WHERE YEAR(e.expenseDate) = YEAR(CURRENT_DATE) " +
            "AND e.user.username = :username " + // Filter by username
            "GROUP BY halfYear")
    List<Object[]> findHalfYearExpensesForCurrentYear(@Param("username") String username);

    @Query("SELECT MONTH(e.expenseDate) AS month, SUM(e.amount) AS totalAmount " +
            "FROM Expenses e " +
            "WHERE YEAR(e.expenseDate) = YEAR(CURRENT_DATE) " +
            "AND e.user.username = :username " + // Filter by username
            "GROUP BY MONTH(e.expenseDate) " +
            "ORDER BY MONTH(e.expenseDate)")
    List<Object[]> findMonthlyExpensesForCurrentYear(@Param("username") String username);

}
