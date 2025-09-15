package com.amithfernando.qrseatreservation.ui.view.form;

import com.amithfernando.qrseatreservation.core.model.TableDetail;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

import javax.annotation.Nullable;
import java.util.Optional;

public class TableDetailForm extends Composite<FormLayout> {

    private final TextField tableNo;
    private final TextField description;
    private final IntegerField noOfSeats;
    private final Binder<TableDetail> binder;
    private @Nullable TableDetail formDataObject;

    public TableDetailForm() {
        //table no
        tableNo = new TextField("Table No");
        tableNo.setPlaceholder("A03");
        tableNo.setMaxLength(3);
        //description
        description = new TextField("Description");
        description.setPlaceholder("First row of tables");
        //no of seats
        noOfSeats = new IntegerField("Number of Seats");
        noOfSeats.setStepButtonsVisible(true);
        noOfSeats.setMin(0);
        noOfSeats.setMax(12);
        //for layout
        FormLayout layout = getContent();
        layout.add(tableNo, noOfSeats ,description);
        //binder
        binder = new Binder<>();
        binder.forField(tableNo)
                .asRequired("Table No is required")
                .bind(TableDetail::getTableName, TableDetail::setTableName);
        binder.forField(description)
                .bind(TableDetail::getDescription, TableDetail::setDescription);
        binder.forField(noOfSeats)
                .asRequired("Table No is required")
                .bind(TableDetail::getNoOfSeats, TableDetail::setNoOfSeats);
    }

    public Optional<TableDetail> getFormDataObject() {
        if (formDataObject == null) {
            formDataObject = new TableDetail();
        }
        if (binder.writeBeanIfValid(formDataObject)) {
            return Optional.of(formDataObject);
        } else {
            return Optional.empty();
        }
    }

    public void setFormDataObject(@Nullable TableDetail formDataObject) {
        this.formDataObject = formDataObject;
        if (formDataObject != null) {
            binder.readBean(formDataObject);
        } else {
            binder.refreshFields();
        }
    }


}
