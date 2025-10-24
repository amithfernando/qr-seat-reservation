package com.amithfernando.qrseatreservation.fe.core.repsitory;

import com.amithfernando.qrseatreservation.fe.core.enums.TicketStatus;
import com.amithfernando.qrseatreservation.fe.core.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Ticket findByTicketNo(String ticketNo);

    List<Ticket> findTcicketByStatus(TicketStatus status);
}
