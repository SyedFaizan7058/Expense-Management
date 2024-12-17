package com.nit.repository;

import com.nit.entity.User;
import org.hibernate.query.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    public User findByUsername(String username);

    List<User> findByUsernameContainingIgnoreCase(String query);
}
