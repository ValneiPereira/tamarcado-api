package com.tamarcado.adapter.out.persistence.repository;

import com.tamarcado.domain.model.user.User;
import com.tamarcado.domain.model.user.UserType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserJpaRepository extends JpaRepository<User, UUID> {

    @EntityGraph(attributePaths = {"address"})
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.userType = :userType")
    Optional<User> findByIdAndUserType(@Param("id") UUID id, @Param("userType") UserType userType);

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.userType = 'CLIENT'")
    Optional<User> findClientById(@Param("id") UUID id);

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.userType = 'PROFESSIONAL'")
    Optional<User> findProfessionalById(@Param("id") UUID id);
}
