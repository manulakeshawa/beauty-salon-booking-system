package com.manula.beautysalon.repository;

import com.manula.beautysalon.model.Stylist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StylistRepository extends JpaRepository<Stylist, Integer> {
    List<Stylist> findAllByOrderByUserIdAsc();

    Optional<Stylist> findByEmailIgnoreCase(String email);

    Optional<Stylist> findTopByOrderByUserIdDesc();
}
