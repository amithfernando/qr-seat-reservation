package com.amithfernando.qrseatreservation.api.repsitory;

import com.amithfernando.qrseatreservation.api.enums.TicketStatus;
import com.amithfernando.qrseatreservation.api.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Ticket findByTicketNo(String ticketNo);

    List<Ticket> findTcicketByStatus(TicketStatus status);
}
