package com.silaipro.repository;

import com.silaipro.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("SELECT u FROM User u WHERE u.phone = :login OR u.email = :login")
    Optional<User> findByPhoneOrEmail(@Param("login") String login);

    Optional<User> findByPhone(String phone);
    
    Optional<User> findByEmail(String email);
}
