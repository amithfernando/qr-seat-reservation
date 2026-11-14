package com.qrseatreservation.ui.view;

import com.qrseatreservation.core.util.QrTicketGenerator;
import com.qrseatreservation.core.model.Setting;
import com.qrseatreservation.core.service.SettingService;
import com.qrseatreservation.core.service.TicketService;
import com.qrseatreservation.ui.view.TicketLayoutEditorView;
import com.qrseatreservation.ui.view.layout.MainLayout;
import com.qrseatreservation.ui.view.layout.PageLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
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
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.RolesAllowed;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Route(value="settings", layout = MainLayout.class)
@RolesAllowed({"ADMIN"})
@PageTitle("Settings")
public class SettingsView extends PageLayout {

    // JavaScript for drag-and-drop positioning on the ticket canvas
    private static final String DRAG_JS = """
        (function() {
          var canvas = this;
          var img = canvas.querySelector('[data-ticket-img]');
          var dragging = null, startX, startY, origLeft, origTop;
          function clamp(v, lo, hi) { return Math.max(lo, Math.min(hi, v)); }
          canvas.querySelectorAll('[data-draggable]').forEach(function(el) {
            el.addEventListener('mousedown', function(ev) {
              if (ev.button !== 0) return;
              ev.preventDefault(); ev.stopPropagation();
              dragging = el;
              startX = ev.clientX; startY = ev.clientY;
              origLeft = parseFloat(el.style.left) || 0;
              origTop  = parseFloat(el.style.top)  || 0;
            });
          });
          document.addEventListener('mousemove', function(ev) {
            if (!dragging || !img) return;
            var rect = img.getBoundingClientRect();
            if (rect.width === 0 || img.naturalWidth === 0) return;
            var dx = (ev.clientX - startX) / rect.width * 100;
            var dy = (ev.clientY - startY) / rect.height * 100;
            var nl = clamp(origLeft + dx, 0, 97);
            var nt = clamp(origTop  + dy, 0, 92);
            dragging.style.left = nl + '%';
            dragging.style.top  = nt + '%';
            var imgX = Math.round(nl / 100 * img.naturalWidth);
            var imgY = Math.round(nt / 100 * img.naturalHeight);
            dragging.dataset.imgX = imgX;
            dragging.dataset.imgY = imgY;
            var lbl = canvas.querySelector('[data-pos-for="' + dragging.dataset.draggable + '"]');
            if (lbl) lbl.textContent = imgX + ', ' + imgY;
          });
          document.addEventListener('mouseup', function() {
            if (!dragging) return;
            canvas.dispatchEvent(new CustomEvent('handle-drop', {
              bubbles: false,
              detail: {
                type: dragging.dataset.draggable,
                x: parseInt(dragging.dataset.imgX || 0),
                y: parseInt(dragging.dataset.imgY || 0)
              }
            }));
            dragging = null;
          });
        }).call(this);
        """;

    private final SettingService settingService;
    private final TicketService ticketService;
    private Setting setting;

    private final Binder<Setting> binder = new Binder<>(Setting.class);

    // General tab
    private final TextField eventName = new TextField("Event name");
    private final TextField venue     = new TextField("Venue");

    // Layout tab
    private final IntegerField tableSize   = new IntegerField("Table size");
    private final IntegerField seatSize    = new IntegerField("Seat size");
    private final IntegerField noOfColumns = new IntegerField("No. of columns");

    // Ticket Layout tab – size/border controls (visible in cards)
    private final IntegerField qrSize           = new IntegerField("QR size (scale)");
    private final IntegerField qrBorder         = new IntegerField("QR border");
    private final IntegerField fontSize          = new IntegerField("Ticket # font size");
    private final IntegerField tableSeatFontSize = new IntegerField("Table/Seat font size");

    // Position fields – hidden, updated by drag handler
    private final IntegerField qrX        = new IntegerField("QR X");
    private final IntegerField qrY        = new IntegerField("QR Y");
    private final IntegerField textX      = new IntegerField("Text X");
    private final IntegerField textY      = new IntegerField("Text Y");
    private final IntegerField tableSeatX = new IntegerField("Seat X");
    private final IntegerField tableSeatY = new IntegerField("Seat Y");

