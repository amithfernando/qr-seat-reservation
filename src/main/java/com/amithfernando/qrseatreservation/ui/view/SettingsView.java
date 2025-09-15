package com.amithfernando.qrseatreservation.ui.view;

import com.amithfernando.qrseatreservation.core.model.Setting;
import com.amithfernando.qrseatreservation.core.service.SettingService;
import com.amithfernando.qrseatreservation.ui.view.layout.MainLayout;
import com.amithfernando.qrseatreservation.ui.view.layout.PageLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.IntegerRangeValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.Tab;
import com.amithfernando.qrseatreservation.core.service.TicketService;
import jakarta.annotation.security.RolesAllowed;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Route(value="settings", layout = MainLayout.class)
@RolesAllowed({"ADMIN"})
@PageTitle("Settings")
public class SettingsView extends PageLayout {

    private final SettingService settingService;
    private Setting  setting;
    private final TicketService ticketService;

    // Binder
    private final Binder<Setting> binder = new Binder<>(Setting.class);

    // Form fields
    private final TextField eventName = new TextField("Event name");
    private final TextField venue = new TextField("Venue");

    private final IntegerField tableSize = new IntegerField("Table size");
    private final IntegerField seatSize = new IntegerField("Seat size");
    private final IntegerField noOfColumns = new IntegerField("No. of columns");

    private final IntegerField fontSize = new IntegerField("Font size");
    private final IntegerField qrX = new IntegerField("QR X");
    private final IntegerField qrY = new IntegerField("QR Y");
    private final IntegerField textX = new IntegerField("Text X");
    private final IntegerField textY = new IntegerField("Text Y");

    private final TextField ticketPrefix = new TextField("Ticket prefix");
    private final IntegerField noOfDigits = new IntegerField("No. of digits");
    private final IntegerField maxNoOfTickets = new IntegerField("Max no. of tickets");

    // Upload for base image (LOB)
    private final MemoryBuffer imageBuffer = new MemoryBuffer();
    private final Upload baseImageUpload = new Upload(imageBuffer);
    private byte[] uploadedBaseImage; // holds current base image bytes
    private final Image baseImagePreview = new Image();

    // Actions
    private final Button saveBtn = new Button("Save");
    private final Button resetBtn = new Button("Reset");
    private final Button generateTicketsBtn = new Button("Generate Tickets");

    public SettingsView(SettingService settingService, TicketService ticketService) {
        super(VaadinIcon.COG,"Settings");
        this.settingService = settingService;
        this.ticketService = ticketService;

        setSizeFull();
        buildForm();
        configureBinder();
        configureUpload();
        loadOrInit();
    }

    private void buildForm() {
        // UX tweaks
        eventName.setRequiredIndicatorVisible(true);
        eventName.setClearButtonVisible(true);
        eventName.setWidthFull();

        venue.setClearButtonVisible(true);
        venue.setWidthFull();

        for (IntegerField f : new IntegerField[]{tableSize, seatSize, noOfColumns, fontSize, qrX, qrY, textX, textY, noOfDigits, maxNoOfTickets}) {
            f.setStepButtonsVisible(true);
            f.setWidthFull();
        }
        tableSize.setMin(10);
        seatSize.setMin(4);
        noOfColumns.setMin(1);
        fontSize.setMin(6);
        noOfDigits.setMin(1);
        maxNoOfTickets.setMin(0);

        ticketPrefix.setClearButtonVisible(true);
        ticketPrefix.setPlaceholder("Optional (e.g., EVT-)");
        ticketPrefix.setWidthFull();

        // Upload configuration (basic UI; logic in configureUpload)
        baseImageUpload.setAcceptedFileTypes("image/png", "image/jpeg", "image/jpg");
        baseImageUpload.setMaxFiles(1);
        baseImageUpload.setDropAllowed(true);

        Span uploadHelp = new Span("Upload a PNG or JPEG base image used for ticket generation.");
        uploadHelp.getStyle().set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        baseImagePreview.setAlt("Base image preview");
        baseImagePreview.setWidth("160px");
        baseImagePreview.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "6px");

