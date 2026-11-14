package com.qrseatreservation.ui.view;

import com.qrseatreservation.core.model.Setting;
import com.qrseatreservation.core.service.SettingService;
import com.qrseatreservation.core.service.TicketService;
import com.qrseatreservation.core.util.QrTicketGenerator;
import com.qrseatreservation.ui.view.component.TicketPreviewDialog;
import com.qrseatreservation.ui.view.layout.MainLayout;
import com.qrseatreservation.ui.view.layout.PageLayout;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.html.Span;
import java.util.concurrent.atomic.AtomicReference;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import elemental.json.JsonObject;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Route(value = "ticket-layout-editor", layout = MainLayout.class)
@RolesAllowed({"ADMIN"})
@PageTitle("Ticket Layout Editor")
@Slf4j
public class TicketLayoutEditorView extends PageLayout {

    // ── JS constants ────────────────────────────────────────────────────────

    /** Setup call: large data URLs, sent only when images change. */
    private static final String SETUP_JS = """
        window.__tktBg = new Image();
        window.__tktBg.src = 'data:image/png;base64,' + $0;
        window.__tktQr = new Image();
        window.__tktQr.src = 'data:image/png;base64,' + $1;
        """;

    /** Text images rendered server-side by the same code that renders the real ticket (small, sent on change). */
    private static final String TEXT_JS = """
        window.__tktTx = new Image();
        window.__tktTx.src = 'data:image/png;base64,' + $0;
        window.__tktSx = new Image();
        window.__tktSx.src = 'data:image/png;base64,' + $1;
        """;