    // Live position labels (updated by JS on drag-move, confirmed on server drop)
    private final Span qrPosLabel   = new Span("-");
    private final Span textPosLabel = new Span("-");
    private final Span seatPosLabel = new Span("-");

    // Sample value fields for canvas preview
    private final TextField sampleTicketField = new TextField("Sample ticket #");
    private final TextField sampleTableField  = new TextField("Sample table");
    private final TextField sampleSeatField   = new TextField("Sample seat");
    private final Checkbox  standingTicket    = new Checkbox("Preview as Standing ticket");

    // Canvas elements
    private Div   canvasDiv;
    private final Image canvasImg = new Image();
    private Div   qrHandle, textHandle, seatHandle;

    // Tickets tab
    private final TextField    ticketPrefix   = new TextField("Ticket prefix");
    private final IntegerField noOfDigits     = new IntegerField("No. of digits");
    private final IntegerField maxNoOfTickets = new IntegerField("Max no. of tickets");

    // Image upload
    private final MemoryBuffer imageBuffer     = new MemoryBuffer();
    private final Upload       baseImageUpload = new Upload(imageBuffer);
    private byte[] uploadedBaseImage;

    // Action buttons
    private final Button saveBtn            = new Button("Save Layout");
    private final Button resetBtn           = new Button("Reset");
    private final Button generateTicketsBtn = new Button("Generate Tickets");
    private final Button regenerateBtn      = new Button("Regenerate Existing Tickets");
    private final Button previewTicketsBtn  = new Button("Preview Generated Tickets", VaadinIcon.EYE.create());
    private final Span   ticketSummary      = new Span();
    private Tabs tabs;
    private Tab generalTab, layoutTab, qrTab, ticketsTab;

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

    // -------------------------------------------------------------------------
    // Form construction
    // -------------------------------------------------------------------------

