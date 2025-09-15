package com.amithfernando.qrseatreservation.ui.view.form;

import com.amithfernando.qrseatreservation.core.model.SellerDetail;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

import javax.annotation.Nullable;
import java.util.Optional;

public class SellerDetailForm extends Composite<FormLayout> {

    private final TextField name;
    private final TextField description;
    private final TextField address;
    private final TextField phoneNo;
    private final TextField email;
    private final Binder<SellerDetail> binder;
    private @Nullable SellerDetail formDataObject;

    public SellerDetailForm() {
        //name
        name = new TextField("Name");
        name.setPlaceholder("Stefan");
        name.setMaxLength(20);
        //description
        description = new TextField("Description");
        description.setPlaceholder("Membership holder");
        //address
        address = new TextField("Address");
        address.setPlaceholder("Vienna 12th district");
        //phone no
        phoneNo = new TextField("Phone No");
        phoneNo.setPlaceholder("+43XX..");
        //email
        email = new TextField("Phone No");
        email.setPlaceholder("+43XX..");
        //for layout
        FormLayout layout = getContent();
        layout.add(name, phoneNo ,email,address,description);
        //binder
        binder = new Binder<>();
        binder.forField(name)
                .asRequired("Name is required")
                .bind(SellerDetail::getName, SellerDetail::setName);
        binder.forField(description)
                .bind(SellerDetail::getDescription, SellerDetail::setDescription);
        binder.forField(email)
                .bind(SellerDetail::getEmail, SellerDetail::setEmail);
        binder.forField(phoneNo)
                .asRequired("Phone No is required")
                .bind(SellerDetail::getPhone, SellerDetail::setPhone);
        binder.forField(address)
                .bind(SellerDetail::getAddress, SellerDetail::setAddress);
    }

    public Optional<SellerDetail> getFormDataObject() {
        if (formDataObject == null) {
            formDataObject = new SellerDetail();
        }
        if (binder.writeBeanIfValid(formDataObject)) {
            return Optional.of(formDataObject);
        } else {
            return Optional.empty();
        }
    }

    public void setFormDataObject(@Nullable SellerDetail formDataObject) {
        this.formDataObject = formDataObject;
        if (formDataObject != null) {
            binder.readBean(formDataObject);
        } else {
            binder.refreshFields();
        }
    }


}