    /**
     * Draw call: small JSON params, constant JS code ~3 KB.
     * Sets up a persistent per-canvas controller (once) that owns the live element positions
     * ("state.pos") separately from the authoritative settings snapshot ("state.p"). Every element
     * position — rendered image/text placement, drag hit-boxes, and outline drawing — is read from
     * the single "state.pos" source, so a live drag visibly follows the cursor and a later hit-test
     * always agrees with what's on screen. Subsequent calls just push a fresh authoritative sync via
     * setP() instead of re-declaring the whole canvas + listeners.
     */
    private static final String DRAW_JS = """
        (function(canvas) {
          const p = JSON.parse($0);

          function ensureController() {
            if (canvas.__ctl) return canvas.__ctl;

            const state = { p: null, pos: null };

            function boxes() {
              return [
                {type:'qr',        x:state.pos.qr.x,        y:state.pos.qr.y,        w:state.p.qrPx, h:state.p.qrPx,      color:'#4f46e5', label:'QR Code'},
                {type:'ticketNo',  x:state.pos.ticketNo.x,  y:state.pos.ticketNo.y,  w:state.p.txW,  h:state.p.txH,  color:'#0ea5e9', label:'Ticket #'},
                {type:'tableSeat', x:state.pos.tableSeat.x, y:state.pos.tableSeat.y, w:state.p.sxW,  h:state.p.sxH,  color:'#10b981', label:'Table/Seat'},
              ];
            }

            function doRender() {
              // Always read the latest images; they are replaced whenever size/text/colour changes
              const bg = window.__tktBg, qrImg = window.__tktQr, txImg = window.__tktTx, sxImg = window.__tktSx;
              if (!state.p || !bg || !bg.complete || !bg.naturalWidth) return;
              // Zoom: 'fit' = whole ticket visible (width and height), 'width' = fit width, '1' = actual pixels
              const wrap = canvas.parentElement;
              const maxW = Math.max(200, (wrap ? wrap.clientWidth : 800) - 24);
              const top = wrap ? Math.max(0, wrap.getBoundingClientRect().top) : 0;
              // Phones: the bottom tab bar covers the lower ~72px of the viewport
              const tabBar = (window.innerWidth <= 800 && matchMedia('(pointer: coarse)').matches) ? 72 : 0;
              const maxH = Math.max(260, window.innerHeight - Math.min(top, window.innerHeight * 0.35) - 32 - tabBar);
              const zoom = canvas.dataset.zoom || 'fit';
              const sc = zoom === '1' ? 1.0
                       : zoom === 'width' ? Math.min(1.0, maxW / bg.naturalWidth)
                       : Math.min(1.0, maxW / bg.naturalWidth, maxH / bg.naturalHeight);
              canvas.width  = Math.round(bg.naturalWidth  * sc);
              canvas.height = Math.round(bg.naturalHeight * sc);
              canvas.dataset.sc = sc;
              const ctx = canvas.getContext('2d');
              ctx.clearRect(0, 0, canvas.width, canvas.height);

              // Background
              ctx.drawImage(bg, 0, 0, canvas.width, canvas.height);

              // QR image, exactly as the server draws it (no extra backing)
              const qx=state.pos.qr.x*sc, qy=state.pos.qr.y*sc, qw=state.p.qrPx*sc;
              if (qrImg.complete) ctx.drawImage(qrImg, qx, qy, qw, qw);

              // Ticket number + table/seat text: server-rendered images, so they match the final ticket pixel-for-pixel
              if (txImg && txImg.complete)
                ctx.drawImage(txImg, state.pos.ticketNo.x*sc, state.pos.ticketNo.y*sc, state.p.txW*sc, state.p.txH*sc);
              if (state.p.showSeat && sxImg && sxImg.complete)
                ctx.drawImage(sxImg, state.pos.tableSeat.x*sc, state.pos.tableSeat.y*sc, state.p.sxW*sc, state.p.sxH*sc);

              // Drag handles
              for (const el of boxes()) {
                ctx.strokeStyle = (canvas.__hover===el.type) ? '#ef4444' : el.color;
                ctx.lineWidth   = (canvas.__hover===el.type) ? 2.5 : 1.5;
                ctx.setLineDash([5,3]); ctx.strokeRect(el.x*sc,el.y*sc,el.w*sc,el.h*sc); ctx.setLineDash([]);
                ctx.fillStyle = el.color; ctx.font='bold 10px Arial';
                ctx.textAlign='left'; ctx.textBaseline='top';
                ctx.fillText(el.label, el.x*sc+3, el.y*sc+2);
              }
            }

            function hitTest(mx, my) {
              const s = parseFloat(canvas.dataset.sc)||1;
              const bs = boxes();
              for (let i=bs.length-1;i>=0;i--) {
                const e=bs[i];
                if (mx>=e.x*s && mx<=(e.x+e.w)*s && my>=e.y*s && my<=(e.y+e.h)*s) return e;
              }
              return null;
            }

            function evPos(e) {
              const r=canvas.getBoundingClientRect(); const src=e.touches?e.touches[0]:e;
              // Map displayed CSS pixels to canvas pixels, in case the browser scales the element
              const kx = r.width ? canvas.width / r.width : 1, ky = r.height ? canvas.height / r.height : 1;
              return {x:(src.clientX-r.left)*kx, y:(src.clientY-r.top)*ky};
            }

            function snapped(nx, ny) {
              if (state.p && state.p.snap) {
                const g = state.p.snapPx || 5;
                return {x: Math.round(nx/g)*g, y: Math.round(ny/g)*g};
              }
              return {x: nx, y: ny};
            }

            let drag=null, ox=0, oy=0;

            function startDrag(e) {
              const pt=evPos(e); const h=hitTest(pt.x,pt.y);
              if(h){ drag=h; const s=parseFloat(canvas.dataset.sc)||1; ox=pt.x-h.x*s; oy=pt.y-h.y*s; e.preventDefault(); }
            }
            function moveDrag(e) {
              if(!drag) return;
              const s=parseFloat(canvas.dataset.sc)||1; const pt=evPos(e);
              const n = snapped(Math.round((pt.x-ox)/s), Math.round((pt.y-oy)/s));
              // Keep elements fully inside the ticket so positions are always valid
              const bg = window.__tktBg;
              n.x = Math.max(0, Math.min(n.x, bg.naturalWidth  - drag.w));
              n.y = Math.max(0, Math.min(n.y, bg.naturalHeight - drag.h));
              state.pos[drag.type] = n;
              doRender();
              if (e.cancelable) e.preventDefault();
            }
            function onEnd() {
              if(!drag) return;
              const n = state.pos[drag.type];
              canvas.dispatchEvent(new CustomEvent('element-moved',{bubbles:true,detail:{type:drag.type,x:n.x,y:n.y}}));
              drag=null; canvas.style.cursor='default';
            }

            canvas.addEventListener('mousedown', startDrag);
            canvas.addEventListener('mousemove', e=>{
              if (drag) { moveDrag(e); }
              else { const pt=evPos(e); const h=hitTest(pt.x,pt.y); canvas.__hover=h?h.type:null; canvas.style.cursor=h?'move':'default'; }
            });
            canvas.addEventListener('mouseup',   onEnd);
            canvas.addEventListener('mouseleave',()=>{ if(drag) onEnd(); });
            canvas.addEventListener('touchstart',startDrag,{passive:false});
            canvas.addEventListener('touchmove', moveDrag,  {passive:false});
            canvas.addEventListener('touchend',  onEnd);

            let resizeTimer = null;
            window.addEventListener('resize', () => {
              clearTimeout(resizeTimer);
              resizeTimer = setTimeout(() => { if (canvas.isConnected) doRender(); }, 150);
            });

            canvas.__ctl = {
              render: doRender,
              setP(newP) {
                state.p = newP;
                state.pos = {
                  qr:        {x:newP.qrX, y:newP.qrY},
                  ticketNo:  {x:newP.txX, y:newP.txY},
                  tableSeat: {x:newP.sxX, y:newP.sxY},
                };
                for (const im of [window.__tktBg, window.__tktQr, window.__tktTx, window.__tktSx]) {
                  if (im && !im.complete) im.addEventListener('load', doRender, {once:true});
                }
                doRender();
              }
            };
            return canvas.__ctl;
          }

          ensureController().setP(p);
        })(this);
        """;

