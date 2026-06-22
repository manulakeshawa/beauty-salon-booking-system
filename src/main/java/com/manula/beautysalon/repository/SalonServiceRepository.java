package com.manula.beautysalon.repository;

import com.manula.beautysalon.model.SalonService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SalonServiceRepository extends JpaRepository<SalonService, Integer> {
    List<SalonService> findAllByOrderByServiceIdAsc();

    Optional<SalonService> findTopByOrderByServiceIdDesc();
}