        Div imageField = new Div(new Span("Base image"), baseImageUpload, uploadHelp, baseImagePreview);
        imageField.getStyle().set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "0.5rem");

        // Tabs
        Tab generalTab = new Tab("General");
        Tab layoutTab = new Tab("Layout");
        Tab qrTab = new Tab("Ticket & QR");
        Tab ticketsTab = new Tab("Tickets");
        Tabs tabs = new Tabs(generalTab, layoutTab, qrTab, ticketsTab);
        tabs.setWidthFull();

        // Pages for tabs
        Div generalPage = new Div();
        Div layoutPage = new Div();
        Div qrPage = new Div();
        Div ticketsPage = new Div();
        generalPage.setWidthFull();
        layoutPage.setWidthFull();
        qrPage.setWidthFull();
        ticketsPage.setWidthFull();

        // General form
        FormLayout generalForm = new FormLayout(eventName, venue);
        generalForm.setWidthFull();
        generalForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );
        generalForm.setColspan(eventName, 2);
        generalForm.setColspan(venue, 2);
        generalPage.add(generalForm);

        // Layout form
        FormLayout layoutForm = new FormLayout(tableSize, seatSize, noOfColumns);
        layoutForm.setWidthFull();
        layoutForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 3)
        );
        layoutPage.add(layoutForm);

        // Ticket & QR form (remaining fields + image upload)
        FormLayout ticketForm = new FormLayout(
                fontSize, qrX, qrY, textX, textY,
                imageField
        );
        ticketForm.setWidthFull();
        ticketForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("720px", 2)
        );
        ticketForm.setColspan(imageField, 2);
        qrPage.add(ticketForm);

        // Preview button + sample ticket field on Ticket & QR tab
        TextField previewTicketNo = new TextField("Sample ticket #");
        previewTicketNo.setPlaceholder("e.g., " + (ticketPrefix.getValue() == null ? "T-" : ticketPrefix.getValue()) + "00001");
        previewTicketNo.setClearButtonVisible(true);
        Button previewBtn = new Button("Preview Ticket", VaadinIcon.EYE.create());
        previewBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        previewBtn.addClickListener(e -> {
            try {
                int f = fontSize.getValue() != null ? fontSize.getValue() : 12;
                int px = qrX.getValue() != null ? qrX.getValue() : 0;
                int py = qrY.getValue() != null ? qrY.getValue() : 0;
                int tx = textX.getValue() != null ? textX.getValue() : 0;
                int ty = textY.getValue() != null ? textY.getValue() : 0;

                String sample = previewTicketNo.getValue();
                if (sample == null || sample.isBlank()) {
                    String prefix = ticketPrefix.getValue() == null ? "" : ticketPrefix.getValue();
                    sample = prefix + "00001";
                }

                java.awt.image.BufferedImage img = ticketService.generateQrTicketPreview(f, px, py, tx, ty, sample);

                // Convert to stream for Vaadin Image
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                javax.imageio.ImageIO.write(img, "png", baos);
                byte[] bytes = baos.toByteArray();

                StreamResource res = new StreamResource("preview.png", () -> new java.io.ByteArrayInputStream(bytes));
                Image preview = new Image(res, "Ticket Preview");
                preview.setWidth("360px");
                Dialog dlg = new Dialog(preview);
                dlg.setHeaderTitle("Ticket Preview");
                Button close = new Button("Close", ev -> dlg.close());
                dlg.getFooter().add(close);
                dlg.setModal(true);
                dlg.setDraggable(true);
                dlg.setResizable(true);
                dlg.open();
            } catch (Exception ex) {
                Notification n = Notification.show("Failed to generate preview: " + ex.getMessage(),
                        3500, Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        HorizontalLayout qrActions = new HorizontalLayout(previewTicketNo, previewBtn);
        qrActions.setWidthFull();
        qrActions.setAlignItems(FlexComponent.Alignment.END);
        qrPage.add(qrActions);

        // Tickets form (prefix, digits, max count)
        FormLayout ticketsForm = new FormLayout(
                ticketPrefix, noOfDigits, maxNoOfTickets
        );
        ticketsForm.setWidthFull();
        ticketsForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 3)
        );
        ticketsPage.add(ticketsForm);

        // Generate Tickets button on Tickets tab
        generateTicketsBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        generateTicketsBtn.addClickListener(e -> {
            generateTicketsBtn.setEnabled(false);
            try {
                ticketService.generateTicketNos();
                Notification n = Notification.show("Ticket numbers generated", 2500, Notification.Position.BOTTOM_START);
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification n = Notification.show("Failed to generate tickets: " + ex.getMessage(),
                        3500, Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } finally {
                generateTicketsBtn.setEnabled(true);
            }
        });
        HorizontalLayout ticketsActions = new HorizontalLayout(generateTicketsBtn);
        ticketsActions.setWidthFull();
        ticketsActions.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);
        ticketsPage.add(ticketsActions);

        // Tab switching
        Div pages = new Div(generalPage, layoutPage, qrPage, ticketsPage);
        pages.setWidthFull();
        generalPage.setVisible(true);
        layoutPage.setVisible(false);
        qrPage.setVisible(false);
        ticketsPage.setVisible(false);

        tabs.addSelectedChangeListener(e -> {
            boolean gen = e.getSelectedTab() == generalTab;
            boolean lay = e.getSelectedTab() == layoutTab;
            boolean qr = e.getSelectedTab() == qrTab;
            generalPage.setVisible(gen);
            layoutPage.setVisible(lay);
            qrPage.setVisible(qr);
            ticketsPage.setVisible(!gen && !lay && !qr);
        });

        // Actions
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(e -> onSave());
        resetBtn.addClickListener(e -> loadOrInit());

        HorizontalLayout actions = new HorizontalLayout(resetBtn, saveBtn);
        actions.setWidthFull();
        actions.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);

        // Add to page
        addToContent(tabs, pages, actions);
    }

    private void configureBinder() {
        binder.forField(eventName)
                .asRequired("Event name is required")
                .withValidator(new StringLengthValidator("Max 255 characters", 1, 255))
                .bind(Setting::getEventName, Setting::setEventName);

        binder.forField(venue)
                .withValidator(new StringLengthValidator("Max 255 characters", 0, 255))
                .bind(Setting::getVenue, Setting::setVenue);

        binder.forField(tableSize)
                .withValidator(new IntegerRangeValidator("Table size must be >= 10", 10, Integer.MAX_VALUE))
                .bind(Setting::getTableSize, Setting::setTableSize);

        binder.forField(seatSize)
                .withValidator(new IntegerRangeValidator("Seat size must be >= 4", 4, Integer.MAX_VALUE))
                .bind(Setting::getSeatSize, Setting::setSeatSize);

        binder.forField(noOfColumns)
                .withValidator(new IntegerRangeValidator("Columns must be >= 1", 1, Integer.MAX_VALUE))
                .bind(Setting::getNoOfColumns, Setting::setNoOfColumns);

        binder.forField(fontSize)
                .withValidator(new IntegerRangeValidator("Font size must be >= 6", 6, Integer.MAX_VALUE))
                .bind(Setting::getFontSize, Setting::setFontSize);

        binder.forField(qrX)
                .withValidator(new IntegerRangeValidator("QR X must be >= 0", 0, Integer.MAX_VALUE))
                .bind(Setting::getQrX, Setting::setQrX);

        binder.forField(qrY)
                .withValidator(new IntegerRangeValidator("QR Y must be >= 0", 0, Integer.MAX_VALUE))
                .bind(Setting::getQrY, Setting::setQrY);

        binder.forField(textX)
                .withValidator(new IntegerRangeValidator("Text X must be >= 0", 0, Integer.MAX_VALUE))
                .bind(Setting::getTextX, Setting::setTextX);

        binder.forField(textY)
                .withValidator(new IntegerRangeValidator("Text Y must be >= 0", 0, Integer.MAX_VALUE))
                .bind(Setting::getTextY, Setting::setTextY);

        binder.forField(ticketPrefix)
                .withValidator(new StringLengthValidator("Max 32 characters", 0, 32))
                .bind(Setting::getTicketPrefix, Setting::setTicketPrefix);

        binder.forField(noOfDigits)
                .withValidator(new IntegerRangeValidator("Digits must be >= 1", 1, Integer.MAX_VALUE))
                .bind(Setting::getNoOfDigits, Setting::setNoOfDigits);

        binder.forField(maxNoOfTickets)
                .withValidator(new IntegerRangeValidator("Must be >= 0", 0, Integer.MAX_VALUE))
                .bind(Setting::getMaxNoOfTickets, Setting::setMaxNoOfTickets);
    }

    private void configureUpload() {
        baseImageUpload.addSucceededListener(e -> {
            String mime = e.getMIMEType() == null ? "" : e.getMIMEType().toLowerCase();
            if (!(mime.contains("png") || mime.contains("jpeg") || mime.contains("jpg"))) {
                showError("Unsupported image type. Please upload PNG or JPEG.");
                uploadedBaseImage = null;
                baseImagePreview.setSrc("");
                return;
            }
            try (InputStream in = imageBuffer.getInputStream()) {
                byte[] data = in.readAllBytes();
                if (data.length == 0) {
                    showError("Empty image.");
                    return;
                }
                uploadedBaseImage = data;
                // Preview
                StreamResource res = new StreamResource(e.getFileName(), () -> new ByteArrayInputStream(uploadedBaseImage));
                baseImagePreview.setSrc(res);
            } catch (IOException ex) {
                showError("Failed to read uploaded file: " + ex.getMessage());
            }
        });

        baseImageUpload.addFileRejectedListener(e -> showError(e.getErrorMessage()));
        baseImageUpload.addFailedListener(e -> showError("Upload failed"));
        baseImageUpload.addAllFinishedListener(e -> {
            // no-op
        });
    }

    private void loadOrInit() {
        try {
            // Only one record: service should fetch singleton or first/only record
            setting = settingService.getSetting();
            if (setting == null) {
                setting = new Setting();
                // sensible defaults
                setting.setTableSize(100);
                setting.setSeatSize(24);
                setting.setNoOfColumns(3);
                setting.setFontSize(14);
                setting.setQrX(50);
                setting.setQrY(50);
                setting.setTextX(50);
                setting.setTextY(120);
                setting.setNoOfDigits(5);
                setting.setMaxNoOfTickets(0);
            }
            binder.readBean(setting);

            // Load preview for existing base image
            uploadedBaseImage = setting.getBaseImage();
            if (uploadedBaseImage != null && uploadedBaseImage.length > 0) {
                StreamResource res = new StreamResource("base-image", () -> new ByteArrayInputStream(uploadedBaseImage));
                baseImagePreview.setSrc(res);
            } else {
                baseImagePreview.setSrc("");
            }
        } catch (Exception ex) {
            showError("Failed to load settings: " + ex.getMessage());
            // fallback to empty form
            if (setting == null) {
                setting = new Setting();
                binder.readBean(setting);
            }
        }
    }

    private void onSave() {
        try {
            binder.writeBean(setting);
        } catch (ValidationException ve) {
            showError("Please fix validation errors.");
            return;
        }

        // Attach uploaded base image if provided; leave existing if not changed
        if (uploadedBaseImage != null && uploadedBaseImage.length > 0) {
            setting.setBaseImage(uploadedBaseImage);
        }

        try {
            // Ensure only one record exists: service should upsert (create if none, otherwise update)
            setting = settingService.save(setting);
            Notification n = Notification.show("Settings saved", 2500, Notification.Position.BOTTOM_START);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            // Reload from DB to ensure we display persisted state
            loadOrInit();
        } catch (Exception ex) {
            showError("Failed to save settings: " + ex.getMessage());
        }
    }

    private void showError(String msg) {
        Notification n = Notification.show(msg, 3000, Notification.Position.MIDDLE);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