    // ── Fields ───────────────────────────────────────────────────────────────
    private final SettingService settingService;
    private final TicketService  ticketService;

    private Setting setting;
    private TicketCanvas ticketCanvas;

    // Caches — avoid resending large data URLs
    private byte[] lastBgBytes = null;
    private String lastQrTicketNo = null;
    private int    lastQrSize = -1;
    private int    lastQrBorder = -1;
    private String lastTextKey = null;
    private int txW, txH, sxW, sxH;

    // Side-panel controls
    private final IntegerField qrSizeField      = new IntegerField("QR size (scale)");
    private final IntegerField qrBorderField    = new IntegerField("QR border (quiet zone)");
    private final IntegerField fontSizeField     = new IntegerField("Ticket# font size");
    private final IntegerField seatFontField     = new IntegerField("Table/Seat font size");
    private final TextField    ticketNoColorField = new TextField("Ticket# color");
    private final TextField    tableSeatColorField = new TextField("Table/Seat color");
    private final TextField    sampleTicketField = new TextField("Sample ticket #");
    private final TextField    sampleTableField  = new TextField("Sample table");
    private final TextField    sampleSeatField   = new TextField("Sample seat");
    private final Checkbox     standingChk       = new Checkbox("Preview as Standing ticket");
    private final Checkbox     snapChk           = new Checkbox("Snap to grid (5px)");
    private final Button       regenBtn          = new Button("Regenerate All Tickets", VaadinIcon.REFRESH.create());
    private final Button       previewBtn        = new Button("Preview Real Ticket", VaadinIcon.EYE.create());

    // Position readouts
    private final Span qrPosSpan   = coordSpan();
    private final Span txtPosSpan  = coordSpan();
    private final Span seatPosSpan = coordSpan();

    private static Span coordSpan() {
        Span s = new Span();
        s.getStyle().set("font-size","0.78rem").set("color","var(--lumo-secondary-text-color)");
        return s;
    }

