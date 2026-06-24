package com.manula.beautysalon.repository;

import com.manula.beautysalon.model.Stylist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StylistRepository extends JpaRepository<Stylist, Integer> {
    List<Stylist> findAllByOrderByUserIdAsc();

    @Query("select s from Stylist s where s.active = true or s.active is null order by s.userId asc")
    List<Stylist> findActiveOrderByUserIdAsc();

    @Query("select s from Stylist s where (s.active = true or s.active is null) and s.available = true order by s.userId asc")
    List<Stylist> findActiveAndAvailableOrderByUserIdAsc();

    Optional<Stylist> findByEmailIgnoreCase(String email);

    Optional<Stylist> findByPasswordSetupTokenHash(String passwordSetupTokenHash);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndUserIdNot(String email, int userId);

    Optional<Stylist> findTopByOrderByUserIdDesc();
}
