package com.qrseatreservation.core.repository;


import com.qrseatreservation.core.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    @Query("SELECT COUNT(u) > 0 FROM User u JOIN u.events e WHERE u.username = :username AND e.id = :eventId")
    boolean isAssignedToEvent(@Param("username") String username, @Param("eventId") Long eventId);
}