    public TicketLayoutEditorView(SettingService settingService, TicketService ticketService) {
        super(VaadinIcon.TICKET, "Ticket Layout Editor");
        this.settingService = settingService;
        this.ticketService  = ticketService;

        try {
            build();
        } catch (Exception ex) {
            log.error("TicketLayoutEditorView load failed", ex);
            Span err = new Span("Could not load — database unavailable. Please refresh.");
            err.getStyle().set("color", "var(--lumo-error-text-color)");
            Button retry = new Button("Retry", VaadinIcon.REFRESH.create());
            retry.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(TicketLayoutEditorView.class)));
            addToContent(err, retry);
        }
    }

    private void build() {
        setting = settingService.getSetting();

        if (setting.getBaseImage() == null || setting.getBaseImage().length == 0) {
            Span msg = new Span("No ticket background image found. Upload one in Management → Settings first, or use the upload below.");
            msg.getStyle().set("color", "var(--lumo-secondary-text-color)");
            addToContent(msg);
            addToContent(buildActionsCard());
            return;
        }

        // ── Canvas ──────────────────────────────────────────────────────────
        ticketCanvas = new TicketCanvas();
        ticketCanvas.setOnAttachCallback(() -> refreshCanvas(true));
        ticketCanvas.getStyle().set("display","block").set("cursor","default").set("margin","0 auto")
                .set("max-width","none").set("touch-action","none")
                .set("border-radius","8px").set("box-shadow","0 8px 24px rgba(0,0,0,0.35)");
        ticketCanvas.getElement().setAttribute("data-zoom", "fit");

        ticketCanvas.getElement()
                .addEventListener("element-moved", ev -> {
                    JsonObject d = (JsonObject) ev.getEventData();
                    onElementMoved(d.getString("event.detail.type"),
                                   (int)d.getNumber("event.detail.x"),
                                   (int)d.getNumber("event.detail.y"));
                })
                .addEventData("event.detail.type")
                .addEventData("event.detail.x")
                .addEventData("event.detail.y");

        Div canvasWrapper = new Div(ticketCanvas);
        canvasWrapper.addClassName("ticket-stage");

        Span hint = new Span("Drag the QR code, ticket number, and table/seat label onto the ticket. Positions save when you drop them; "
                + "the text shown is rendered by the same engine as the final ticket. Use \"Preview Real Ticket\" to check the exact output.");
        hint.getStyle().set("color","var(--lumo-secondary-text-color)").set("font-size","0.8rem");

        snapChk.addValueChangeListener(e -> refreshCanvas(false));
        previewBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        previewBtn.addClickListener(e -> openRealPreview());
        Select<String> zoom = new Select<>();
        zoom.setItems("fit", "width", "1");
        zoom.setItemLabelGenerator(z -> switch (z) {
            case "width" -> "Fit width";
            case "1" -> "100% (actual size)";
            default -> "Fit screen";
        });
        zoom.setValue("fit");
        zoom.setWidth("180px");
        zoom.setAriaLabel("Zoom");
        zoom.addValueChangeListener(e -> ticketCanvas.getElement().executeJs(
                "this.dataset.zoom=$0; if (this.__ctl) this.__ctl.render();", e.getValue()));

        Div controls = new Div(zoom, previewBtn, snapChk);
        controls.addClassName("editor-controls");
        Div toolbar = new Div(hint, controls);
        toolbar.addClassName("editor-toolbar");

        // ── Cards row ────────────────────────────────────────────────────────
        HorizontalLayout cardsRow = new HorizontalLayout(
                buildQrCard(), buildTicketNoCard(), buildTableSeatCard(),
                buildPreviewCard(), buildActionsCard());
        cardsRow.setWidthFull();
        cardsRow.setSpacing(true);
        cardsRow.setAlignItems(FlexComponent.Alignment.START);
        cardsRow.getStyle().set("flex-wrap","wrap");

        addToContent(toolbar, canvasWrapper, cardsRow);

        // default sample values
        // Use prefix from Settings so the sample number looks like a real ticket
        String prefix = (setting.getTicketPrefix() != null) ? setting.getTicketPrefix() : "";
        int digits = (setting.getNoOfDigits() != null && setting.getNoOfDigits() > 0) ? setting.getNoOfDigits() : 6;
        sampleTicketField.setValue(setting.getSampleTicketNo() != null ? setting.getSampleTicketNo() : prefix + String.format("%0" + digits + "d", 1));
        sampleTableField.setValue(setting.getSampleTable() != null ? setting.getSampleTable() : "");
        sampleSeatField.setValue(setting.getSampleSeat() != null ? setting.getSampleSeat() : "");

        updateCoordLabels();
    }

    // ── Cards ────────────────────────────────────────────────────────────────

    private VerticalLayout buildQrCard() {
        qrSizeField.setMin(1); qrSizeField.setMax(50); qrSizeField.setStepButtonsVisible(true); qrSizeField.setWidthFull();
        qrSizeField.setValue(setting.getQrSize() > 0 ? setting.getQrSize() : 13);
        qrSizeField.addValueChangeListener(e -> refreshCanvas(false));
        qrBorderField.setMin(0); qrBorderField.setMax(10); qrBorderField.setStepButtonsVisible(true); qrBorderField.setWidthFull();
        qrBorderField.setValue(setting.getQrBorder() >= 0 ? setting.getQrBorder() : 1);
        qrBorderField.addValueChangeListener(e -> refreshCanvas(false));
        return card("QR Code", "card-indigo", qrPosSpan, qrSizeField, qrBorderField);
    }

    private VerticalLayout buildTicketNoCard() {
        fontSizeField.setMin(6); fontSizeField.setStepButtonsVisible(true); fontSizeField.setWidthFull();
        fontSizeField.setValue(setting.getFontSize() > 0 ? setting.getFontSize() : 20);
        fontSizeField.addValueChangeListener(e -> refreshCanvas(false));
        ticketNoColorField.setWidthFull();
        ticketNoColorField.setPlaceholder("#000000");
        ticketNoColorField.setValue(setting.getTicketNoColor() != null ? setting.getTicketNoColor() : "#000000");
        ticketNoColorField.addValueChangeListener(e -> refreshCanvas(false));
        return card("Ticket Number", "card-sky", txtPosSpan, fontSizeField, ticketNoColorField, matchToQrButtons("ticketNo"));
    }

    private VerticalLayout buildTableSeatCard() {
        seatFontField.setMin(6); seatFontField.setStepButtonsVisible(true); seatFontField.setWidthFull();
        int sf = setting.getTableSeatFontSize() != null ? setting.getTableSeatFontSize() : 24;
        seatFontField.setValue(sf);
        seatFontField.addValueChangeListener(e -> refreshCanvas(false));
        tableSeatColorField.setWidthFull();
        tableSeatColorField.setPlaceholder("#000000");
        tableSeatColorField.setValue(setting.getTableSeatColor() != null ? setting.getTableSeatColor() : "#000000");
        tableSeatColorField.addValueChangeListener(e -> refreshCanvas(false));
        return card("Table/Seat Label", "card-emerald", seatPosSpan, seatFontField, tableSeatColorField, matchToQrButtons("tableSeat"));
    }

    /** Normalizes a user-entered color to "#rrggbb", falling back to black if blank/invalid. */
    private static String normalizeHexColor(String value) {
        if (value == null || value.isBlank()) return "#000000";
        String v = value.trim();
        if (!v.startsWith("#")) v = "#" + v;
        return v.matches("^#[0-9a-fA-F]{6}$") ? v : "#000000";
    }

    /** "Match X" / "Match Y" buttons that align the given element to the QR code's position on that axis. */
    private HorizontalLayout matchToQrButtons(String targetType) {
        Button matchX = new Button("Match X to QR");
        matchX.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        matchX.addClickListener(e -> matchToQr(targetType, true));

        Button matchY = new Button("Match Y to QR");
        matchY.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        matchY.addClickListener(e -> matchToQr(targetType, false));

        HorizontalLayout row = new HorizontalLayout(matchX, matchY);
        row.setSpacing(true);
        return row;
    }

    private void matchToQr(String targetType, boolean matchX) {
        setting = settingService.getSetting();
        if (matchX) {
            int qrX = setting.getQrX();
            if ("ticketNo".equals(targetType)) setting.setTextX(qrX); else setting.setTableSeatX(qrX);
        } else {
            int qrY = setting.getQrY();
            if ("ticketNo".equals(targetType)) setting.setTextY(qrY); else setting.setTableSeatY(qrY);
        }
        settingService.save(setting);
        updateCoordLabels();
        refreshCanvas(false);
    }

    private VerticalLayout buildPreviewCard() {
        sampleTicketField.setWidthFull(); sampleTableField.setWidthFull(); sampleSeatField.setWidthFull();
        // Redraw canvas when sample values change
        sampleTicketField.addValueChangeListener(e -> refreshCanvas(false));
        sampleTableField.addValueChangeListener(e -> refreshCanvas(false));
        sampleSeatField.addValueChangeListener(e -> refreshCanvas(false));
        standingChk.addValueChangeListener(e -> refreshCanvas(false));
        return card("Preview sample values", "card-amber",
                sampleTicketField, sampleTableField, sampleSeatField, standingChk);
    }

    private VerticalLayout buildActionsCard() {
        // Base image upload
        MemoryBuffer buf = new MemoryBuffer();
        Upload upload = new Upload(buf);
        upload.setAcceptedFileTypes("image/png","image/jpeg","image/jpg");
        upload.setMaxFiles(1);
        upload.addSucceededListener(e -> {
            try {
                byte[] newImg = buf.getInputStream().readAllBytes();
                setting = settingService.getSetting();
                setting.setBaseImage(newImg);
                settingService.save(setting);
                lastBgBytes = null; // force resend on next draw
                Notification.show("Base image updated.", 2000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                refreshCanvas(true);
            } catch (IOException ex) {
                log.error("Image upload failed", ex);
            }
        });

        Button saveBtn = new Button("Save Layout", VaadinIcon.CHECK.create());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.setWidthFull();
        saveBtn.addClickListener(e -> saveLayout());

        regenBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        regenBtn.setWidthFull();
        regenBtn.addClickListener(e -> confirmRegenerate());

        Span regenHint = new Span("Saves the layout, then re-renders every ticket — unused and already reserved (with its table/seat). "
                + "QR codes, ticket numbers and status stay the same.");
        regenHint.getStyle().set("font-size","0.72rem").set("color","var(--lumo-secondary-text-color)");

        Button previewCardBtn = new Button("Preview Real Ticket", VaadinIcon.EYE.create());
        previewCardBtn.setWidthFull();
        previewCardBtn.addClickListener(e -> openRealPreview());

        return card("Base image & actions", "card-violet", upload, saveBtn, previewCardBtn,
                regenBtn, regenHint);
    }

    private int safeMaxTickets() {
        Integer max = setting != null ? setting.getMaxNoOfTickets() : null;
        return max != null ? max : 0;
    }

    private static VerticalLayout card(String title, String colorClass, com.vaadin.flow.component.Component... children) {
        Span t = new Span(title);
        t.getStyle().set("font-weight","700").set("font-size","0.85rem").set("color","var(--lumo-body-text-color)");
        VerticalLayout card = new VerticalLayout(t);
        card.add(children);
        card.addClassNames("stat-card", colorClass);
        card.getStyle().set("min-width","200px").set("flex","1 1 200px");
        card.setPadding(true); card.setSpacing(true);
        return card;
    }

    // ── Event handlers ───────────────────────────────────────────────────────

    private void onElementMoved(String type, int x, int y) {
        x = Math.max(0, x);
        y = Math.max(0, y);
        setting = settingService.getSetting();
        switch (type) {
            case "qr"        -> { setting.setQrX(x);       setting.setQrY(y); }
            case "ticketNo"  -> { setting.setTextX(x);     setting.setTextY(y); }
            case "tableSeat" -> { setting.setTableSeatX(x);setting.setTableSeatY(y); }
        }
        settingService.save(setting);
        updateCoordLabels();
        refreshCanvas(false);
    }

    /** Copies the (possibly unsaved) side-panel values onto the given setting. */
    private void applyFields(Setting target) {
        target.setQrSize(clamp(qrSizeField.getValue(), 13, 1, 50));
        target.setQrBorder(clamp(qrBorderField.getValue(), 1, 0, 10));
        target.setFontSize(clamp(fontSizeField.getValue(), 20, 6, 400));
        target.setTableSeatFontSize(clamp(seatFontField.getValue(), 24, 6, 400));
        target.setTicketNoColor(normalizeHexColor(ticketNoColorField.getValue()));
        target.setTableSeatColor(normalizeHexColor(tableSeatColorField.getValue()));
        target.setQrX(Math.max(0, target.getQrX()));
        target.setQrY(Math.max(0, target.getQrY()));
        target.setTextX(Math.max(0, target.getTextX()));
        target.setTextY(Math.max(0, target.getTextY()));
        if (target.getTableSeatX() != null) target.setTableSeatX(Math.max(0, target.getTableSeatX()));
        if (target.getTableSeatY() != null) target.setTableSeatY(Math.max(0, target.getTableSeatY()));
    }

    private static int clamp(Integer v, int def, int min, int max) {
        int x = v != null ? v : def;
        return Math.max(min, Math.min(max, x));
    }

    private void persistLayout() {
        setting = settingService.getSetting();
        applyFields(setting);
        setting.setSampleTicketNo(sampleTicketField.getValue());
        setting.setSampleTable(sampleTableField.getValue());
        setting.setSampleSeat(sampleSeatField.getValue());
        settingService.save(setting);
    }

    private void saveLayout() {
        persistLayout();
        lastQrSize = -1;
        lastQrBorder = -1;
        Notification n = Notification.show("Layout saved.", 2000, Notification.Position.BOTTOM_END);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        updateCoordLabels();
        refreshCanvas(true);
    }

    private String sampleTicketNo() {
        String v = sampleTicketField.getValue();
        if (v != null && !v.isBlank()) return v.trim();
        String prefix = setting.getTicketPrefix() != null ? setting.getTicketPrefix() : "";
        int digits = setting.getNoOfDigits() != null && setting.getNoOfDigits() > 0 ? setting.getNoOfDigits() : 4;
        return prefix + String.format("%0" + digits + "d", 1);
    }

    private String sampleTableSeat() {
        String t = sampleTableField.getValue() == null ? "" : sampleTableField.getValue().trim();
        String st = sampleSeatField.getValue() == null ? "" : sampleSeatField.getValue().trim();
        return (t + "  " + st).trim();
    }

    /** Shows the ticket rendered by the real generator with the current (even unsaved) values — no download needed. */
    private void openRealPreview() {
        try {
            Setting preview = settingService.getSetting();
            applyFields(preview);
            String seat = standingChk.getValue() ? "" : sampleTableSeat();
            byte[] jpg = ticketService.renderTicket(preview, sampleTicketNo(), seat);
            TicketPreviewDialog.open("Ticket preview — " + sampleTicketNo(),
                    java.util.List.of(new TicketPreviewDialog.Item(sampleTicketNo(), jpg)));
        } catch (Exception ex) {
            log.error("Ticket preview failed", ex);
            Notification.show("Preview failed: " + ex.getMessage(), 4000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void confirmRegenerate() {
        ConfirmDialog dlg = new ConfirmDialog();
        dlg.setHeader("Regenerate all tickets?");
        dlg.setText("This saves the current layout and re-renders every ticket with it, including tickets already reserved " +
                "(their table/seat label is redrawn). QR codes, ticket numbers and status stay the same. " +
                "Ticket numbers and status are left unchanged. This may take a while if there are many tickets.");
        dlg.setConfirmText("Regenerate");
        dlg.setConfirmButtonTheme("error primary");
        dlg.setCancelable(true);
        dlg.addConfirmListener(e -> regenerateTickets());
        dlg.open();
    }

    private void regenerateTickets() {
        persistLayout();
        regenBtn.setEnabled(false);
        Span progressText = new Span("Regenerating… 0 tickets done");
        Notification progress = new Notification(progressText);
        progress.setDuration(0);
        progress.setPosition(Notification.Position.BOTTOM_END);
        progress.open();
        UI ui = UI.getCurrent();
        // The background thread has no UI session, so the event must be resolved here
        Long eventId = ticketService.currentEventId();
        Thread.ofVirtual().start(() -> {
            try {
                int count = ticketService.regenerateAllTicketImages(eventId, done ->
                        ui.access(() -> progressText.setText("Regenerating… " + done + " tickets done")));
                long reserved = ticketService.countTicketsByStatus(eventId, com.qrseatreservation.core.enums.TicketStatus.USED);
                ui.access(() -> {
                    progress.close();
                    Notification n = Notification.show("Regenerated " + count + " ticket(s), including " + reserved
                            + " reserved. QR codes unchanged.", 5000, Notification.Position.BOTTOM_END);
                    n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    regenBtn.setEnabled(true);
                });
            } catch (Exception ex) {
                log.error("Ticket regeneration failed", ex);
                ui.access(() -> {
                    progress.close();
                    Notification.show("Failed: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
                    regenBtn.setEnabled(true);
                });
            }
        });
    }

    private void generateMoreTickets() {
        try {
            ticketService.generateTicketNos();
            Notification n = Notification.show("Generated " + safeMaxTickets() + " additional ticket(s).", 3000, Notification.Position.BOTTOM_END);
            n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception ex) {
            log.error("Ticket generation failed", ex);
            Notification.show("Failed: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private void updateCoordLabels() {
        qrPosSpan.setText("QR position: " + setting.getQrX() + ", " + setting.getQrY());
        txtPosSpan.setText("Ticket # position: " + setting.getTextX() + ", " + setting.getTextY());
        int sx = setting.getTableSeatX() != null ? setting.getTableSeatX() : 0;
        int sy = setting.getTableSeatY() != null ? setting.getTableSeatY() : 0;
        seatPosSpan.setText("Table/Seat position: " + sx + ", " + sy);
    }

    /** Sends images to browser (only when they change), then draws. */
    private void refreshCanvas(boolean forceImageResend) {
        if (ticketCanvas == null) return;
        setting = settingService.getSetting();

        String ticketNo  = sampleTicketNo();
        int    qrSize    = clamp(qrSizeField.getValue(), 13, 1, 50);
        int    qrBorder  = clamp(qrBorderField.getValue(), 1, 0, 10);
        boolean bgChanged = forceImageResend || !java.util.Arrays.equals(setting.getBaseImage(), lastBgBytes);
        boolean qrChanged = forceImageResend || !ticketNo.equals(lastQrTicketNo) || qrSize != lastQrSize || qrBorder != lastQrBorder;

        if (bgChanged || qrChanged) {
            try {
                String bgBase64 = Base64.getEncoder().encodeToString(setting.getBaseImage());
                byte[] qrBytes = QrTicketGenerator.generateQrImageBytes(ticketNo, qrSize, qrBorder);
                String qrBase64 = Base64.getEncoder().encodeToString(qrBytes);
                ticketCanvas.getElement().executeJs(SETUP_JS, bgBase64, qrBase64);
                lastBgBytes    = setting.getBaseImage().clone();
                lastQrTicketNo = ticketNo;
                lastQrSize     = qrSize;
                lastQrBorder   = qrBorder;
            } catch (Exception ex) {
                log.error("Failed to generate QR preview", ex);
            }
        }

        // Text is rendered server-side with the exact ticket renderer so the editor matches the output
        int txFont = clamp(fontSizeField.getValue(), 20, 6, 400);
        int sxFont = clamp(seatFontField.getValue(), 24, 6, 400);
        String txColor = normalizeHexColor(ticketNoColorField.getValue());
        boolean standing = standingChk.getValue();
        String sxColor = standing ? "#ffffff" : normalizeHexColor(tableSeatColorField.getValue());
        String tableSeat = sampleTableSeat();
        if (tableSeat.isEmpty()) tableSeat = "Table  Seat";
        String textKey = ticketNo + "|" + txFont + "|" + txColor + "|" + tableSeat + "|" + sxFont + "|" + sxColor;
        if (forceImageResend || !textKey.equals(lastTextKey)) {
            try {
                BufferedImage tx = QrTicketGenerator.renderText(ticketNo, txFont, txColor);
                BufferedImage sx = QrTicketGenerator.renderText(tableSeat, sxFont, sxColor);
                txW = tx.getWidth(); txH = tx.getHeight();
                sxW = sx.getWidth(); sxH = sx.getHeight();
                ticketCanvas.getElement().executeJs(TEXT_JS, pngBase64(tx), pngBase64(sx));
                lastTextKey = textKey;
            } catch (Exception ex) {
                log.error("Failed to render text preview", ex);
            }
        }

        int qrPx = computeQrPixels(ticketNo, qrSize, qrBorder);
        int sxX  = setting.getTableSeatX() != null ? setting.getTableSeatX() : 0;
        int sxY  = setting.getTableSeatY() != null ? setting.getTableSeatY() : 0;

        String json = String.format(
            "{\"qrX\":%d,\"qrY\":%d,\"qrPx\":%d,\"txX\":%d,\"txY\":%d,\"txW\":%d,\"txH\":%d," +
            "\"sxX\":%d,\"sxY\":%d,\"sxW\":%d,\"sxH\":%d,\"showSeat\":true,\"snap\":%b,\"snapPx\":5}",
            setting.getQrX(), setting.getQrY(), qrPx,
            setting.getTextX(), setting.getTextY(), txW, txH,
            sxX, sxY, sxW, sxH, snapChk.getValue());

        ticketCanvas.getElement().executeJs(DRAW_JS, json);
    }

    private static String pngBase64(BufferedImage img) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }

    /** Exact QR pixel size = (modules + 2*border) * scale, using the real module count for this text. */
    private static int computeQrPixels(String ticketNo, int scale, int border) {
        int modules = QrTicketGenerator.qrModuleCount(ticketNo != null && !ticketNo.isBlank() ? ticketNo : "0");
        return (modules + border * 2) * scale;
    }

    // ── Inner canvas element ─────────────────────────────────────────────────

    @Tag("canvas")
    public static class TicketCanvas extends Div {
        private Runnable onAttachCallback;

        public void setOnAttachCallback(Runnable r) { this.onAttachCallback = r; }

        @Override
        protected void onAttach(AttachEvent e) {
            super.onAttach(e);
            if (onAttachCallback != null) onAttachCallback.run();
        }
    }
}