    private void buildForm() {
        // General tab inputs
        eventName.setRequiredIndicatorVisible(true);
        eventName.setClearButtonVisible(true);
        eventName.setWidthFull();
        venue.setClearButtonVisible(true);
        venue.setWidthFull();

        // Layout tab inputs
        for (IntegerField f : new IntegerField[]{tableSize, seatSize, noOfColumns}) {
            f.setStepButtonsVisible(true); f.setWidthFull();
        }
        tableSize.setMin(10); seatSize.setMin(4); noOfColumns.setMin(1);

        // Size/border controls for Ticket Layout tab
        for (IntegerField f : new IntegerField[]{qrSize, qrBorder, fontSize, tableSeatFontSize}) {
            f.setStepButtonsVisible(true); f.setWidthFull();
        }
        qrSize.setMin(1); qrSize.setMax(50);
        qrBorder.setMin(0); qrBorder.setMax(10);
        fontSize.setMin(6);
        tableSeatFontSize.setMin(6);

        // Hidden position fields (updated by drag, persisted via Binder)
        for (IntegerField f : new IntegerField[]{qrX, qrY, textX, textY, tableSeatX, tableSeatY}) {
            f.setVisible(false);
        }

        // Sample value defaults
        sampleTicketField.setValue("CSR00001");
        sampleTableField.setValue("T1");
        sampleSeatField.setValue("S1");
        sampleTicketField.setWidthFull();
        sampleTableField.setWidthFull();
        sampleSeatField.setWidthFull();

        sampleTicketField.addValueChangeListener(e -> {
            String v = e.getValue() == null || e.getValue().isBlank() ? "T-00001" : e.getValue();
            canvasDiv.getElement().executeJs(
                    "this.querySelectorAll('[data-draggable=\"text\"]').forEach(h => h.textContent=$0);", v);
        });
        sampleTableField.addValueChangeListener(e -> refreshSeatHandleLabel());
        sampleSeatField.addValueChangeListener(e -> refreshSeatHandleLabel());

        // Tickets tab inputs
        ticketPrefix.setClearButtonVisible(true);
        ticketPrefix.setPlaceholder("Optional (e.g., CSR)");
        ticketPrefix.setWidthFull();
        for (IntegerField f : new IntegerField[]{noOfDigits, maxNoOfTickets}) {
            f.setStepButtonsVisible(true); f.setWidthFull();
        }
        noOfDigits.setMin(1); maxNoOfTickets.setMin(0); maxNoOfTickets.setMax(600);

        // Upload config
        baseImageUpload.setAcceptedFileTypes("image/png", "image/jpeg", "image/jpg");
        baseImageUpload.setMaxFiles(1);
        baseImageUpload.setMaxFileSize(10 * 1024 * 1024);
        baseImageUpload.setDropAllowed(true);

        // --- Build visual ticket canvas ---
        canvasDiv = new Div();
        canvasDiv.getStyle()
                .set("position", "relative")
                .set("width", "100%")
                .set("user-select", "none")
                .set("background", "#1a1a1a")
                .set("border-radius", "8px")
                .set("overflow", "hidden")
                .set("min-height", "80px");

        canvasImg.getElement().setAttribute("data-ticket-img", "true");
        canvasImg.getStyle().set("width", "100%").set("display", "block");
        canvasImg.setAlt("Upload a base image to start");

        qrHandle   = makeDragHandle("qr",   "QR",               "3px solid #10b981", "#065f46", "rgba(255,255,255,0.92)");
        textHandle = makeDragHandle("text", "CSR00001",          "2px solid #3b82f6", "#1e3a5f", "rgba(255,255,255,0.92)");
        seatHandle = makeDragHandle("seat", "Table T1  Seat S1", "2px dashed #ef4444","#7f1d1d", "rgba(255,255,255,0.92)");
        qrHandle.getStyle().set("min-width","80px").set("min-height","80px")
                .set("display","flex").set("align-items","center").set("justify-content","center")
                .set("font-size","18px");

        canvasDiv.add(canvasImg, qrHandle, textHandle, seatHandle);

        // Attach drag JS and register server-side drop handler once attached to DOM
        canvasDiv.addAttachListener(ev -> {
            canvasDiv.getElement().executeJs(DRAG_JS);
            canvasDiv.getElement()
                    .addEventListener("handle-drop", e -> {
                        String type = e.getEventData().getString("event.detail.type");
                        int    x    = (int) e.getEventData().getNumber("event.detail.x");
                        int    y    = (int) e.getEventData().getNumber("event.detail.y");
                        applyDrop(type, x, y);
                    })
                    .addEventData("event.detail.type")
                    .addEventData("event.detail.x")
                    .addEventData("event.detail.y");
        });

        // --- Control cards ---
        Div qrCard   = makeCard("QR Code",
                cardContent(qrSize, qrBorder, posRow("QR position:", qrPosLabel, "qr")));
        Div textCard = makeCard("Ticket Number",
                cardContent(fontSize, posRow("Ticket # position:", textPosLabel, "text")));
        Div seatCard = makeCard("Table/Seat Label",
                cardContent(tableSeatFontSize, posRow("Table/Seat position:", seatPosLabel, "seat")));
        Div previewCard = makeCard("Preview sample values",
                cardContent(sampleTicketField, sampleTableField, sampleSeatField, standingTicket));

        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.setWidthFull();
        saveBtn.addClickListener(e -> onSave());

        regenerateBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        regenerateBtn.setIcon(VaadinIcon.REFRESH.create());
        regenerateBtn.setWidthFull();
        regenerateBtn.addClickListener(e -> onRegenerate());

        VerticalLayout actionsContent = new VerticalLayout(baseImageUpload, saveBtn, regenerateBtn);
        actionsContent.setPadding(false); actionsContent.setSpacing(true);
        Div actionsCard = makeCard("Base image & actions", actionsContent);

        HorizontalLayout controls = new HorizontalLayout(qrCard, textCard, seatCard, previewCard, actionsCard);
        controls.setWidthFull();
        controls.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.START);
        controls.getStyle().set("flex-wrap", "wrap");
        controls.setSpacing(true);

