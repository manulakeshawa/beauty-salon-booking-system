package com.manula.beautysalon.repository;

import com.manula.beautysalon.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
    List<Appointment> findAllByOrderByAppointmentIdAsc();

    List<Appointment> findByCustomerNameIgnoreCaseOrderByAppointmentIdAsc(String customerName);

    List<Appointment> findByStylistNameIgnoreCaseOrderByAppointmentDateAscAppointmentTimeAsc(String stylistName);

    Optional<Appointment> findTopByOrderByAppointmentIdDesc();
}
