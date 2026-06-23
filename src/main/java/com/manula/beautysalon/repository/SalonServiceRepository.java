package com.manula.beautysalon.repository;

import com.manula.beautysalon.model.SalonService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SalonServiceRepository extends JpaRepository<SalonService, Integer> {
    List<SalonService> findAllByOrderByServiceIdAsc();

    @Query("select s from SalonService s where s.active = true or s.active is null order by s.serviceId asc")
    List<SalonService> findActiveOrderByServiceIdAsc();

    List<SalonService> findByNameIgnoreCaseOrderByServiceIdDesc(String name);

    boolean existsByStylistNameIgnoreCase(String stylistName);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update SalonService s set s.stylistName = :newName where lower(s.stylistName) = lower(:oldName)")
    int updateStylistNameIgnoreCase(@Param("oldName") String oldName, @Param("newName") String newName);

    Optional<SalonService> findTopByOrderByServiceIdDesc();
}