        Span hint = new Span("Drag the QR code, ticket number, and table/seat label directly onto the ticket. Positions save when you click \"Save Layout\".");
        hint.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "var(--lumo-font-size-s)");

        // --- Tabs ---
        generalTab = new Tab("General");
        layoutTab  = new Tab("Layout");
        qrTab      = new Tab("Ticket Layout");
        ticketsTab = new Tab("Tickets");
        tabs = new Tabs(generalTab, layoutTab, qrTab, ticketsTab);
        tabs.setWidthFull();

        Div generalPage = new Div(); generalPage.setWidthFull();
        Div layoutPage  = new Div(); layoutPage.setWidthFull();
        Div qrPage      = new Div(); qrPage.setWidthFull();
        Div ticketsPage = new Div(); ticketsPage.setWidthFull();

        // General form
        FormLayout generalForm = new FormLayout(eventName, venue);
        generalForm.setWidthFull();
        generalForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("600px", 2));
        generalForm.setColspan(eventName, 2); generalForm.setColspan(venue, 2);
        generalPage.add(generalForm);

        // Layout form
        FormLayout layoutForm = new FormLayout(tableSize, seatSize, noOfColumns);
        layoutForm.setWidthFull();
        layoutForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("600px", 3));
        layoutPage.add(layoutForm);

        // Ticket Layout tab — redirect to the dedicated editor (single source of truth)
        Span redirectMsg = new Span("Ticket layout settings (QR position, font sizes, text colours) are managed in the Ticket Layout Editor.");
        redirectMsg.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.9rem");
        Button goEditorBtn = new Button("Open Ticket Layout Editor", new com.vaadin.flow.component.icon.Icon(com.vaadin.flow.component.icon.VaadinIcon.TICKET));
        goEditorBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        goEditorBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(TicketLayoutEditorView.class)));
        VerticalLayout redirectCard = new VerticalLayout(redirectMsg, goEditorBtn);
        redirectCard.getStyle()
                .set("background", "var(--app-surface)").set("border", "1px solid var(--app-border)")
                .set("border-radius", "var(--app-radius)").set("padding", "var(--lumo-space-m)");
        redirectCard.setPadding(false); redirectCard.setSpacing(true); redirectCard.setWidthFull();
        qrPage.add(redirectCard);
        // Hidden fields still bound so save() doesn't corrupt existing layout values
        qrPage.add(qrX, qrY, textX, textY, tableSeatX, tableSeatY);
        qrX.setVisible(false); qrY.setVisible(false); textX.setVisible(false);
        textY.setVisible(false); tableSeatX.setVisible(false); tableSeatY.setVisible(false);

        // Tickets form
        FormLayout ticketsForm = new FormLayout(ticketPrefix, noOfDigits, maxNoOfTickets);
        ticketsForm.setWidthFull();
        ticketsForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("600px", 3));
        generateTicketsBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        generateTicketsBtn.addClickListener(e -> onGenerateTickets());
        previewTicketsBtn.addClickListener(e -> onPreviewTickets());
        ticketSummary.getStyle().set("font-size", "var(--lumo-font-size-s)").set("color", "var(--lumo-secondary-text-color)");
        HorizontalLayout ticketsActions = new HorizontalLayout(ticketSummary, previewTicketsBtn, generateTicketsBtn);
        ticketsActions.setWidthFull();
        ticketsActions.setAlignItems(FlexComponent.Alignment.CENTER);
        ticketsActions.expand(ticketSummary);
        ticketsPage.add(ticketsForm, ticketsActions);

        // Tab visibility switching
        Div pages = new Div(generalPage, layoutPage, qrPage, ticketsPage);
        pages.setWidthFull();
        generalPage.setVisible(true); layoutPage.setVisible(false);
        qrPage.setVisible(false);     ticketsPage.setVisible(false);
        tabs.addSelectedChangeListener(e -> {
            generalPage.setVisible(e.getSelectedTab() == generalTab);
            layoutPage.setVisible(e.getSelectedTab()  == layoutTab);
            qrPage.setVisible(e.getSelectedTab()      == qrTab);
            ticketsPage.setVisible(e.getSelectedTab() == ticketsTab);
            if (e.getSelectedTab() == ticketsTab) refreshTicketSummary();
        });

        // Bottom action bar
        resetBtn.addClickListener(e -> loadOrInit());
        HorizontalLayout actionsBar = new HorizontalLayout(resetBtn, saveBtn);
        actionsBar.setWidthFull();
        actionsBar.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);

        addToContent(tabs, pages, actionsBar);
    }

    // -------------------------------------------------------------------------
    // Canvas helpers
    // -------------------------------------------------------------------------

    private Div makeDragHandle(String type, String text, String border, String color, String bg) {
        Div h = new Div();
        h.getElement().setAttribute("data-draggable", type);
        h.getStyle()
                .set("position", "absolute").set("left", "70%").set("top", "10%")
                .set("border", border).set("background", bg).set("padding", "4px 10px")
                .set("cursor", "move").set("z-index", "10").set("font-weight", "bold")
                .set("color", color).set("white-space", "nowrap")
                .set("font-family", "Arial, sans-serif").set("font-size", "14px");
        h.setText(text);
        return h;
    }

    private Div makeCard(String title, Component content) {
        Span label = new Span(title);
        label.getStyle().set("font-weight","600").set("font-size","var(--lumo-font-size-s)")
                .set("color","var(--lumo-secondary-text-color)").set("text-transform","uppercase")
                .set("letter-spacing","0.05em");
        Div card = new Div(label, content);
        card.getStyle().set("background","var(--lumo-base-color)")
                .set("border","1px solid var(--lumo-contrast-10pct)").set("border-radius","8px")
                .set("padding","var(--lumo-space-m)").set("min-width","170px").set("flex","1");
        return card;
    }

    private VerticalLayout cardContent(Component... children) {
        VerticalLayout vl = new VerticalLayout(children);
        vl.setPadding(false); vl.setSpacing(true);
        return vl;
    }

    private HorizontalLayout posRow(String label, Span valueSpan, String handleType) {
        Span lbl = new Span(label);
        lbl.getStyle().set("font-size","var(--lumo-font-size-s)").set("color","var(--lumo-secondary-text-color)");
        valueSpan.getStyle().set("font-size","var(--lumo-font-size-s)").set("font-weight","600");
        valueSpan.getElement().setAttribute("data-pos-for", handleType);
        HorizontalLayout row = new HorizontalLayout(lbl, valueSpan);
        row.setSpacing(true); row.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        return row;
    }

    private void applyDrop(String type, int x, int y) {
        switch (type) {
            case "qr"   -> { qrX.setValue(x);        qrY.setValue(y);        qrPosLabel.setText(x + ", " + y); }
            case "text" -> { textX.setValue(x);      textY.setValue(y);      textPosLabel.setText(x + ", " + y); }
            case "seat" -> { tableSeatX.setValue(x); tableSeatY.setValue(y); seatPosLabel.setText(x + ", " + y); }
        }
    }

    /** Renders a real QR code into the QR drag handle, sized proportionally to the base image. */
    private void refreshQrPreview() {
        String ticketNo = sampleTicketField.getValue();
        if (ticketNo == null || ticketNo.isBlank()) ticketNo = "CSR00001";
        int scale  = qrSize.getValue()  != null ? qrSize.getValue()  : 13;
        int border = qrBorder.getValue() != null ? qrBorder.getValue() : 1;
        try {
            byte[] qrBytes = QrTicketGenerator.generateQrImageBytes(ticketNo, scale, border);
            BufferedImage qrImg = ImageIO.read(new ByteArrayInputStream(qrBytes));
            int qrPx = qrImg != null ? qrImg.getWidth() : 100;

            int imgW = 1600;
            if (setting != null && setting.getBaseImage() != null && setting.getBaseImage().length > 0) {
                try {
                    BufferedImage bi = ImageIO.read(new ByteArrayInputStream(setting.getBaseImage()));
                    if (bi != null) imgW = bi.getWidth();
                } catch (IOException ignored) {}
            }
            double pct = Math.max(5, Math.min(40, qrPx * 100.0 / imgW));

            String dataUrl = "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(qrBytes);
            canvasDiv.getElement().executeJs("""
                    var h = this.querySelector('[data-draggable="qr"]');
                    if (!h) return;
                    h.innerHTML = '';
                    var img = document.createElement('img');
                    img.src = $0;
                    img.style.cssText = 'width:100%;height:100%;display:block;';
                    h.appendChild(img);
                    h.style.width  = $1 + '%';
                    h.style.height = '';
                    h.style.minWidth  = '';
                    h.style.minHeight = '';
                    """, dataUrl, pct);
        } catch (Exception ignored) {}
    }

    private void refreshSeatHandleLabel() {
        String t = sampleTableField.getValue() == null ? "T1" : sampleTableField.getValue();
        String s = sampleSeatField.getValue()  == null ? "S1" : sampleSeatField.getValue();
        canvasDiv.getElement().executeJs(
                "this.querySelectorAll('[data-draggable=\"seat\"]').forEach(h => h.textContent=$0);",
                "Table " + t + "  Seat " + s);
    }

    private void refreshCanvasPositions() {
        if (setting == null || setting.getBaseImage() == null || setting.getBaseImage().length == 0) return;
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(setting.getBaseImage()));
            if (img == null) return;
            int w = img.getWidth(), h = img.getHeight();
            double qxP = w > 0 ? setting.getQrX()   * 100.0 / w : 70;
            double qyP = h > 0 ? setting.getQrY()   * 100.0 / h : 10;
            double txP = w > 0 ? setting.getTextX() * 100.0 / w : 75;
            double tyP = h > 0 ? setting.getTextY() * 100.0 / h :  5;
            Integer sxR = setting.getTableSeatX(), syR = setting.getTableSeatY();
            double  sxP = (sxR != null && w > 0) ? sxR * 100.0 / w : 72;
            double  syP = (syR != null && h > 0) ? syR * 100.0 / h : 80;
            canvasDiv.getElement().executeJs("""
                    var hs = this.querySelectorAll('[data-draggable]');
                    hs.forEach(function(h) {
                      var t = h.dataset.draggable;
                      if (t==='qr')   { h.style.left=$0+'%'; h.style.top=$1+'%'; }
                      if (t==='text') { h.style.left=$2+'%'; h.style.top=$3+'%'; }
                      if (t==='seat') { h.style.left=$4+'%'; h.style.top=$5+'%'; }
                    });
                    """, qxP, qyP, txP, tyP, sxP, syP);
        } catch (IOException ignored) {}
    }

    // -------------------------------------------------------------------------
    // Binder
    // -------------------------------------------------------------------------

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
        binder.forField(qrSize)
                .withValidator(new IntegerRangeValidator("QR size must be between 1 and 50", 1, 50))
                .bind(Setting::getQrSize, Setting::setQrSize);
        binder.forField(qrBorder)
                .withValidator(new IntegerRangeValidator("QR border must be between 0 and 10", 0, 10))
                .bind(Setting::getQrBorder, Setting::setQrBorder);
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
        binder.forField(tableSeatFontSize)
                .withNullRepresentation(24)
                .withValidator(new IntegerRangeValidator("Font size must be >= 6", 6, Integer.MAX_VALUE))
                .bind(Setting::getTableSeatFontSize, Setting::setTableSeatFontSize);
        binder.forField(tableSeatX)
                .withNullRepresentation(0)
                .withValidator(new IntegerRangeValidator("Seat X must be >= 0", 0, Integer.MAX_VALUE))
                .bind(Setting::getTableSeatX, Setting::setTableSeatX);
        binder.forField(tableSeatY)
                .withNullRepresentation(0)
                .withValidator(new IntegerRangeValidator("Seat Y must be >= 0", 0, Integer.MAX_VALUE))
                .bind(Setting::getTableSeatY, Setting::setTableSeatY);
        binder.forField(ticketPrefix)
                .withValidator(new StringLengthValidator("Max 32 characters", 0, 32))
                .bind(Setting::getTicketPrefix, Setting::setTicketPrefix);
        binder.forField(noOfDigits)
                .withValidator(new IntegerRangeValidator("Digits must be >= 1", 1, Integer.MAX_VALUE))
                .bind(Setting::getNoOfDigits, Setting::setNoOfDigits);
        binder.forField(maxNoOfTickets)
                .withValidator(new IntegerRangeValidator("Must be between 0 and 600", 0, 600))
                .bind(Setting::getMaxNoOfTickets, Setting::setMaxNoOfTickets);
    }

    // -------------------------------------------------------------------------
    // Upload
    // -------------------------------------------------------------------------

    private void configureUpload() {
        baseImageUpload.addSucceededListener(e -> {
            String mime = e.getMIMEType() == null ? "" : e.getMIMEType().toLowerCase();
            if (!(mime.contains("png") || mime.contains("jpeg") || mime.contains("jpg"))) {
                showError("Unsupported image type. Please upload PNG or JPEG.");
                return;
            }
            try (InputStream in = imageBuffer.getInputStream()) {
                byte[] data = in.readAllBytes();
                if (data.length == 0) { showError("Empty image."); return; }
                uploadedBaseImage = data;
                if (setting != null) setting.setBaseImage(uploadedBaseImage);
                StreamResource res = new StreamResource(e.getFileName(), () -> new ByteArrayInputStream(uploadedBaseImage));
                canvasImg.setSrc(res);
            } catch (IOException ex) {
                showError("Failed to read uploaded file: " + ex.getMessage());
            }
        });
        baseImageUpload.addFileRejectedListener(e -> showError(e.getErrorMessage()));
        baseImageUpload.addFailedListener(e -> showError("Upload failed"));
    }

    // -------------------------------------------------------------------------
    // Load / Save
    // -------------------------------------------------------------------------

    private void loadOrInit() {
        try {
            setting = settingService.getSetting();
            if (setting == null) {
                setting = new Setting();
                setting.setTableSize(100);  setting.setSeatSize(24);    setting.setNoOfColumns(3);
                setting.setFontSize(20);    setting.setQrBorder(1);     setting.setQrSize(13);
                setting.setQrX(50);         setting.setQrY(50);
                setting.setTextX(50);       setting.setTextY(120);
                setting.setTableSeatFontSize(24); setting.setTableSeatX(50); setting.setTableSeatY(200);
                setting.setNoOfDigits(5);   setting.setMaxNoOfTickets(0);
            }
            clampPositions(setting);
            binder.readBean(setting);

            qrPosLabel.setText(setting.getQrX() + ", " + setting.getQrY());
            textPosLabel.setText(setting.getTextX() + ", " + setting.getTextY());
            Integer sx = setting.getTableSeatX(), sy = setting.getTableSeatY();
            seatPosLabel.setText((sx != null ? sx : 0) + ", " + (sy != null ? sy : 0));

            uploadedBaseImage = setting.getBaseImage();
            if (uploadedBaseImage != null && uploadedBaseImage.length > 0) {
                StreamResource res = new StreamResource("base-image", () -> new ByteArrayInputStream(uploadedBaseImage));
                canvasImg.setSrc(res);
            }
            refreshCanvasPositions();
            refreshQrPreview();
        } catch (Exception ex) {
            showError("Failed to load settings: " + ex.getMessage());
            if (setting == null) { setting = new Setting(); binder.readBean(setting); }
        }
    }

    private void onSave() {
        if (saveSettings()) {
            Notification n = Notification.show("Settings saved", 2500, Notification.Position.BOTTOM_START);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            loadOrInit();
        }
    }

    /** Validates and saves the form. On failure, names the invalid fields and switches to their tab. */
    private boolean saveSettings() {
        try { binder.writeBean(setting); }
        catch (ValidationException ve) {
            java.util.List<String> msgs = new java.util.ArrayList<>();
            Tab firstTab = null;
            for (var err : ve.getFieldValidationErrors()) {
                Component field = (Component) err.getField();
                String label = field instanceof com.vaadin.flow.component.HasLabel hl && hl.getLabel() != null ? hl.getLabel() : "Field";
                msgs.add(label + ": " + err.getMessage().orElse("invalid"));
                if (firstTab == null) firstTab = tabFor(field);
            }
            if (firstTab != null) tabs.setSelectedTab(firstTab);
            showError("Please fix: " + String.join("; ", msgs));
            return false;
        }
        if (uploadedBaseImage != null && uploadedBaseImage.length > 0)
            setting.setBaseImage(uploadedBaseImage);
        try {
            setting = settingService.save(setting);
            return true;
        } catch (Exception ex) {
            showError("Failed to save: " + ex.getMessage());
            return false;
        }
    }

    private Tab tabFor(Component field) {
        if (field == eventName || field == venue) return generalTab;
        if (field == tableSize || field == seatSize || field == noOfColumns) return layoutTab;
        if (field == ticketPrefix || field == noOfDigits || field == maxNoOfTickets) return ticketsTab;
        return qrTab;
    }

    private static void clampPositions(Setting s) {
        s.setQrX(Math.max(0, s.getQrX()));
        s.setQrY(Math.max(0, s.getQrY()));
        s.setTextX(Math.max(0, s.getTextX()));
        s.setTextY(Math.max(0, s.getTextY()));
        if (s.getTableSeatX() != null) s.setTableSeatX(Math.max(0, s.getTableSeatX()));
        if (s.getTableSeatY() != null) s.setTableSeatY(Math.max(0, s.getTableSeatY()));
    }

    private void refreshTicketSummary() {
        try {
            long total = ticketService.countTickets();
            long available = ticketService.countTicketsByStatus(com.qrseatreservation.core.enums.TicketStatus.AVAILABLE);
            int oldFormat = ticketService.findUnusedTicketsWithOldFormat().size();
            String text = total + " tickets (" + available + " available, " + (total - available) + " used)";
            if (oldFormat > 0) text += " — " + oldFormat + " unused ticket(s) have an old number format";
            ticketSummary.setText(text);
        } catch (Exception ex) {
            ticketSummary.setText("");
        }
    }

    /** Saves the settings first so generation always uses the prefix/digits shown on screen. */
    private void onGenerateTickets() {
        if (!saveSettings()) return;
        java.util.List<String> oldFormat = ticketService.findUnusedTicketsWithOldFormat();
        if (oldFormat.isEmpty()) { doGenerateTickets(false); return; }

        String expected = (setting.getTicketPrefix() != null ? setting.getTicketPrefix() : "")
                + "#".repeat(Math.max(1, setting.getNoOfDigits() != null ? setting.getNoOfDigits() : 1));
        ConfirmDialog dlg = new ConfirmDialog();
        dlg.setHeader("Replace old-format tickets?");
        dlg.setText(oldFormat.size() + " unused ticket(s) don't match the current format " + expected
                + " (e.g. " + String.join(", ", oldFormat.subList(0, Math.min(3, oldFormat.size()))) + ")."
                + " Replace them with new " + expected + " tickets? Tickets already assigned to reservations are never changed.");
        dlg.setConfirmText("Replace");
        dlg.setRejectable(true);
        dlg.setRejectText("Keep them");
        dlg.setCancelable(true);
        dlg.addConfirmListener(e -> doGenerateTickets(true));
        dlg.addRejectListener(e -> doGenerateTickets(false));
        dlg.open();
    }

    private void doGenerateTickets(boolean replaceOldFormat) {
        generateTicketsBtn.setEnabled(false);
        try {
            int removed = replaceOldFormat ? ticketService.deleteUnusedTicketsWithOldFormat() : 0;
            ticketService.generateTicketNos();
            Notification n = Notification.show("Tickets generated" + (removed > 0 ? " (" + removed + " old-format tickets replaced)" : ""),
                    3500, Notification.Position.BOTTOM_START);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception ex) {
            showError("Failed to generate tickets: " + ex.getMessage());
        } finally {
            generateTicketsBtn.setEnabled(true);
            refreshTicketSummary();
        }
    }

    private void onPreviewTickets() {
        java.util.List<com.qrseatreservation.ui.view.component.TicketPreviewDialog.Item> items =
                ticketService.findFirstTickets(20).stream()
                        .filter(t -> t.getData() != null)
                        .map(t -> new com.qrseatreservation.ui.view.component.TicketPreviewDialog.Item(t.getTicketNo(), t.getData()))
                        .toList();
        com.qrseatreservation.ui.view.component.TicketPreviewDialog.open("Generated tickets (first " + items.size() + ")", items);
    }

    private void onRegenerate() {
        ConfirmDialog dlg = new ConfirmDialog();
        dlg.setHeader("Regenerate all tickets?");
        dlg.setText("This re-renders the image of every existing ticket using the current layout. " +
                "Ticket numbers and status are left unchanged. This may take a while if there are many tickets.");
        dlg.setConfirmText("Regenerate");
        dlg.setConfirmButtonTheme("error primary");
        dlg.setCancelable(true);
        dlg.addConfirmListener(e -> doRegenerate());
        dlg.open();
    }

    private void doRegenerate() {
        regenerateBtn.setEnabled(false);
        try {
            int count = ticketService.regenerateAllTicketImages();
            Notification n = Notification.show("Regenerated " + count + " ticket(s) with the new layout", 2500, Notification.Position.BOTTOM_START);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception ex) {
            showError("Failed to regenerate: " + ex.getMessage());
        } finally {
            regenerateBtn.setEnabled(true);
        }
    }

    private void showError(String msg) {
        Notification n = Notification.show(msg, 3500, Notification.Position.MIDDLE);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
