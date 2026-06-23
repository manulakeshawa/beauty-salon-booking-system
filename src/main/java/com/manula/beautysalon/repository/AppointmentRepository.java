package com.manula.beautysalon.repository;

import com.manula.beautysalon.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    List<Appointment> findAllByOrderByAppointmentIdAsc();

    List<Appointment> findByCustomerNameIgnoreCaseOrderByAppointmentIdAsc(String customerName);

    List<Appointment> findByStylistNameIgnoreCaseOrderByAppointmentDateAscAppointmentTimeAsc(String stylistName);

    long deleteByCustomerNameIgnoreCase(String customerName);

    boolean existsByServiceNameIgnoreCase(String serviceName);

    boolean existsByStylistNameIgnoreCase(String stylistName);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Appointment a set a.customerName = :newName where lower(a.customerName) = lower(:oldName)")
    int updateCustomerNameIgnoreCase(@Param("oldName") String oldName, @Param("newName") String newName);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Appointment a set a.serviceName = :newName where lower(a.serviceName) = lower(:oldName)")
    int updateServiceNameIgnoreCase(@Param("oldName") String oldName, @Param("newName") String newName);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Appointment a set a.stylistName = :newName where lower(a.stylistName) = lower(:oldName)")
    int updateStylistNameIgnoreCase(@Param("oldName") String oldName, @Param("newName") String newName);

    Optional<Appointment> findTopByOrderByAppointmentIdDesc();
}
