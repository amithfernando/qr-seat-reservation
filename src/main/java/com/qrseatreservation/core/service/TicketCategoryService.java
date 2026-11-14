package com.qrseatreservation.core.service;

import com.qrseatreservation.core.event.EventAccessException;
import com.qrseatreservation.core.event.EventContext;
import com.qrseatreservation.core.model.Event;

import com.qrseatreservation.core.model.TicketCategory;
import com.qrseatreservation.core.repository.TicketCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TicketCategoryService {

    private final TicketCategoryRepository ticketCategoryRepository;
    private final EventContext eventContext;

    /**
     * Get all ticket categories (active and inactive)
     */
    @Transactional(readOnly = true)
    public List<TicketCategory> getAllCategories() {
        log.info("Fetching all ticket categories");
        return ticketCategoryRepository.findAllByEventOrdered(eventContext.currentId());
    }

    /**
     * Get only active ticket categories for user selection
     */
    @Transactional(readOnly = true)
    public List<TicketCategory> getAllActiveCategories() {
        log.info("Fetching active ticket categories");
        return ticketCategoryRepository.findActiveByEvent(eventContext.currentId());
    }

    /**
     * Save or update a ticket category
     */
    @Transactional
    public TicketCategory saveCategory(TicketCategory category) {
        log.info("Saving ticket category: {}", category.getName());
        Event event = eventContext.requireEditable();
        if (category.getId() == null) {
            category.setEvent(event);
        } else {
            TicketCategory existingCategory = ticketCategoryRepository.findByIdAndEventId(category.getId(), event.getId())
                    .orElseThrow(() -> new EventAccessException("Category does not belong to the current event"));
            category.setEvent(existingCategory.getEvent());
        }
        
        // Validate price is not negative
        if (category.getPrice() < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        
        // Check for duplicate name (excluding current entity if updating)
        Optional<TicketCategory> existing = ticketCategoryRepository.findByEventAndName(event.getId(), category.getName());
        if (existing.isPresent() && !existing.get().getId().equals(category.getId())) {
            throw new IllegalArgumentException("Category with name '" + category.getName() + "' already exists");
        }
        
        return ticketCategoryRepository.save(category);
    }

    /**
     * Delete a ticket category
     */
    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting ticket category with ID: {}", id);
        
        TicketCategory category = ticketCategoryRepository.findByIdAndEventId(id, eventContext.requireEditable().getId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));

        // TODO: Add check to prevent deletion if category has associated reservations
        ticketCategoryRepository.delete(category);
    }

    /**
     * Find category by ID
     */
    @Transactional(readOnly = true)
    public Optional<TicketCategory> findById(Long id) {
        return ticketCategoryRepository.findByIdAndEventId(id, eventContext.currentId());
    }

    /**
     * Find category by name
     */
    @Transactional(readOnly = true)
    public Optional<TicketCategory> findByName(String name) {
        return ticketCategoryRepository.findByEventAndName(eventContext.currentId(), name);
    }

    /**
     * Initialize default ticket categories if none exist
     */
    @Transactional
    public void initializeDefaultCategories() {
        Event event = eventContext.current();
        if (event.isArchived()) return;
        initializeDefaultCategories(event);
    }

    /** Creates FULL/HALF/INVITATION/STAFF for the given event if it has no categories yet. */
    @Transactional
    public void initializeDefaultCategories(Event event) {
        long count = ticketCategoryRepository.countByEventId(event.getId());
        if (count == 0) {
            log.info("Initializing default ticket categories");
            
            TicketCategory full = TicketCategory.builder()
                    .name("FULL")
                    .price(1000.00)
                    .description("Full price ticket")
                    .active(true)
                    .event(event)
                    .build();
            
            TicketCategory half = TicketCategory.builder()
                    .name("HALF")
                    .price(500.00)
                    .description("Half price ticket")
                    .active(true)
                    .event(event)
                    .build();
            
            TicketCategory invitation = TicketCategory.builder()
                    .name("INVITATION")
                    .price(0.00)
                    .description("Complimentary invitation ticket")
                    .active(true)
                    .event(event)
                    .build();
            
            TicketCategory staff = TicketCategory.builder()
                    .name("STAFF")
                    .price(0.00)
                    .description("Staff ticket")
                    .active(true)
                    .event(event)
                    .build();
            
            ticketCategoryRepository.save(full);
            ticketCategoryRepository.save(half);
            ticketCategoryRepository.save(invitation);
            ticketCategoryRepository.save(staff);
            
            log.info("Default ticket categories created successfully");
        }
    }
}
