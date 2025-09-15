package com.amithfernando.qrseatreservation.ui.view.form;

import com.amithfernando.qrseatreservation.core.model.ReservationDetail;
import com.amithfernando.qrseatreservation.core.model.SeatDetail;
import com.amithfernando.qrseatreservation.core.model.SeatReservation;
import com.amithfernando.qrseatreservation.core.model.SellerDetail;
import com.amithfernando.qrseatreservation.core.model.TableDetail;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class ReservationDetailForm extends Composite<FormLayout> {

    private final Binder<ReservationDetail> binder;
    private final Select<SellerDetail> sellersSelect;
    private final Select<TableDetail> tablesSelect;
    private final MultiSelectComboBox<SeatDetail> seatDtoMultiSelectComboBox;
    private final TextField description;
    private @Nullable ReservationDetail formDataObject;
    private Set<SeatReservation> seatReservations;

    public ReservationDetailForm(List<SellerDetail> sellers, List<TableDetail> tables) {
        //seller
        sellersSelect = new Select();
        sellersSelect.setLabel("Seller");
        sellersSelect.setItems(sellers);
        sellersSelect.setItemLabelGenerator(SellerDetail::getName);
        //seller
        tablesSelect = new Select();
        tablesSelect.setLabel("Table");
        tablesSelect.setItems(tables);
        tablesSelect.setItemLabelGenerator(TableDetail::getTableNoWithStatus);
        tablesSelect.addValueChangeListener(this::onTableChangeEvent);
        //seats
        seatDtoMultiSelectComboBox = new MultiSelectComboBox<>();
        seatDtoMultiSelectComboBox.setLabel("Seats");
        seatDtoMultiSelectComboBox.setItemLabelGenerator(SeatDetail::getNoWithStatus);
        seatDtoMultiSelectComboBox.setItems(new ArrayList<>());
        seatDtoMultiSelectComboBox.addValueChangeListener(this::onSeatSelectEvent);
        //description
        description = new TextField("Description");
        description.setPlaceholder("");
        //for layout
        FormLayout layout = getContent();
        layout.add(sellersSelect, tablesSelect, seatDtoMultiSelectComboBox,description);
        //binder
        binder = new Binder<>();
        
        // Set up all bindings first
        // Seller binding
        binder.forField(sellersSelect)
                .asRequired("Seller is required")
                .bind(ReservationDetail::getSellerDetail, ReservationDetail::setSellerDetail);
                
        // Table binding - we need to complete the binding even if it's just for reading
        binder.forField(tablesSelect)
                .asRequired("Table is required")
                .bind(
                    reservation -> tablesSelect.getValue(), // Getter
                    (reservation, table) -> {} // No-op setter as we handle this in the change listener
                );
                
        // Seats binding - we'll handle the actual binding in the setFormDataObject method
        binder.forField(seatDtoMultiSelectComboBox)
                .asRequired("At least one seat must be selected")
                .bind(
                    reservation -> seatDtoMultiSelectComboBox.getSelectedItems(), // Getter
                    (reservation, seats) -> {} // No-op setter as we handle this in the change listener
                );
                
        // Description binding
        binder.forField(description)
                .bind(ReservationDetail::getDescription, ReservationDetail::setDescription);

        seatReservations=new HashSet<>();
    }

    private void onTableChangeEvent(AbstractField.ComponentValueChangeEvent e) {
        log.info("onTableChangeEvent {}", e.getValue());
        if(e.getValue() != null) {
            TableDetail value = (TableDetail) e.getValue();
            seatDtoMultiSelectComboBox.setItems(value.getSeatDetails().stream().filter(seatDetail -> !seatDetail.isReserved()).toList());
            seatDtoMultiSelectComboBox.setItemLabelGenerator(SeatDetail::getNoWithStatus);
        }
    }

    private void onSeatSelectEvent(AbstractField.ComponentValueChangeEvent e) {
        log.info("onSeatSelectEvent {}", e.getValue());
        if(e.getValue() != null) {
            Set<SeatDetail> seatDetails = (Set<SeatDetail>) e.getValue();
            for (SeatDetail seatDetail : seatDetails) {
                seatReservations.add(new SeatReservation(seatDetail));
            }
        }
    }

    public Optional<ReservationDetail> getFormDataObject() {
        if (formDataObject == null) {
            formDataObject = new ReservationDetail();
        }
        
        try {
            // First update the form data object with the current form values
            if (binder.writeBeanIfValid(formDataObject)) {
                // Set the seat reservations from our local set
                formDataObject.setSeatReservations(seatReservations);
                return Optional.of(formDataObject);
            } else {
                // Validation failed
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Error getting form data object", e);
            return Optional.empty();
        }
    }

    public void setFormDataObject(@Nullable ReservationDetail formDataObject,List<SellerDetail> sellers, List<TableDetail> tables) {
        this.formDataObject = formDataObject;
        if (formDataObject != null) {
            // First set the seller
            sellersSelect.setValue(formDataObject.getSellerDetail());
            
            // If there are seat reservations, set the table and seats
            if (formDataObject.getSeatReservations() != null && !formDataObject.getSeatReservations().isEmpty()) {
                // Get the first seat's table
                TableDetail table = formDataObject.getSeatReservations().iterator().next().getSeatDetail().getTableDetail();
                tablesSelect.setValue(table);
                
                // Set the seat details in the multi-select
                Set<SeatDetail> selectedSeats = formDataObject.getSeatReservations().stream()
                        .map(SeatReservation::getSeatDetail)
                        .collect(java.util.stream.Collectors.toSet());
                seatDtoMultiSelectComboBox.setItems(table.getSeatDetails());
                seatDtoMultiSelectComboBox.select(selectedSeats);
                
                // Update the seatReservations set
                this.seatReservations = new HashSet<>(formDataObject.getSeatReservations());
            }
            
            // Set the description
            description.setValue(formDataObject.getDescription() != null ? formDataObject.getDescription() : "");
            
            // Read the bean with all fields properly set
            binder.readBean(formDataObject);
        } else {
            // Clear the form
            tablesSelect.setItems(tables);
            sellersSelect.setItems(sellers);
            seatDtoMultiSelectComboBox.setItems(new ArrayList<>());
            description.clear();
            this.seatReservations = new HashSet<>();
        }
    }


}
