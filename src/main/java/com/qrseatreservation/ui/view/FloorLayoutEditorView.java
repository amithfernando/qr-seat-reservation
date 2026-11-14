package com.qrseatreservation.ui.view;

import com.qrseatreservation.core.enums.ZoneType;
import com.qrseatreservation.core.model.TableDetail;
import com.qrseatreservation.core.model.VenueZone;
import com.qrseatreservation.core.service.TableDetailService;
import com.qrseatreservation.core.service.VenueZoneService;
import com.qrseatreservation.ui.view.layout.MainLayout;
import com.qrseatreservation.ui.view.layout.PageLayout;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import elemental.json.JsonObject;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Route(value = "floor-layout-editor", layout = MainLayout.class)
@RolesAllowed({"ADMIN"})
@PageTitle("Floor Layout Editor")
@Slf4j
public class FloorLayoutEditorView extends PageLayout {

    private final VenueZoneService venueZoneService;
    private final TableDetailService tableDetailService;

    private FloorCanvas canvas;

    private final Grid<VenueZone> zoneGrid = new Grid<>(VenueZone.class, false);

    private final TextField labelField         = new TextField("Label");
    private final ComboBox<ZoneType> typeField = new ComboBox<>("Type");
    private final IntegerField posxField       = new IntegerField("X");
    private final IntegerField posyField       = new IntegerField("Y");
    private final IntegerField widthField      = new IntegerField("Width");
    private final IntegerField heightField     = new IntegerField("Height");
    private final IntegerField rotationField   = new IntegerField("Rotation (°)");
    private final TextField colorField         = new TextField("Colour (hex)");

    private final Button saveBtn   = new Button("Save Zone", VaadinIcon.CHECK.create());
    private final Button deleteBtn = new Button("Delete", VaadinIcon.TRASH.create());
    private final Button clearBtn  = new Button("Clear", VaadinIcon.ERASER.create());

    private VenueZone editing;

    public FloorLayoutEditorView(VenueZoneService venueZoneService,
                                 TableDetailService tableDetailService) {
        super(VaadinIcon.GRID, "Floor Layout Editor");
        this.venueZoneService = venueZoneService;
        this.tableDetailService = tableDetailService;

        try {
            build();
        } catch (Exception ex) {
            log.error("FloorLayoutEditorView load failed", ex);
            Span err = new Span("Could not load — database unavailable. Please refresh.");
            err.getStyle().set("color", "var(--lumo-error-text-color)");
            Button retry = new Button("Retry", VaadinIcon.REFRESH.create());
            retry.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(FloorLayoutEditorView.class)));
            addToContent(err, retry);
        }
    }

    private void build() {
        List<TableDetail> tables = tableDetailService.getAllTables();
        List<VenueZone>   zones  = venueZoneService.getAll();

        canvas = new FloorCanvas(tables, zones);
        canvas.setWidth("100%");
        canvas.getStyle().set("display", "block").set("cursor", "default");

        canvas.getElement()
              .addEventListener("zone-clicked", event -> {
                  JsonObject d = (JsonObject) event.getEventData();
                  long id = (long) d.getNumber("event.detail.id");
                  venueZoneService.getAll().stream()
                          .filter(z -> z.getId() != null && z.getId() == id)
                          .findFirst()
                          .ifPresent(z -> { openEdit(z); zoneGrid.select(z); });
              })
              .addEventData("event.detail.id");

        canvas.getElement()
              .addEventListener("zone-moved", event -> {
                  JsonObject d = (JsonObject) event.getEventData();
                  long id = (long) d.getNumber("event.detail.id");
                  int  nx = (int)  d.getNumber("event.detail.x");
                  int  ny = (int)  d.getNumber("event.detail.y");
                  onZoneMoved(id, nx, ny);
              })
              .addEventData("event.detail.id")
              .addEventData("event.detail.x")
              .addEventData("event.detail.y");

        canvas.getElement()
              .addEventListener("table-moved", event -> {
                  JsonObject d = (JsonObject) event.getEventData();
                  long id = (long) d.getNumber("event.detail.id");
                  int  nx = (int)  d.getNumber("event.detail.x");
                  int  ny = (int)  d.getNumber("event.detail.y");
                  onTableMoved(id, nx, ny);
              })
              .addEventData("event.detail.id")
              .addEventData("event.detail.x")
              .addEventData("event.detail.y");

        Div canvasWrapper = new Div(canvas);
        canvasWrapper.getStyle()
                .set("overflow", "auto").set("width", "100%").set("min-height", "460px")
                .set("border", "1px solid var(--app-border)").set("border-radius", "var(--app-radius)")
                .set("background", "#f8fafc");

        Button downloadBtn = new Button("Download PNG", VaadinIcon.DOWNLOAD.create());
        downloadBtn.addClickListener(e -> getUI().ifPresent(ui ->
                ui.getPage().executeJs("""
                    const c = $0;
                    const exp = document.createElement('canvas');
                    exp.width = c.width; exp.height = c.height;
                    const ctx2 = exp.getContext('2d');
                    ctx2.fillStyle='#fff'; ctx2.fillRect(0,0,exp.width,exp.height);
                    ctx2.drawImage(c,0,0);
                    const a = document.createElement('a');
                    const d = new Date();
                    const p = n=>String(n).padStart(2,'0');
                    a.download=`floor-${d.getFullYear()}${p(d.getMonth()+1)}${p(d.getDate())}.png`;
                    a.href=exp.toDataURL('image/png');
                    document.body.appendChild(a); a.click(); a.remove();
                """, canvas.getElement())));

        Span hint = new Span("💡 Drag zones and tables directly on the canvas to reposition them");
        hint.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.8rem");

        HorizontalLayout canvasBar = new HorizontalLayout(downloadBtn, hint);
        canvasBar.setWidthFull();
        canvasBar.setAlignItems(FlexComponent.Alignment.CENTER);

        // Zone list
        configureGrid();
        Button addBtn = new Button("Add Zone", VaadinIcon.PLUS_CIRCLE.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> openNew());

        VerticalLayout leftPanel = new VerticalLayout(new HorizontalLayout(addBtn), zoneGrid);
        leftPanel.setPadding(false); leftPanel.setSpacing(true); leftPanel.setWidth("400px");

        VerticalLayout formPanel = buildFormPanel();
        formPanel.setWidth("300px");

        HorizontalLayout editorRow = new HorizontalLayout(leftPanel, formPanel);
        editorRow.setWidthFull(); editorRow.setSpacing(true);
        editorRow.setAlignItems(FlexComponent.Alignment.START);
        editorRow.getStyle().set("flex-wrap", "wrap");

        addToContent(canvasBar, canvasWrapper, editorRow);
        refreshGrid();
    }

    /** Called by the drag-drop JS event — saves new position and redraws. */
    private void onZoneMoved(long id, int x, int y) {
        venueZoneService.getAll().stream()
                .filter(z -> z.getId() == id)
                .findFirst()
                .ifPresent(z -> {
                    z.setPosx(x); z.setPosy(y);
                    venueZoneService.save(z);
                    // Sync form if this zone is being edited
                    if (editing != null && editing.getId() != null && editing.getId() == id) {
                        posxField.setValue(x); posyField.setValue(y);
                    }
                    refreshGrid();
                    refreshCanvas();
                });
    }

    /** Called by the drag-drop JS event for a table — persists its new center position. */
    private void onTableMoved(long id, int x, int y) {
        tableDetailService.updatePosition(id, x, y);
        refreshCanvas();
    }

    private void configureGrid() {
        zoneGrid.addColumn(VenueZone::getLabel).setHeader("Label").setFlexGrow(1);
        zoneGrid.addColumn(z -> z.getZoneType() != null ? z.getZoneType().name() : "").setHeader("Type").setAutoWidth(true);
        zoneGrid.addColumn(z -> z.getPosx() + ", " + z.getPosy()).setHeader("Pos").setAutoWidth(true);
        zoneGrid.addColumn(z -> z.getWidth() + " × " + z.getHeight()).setHeader("Size").setAutoWidth(true);
        zoneGrid.setHeight("240px");
        zoneGrid.getElement().getThemeList().add("row-stripes");
        zoneGrid.asSingleSelect().addValueChangeListener(e -> { if (e.getValue() != null) openEdit(e.getValue()); });
    }

    private VerticalLayout buildFormPanel() {
        labelField.setWidthFull(); colorField.setWidthFull(); colorField.setPlaceholder("#4f46e5");
        typeField.setItems(ZoneType.values()); typeField.setWidthFull();
        for (IntegerField f : new IntegerField[]{posxField, posyField, widthField, heightField, rotationField}) {
            f.setStepButtonsVisible(true); f.setWidthFull();
        }
        rotationField.setMin(0); rotationField.setMax(360); rotationField.setValue(0);
        widthField.setMin(10); heightField.setMin(10);

        saveBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        saveBtn.addClickListener(e -> onSave());
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
        deleteBtn.setEnabled(false);
        deleteBtn.addClickListener(e -> onDelete());
        clearBtn.addClickListener(e -> clearForm());

        FormLayout form = new FormLayout(labelField, typeField, posxField, posyField,
                widthField, heightField, rotationField, colorField);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("260px", 2));
        form.setWidthFull();

        Span title = new Span("Zone Details");
        title.getStyle().set("font-weight", "700").set("font-size", "0.9rem").set("color", "var(--lumo-secondary-text-color)");

        VerticalLayout p = new VerticalLayout(title, form, new HorizontalLayout(saveBtn, deleteBtn, clearBtn));
        p.getStyle().set("background", "var(--app-surface)").set("border", "1px solid var(--app-border)")
                    .set("border-radius", "var(--app-radius)").set("padding", "var(--lumo-space-m)");
        p.setPadding(false); p.setSpacing(true);
        return p;
    }

    private void openNew() {
        editing = new VenueZone(); clearForm(); deleteBtn.setEnabled(false); zoneGrid.deselectAll();
    }

    private void openEdit(VenueZone z) {
        editing = z;
        labelField.setValue(z.getLabel() != null ? z.getLabel() : "");
        typeField.setValue(z.getZoneType());
        posxField.setValue(z.getPosx() != null ? z.getPosx() : 0);
        posyField.setValue(z.getPosy() != null ? z.getPosy() : 0);
        widthField.setValue(z.getWidth() != null ? z.getWidth() : 100);
        heightField.setValue(z.getHeight() != null ? z.getHeight() : 60);
        rotationField.setValue(z.getRotation() != null ? z.getRotation() : 0);
        colorField.setValue(z.getColor() != null ? z.getColor() : "");
        deleteBtn.setEnabled(true);
    }

    private void onSave() {
        if (labelField.getValue().isBlank()) {
            Notification.show("Label is required.", 2500, Notification.Position.MIDDLE); return;
        }
        if (editing == null) editing = new VenueZone();
        editing.setLabel(labelField.getValue().trim());
        editing.setZoneType(typeField.getValue());
        editing.setPosx(posxField.getValue() != null ? posxField.getValue() : 0);
        editing.setPosy(posyField.getValue() != null ? posyField.getValue() : 0);
        editing.setWidth(widthField.getValue() != null ? widthField.getValue() : 100);
        editing.setHeight(heightField.getValue() != null ? heightField.getValue() : 60);
        editing.setRotation(rotationField.getValue() != null ? rotationField.getValue() : 0);
        editing.setColor(colorField.getValue().isBlank() ? null : colorField.getValue().trim());
        editing = venueZoneService.save(editing); // keep saved entity (with ID) so delete works immediately
        deleteBtn.setEnabled(true);
        Notification n = Notification.show("Zone saved.", 2000, Notification.Position.BOTTOM_END);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        refreshGrid(); refreshCanvas();
    }

    private void onDelete() {
        if (editing == null || editing.getId() == null) return;
        ConfirmDialog dlg = new ConfirmDialog();
        dlg.setHeader("Delete zone");
        dlg.setText("Delete \"" + editing.getLabel() + "\"?");
        dlg.setConfirmButtonTheme("error primary");
        dlg.addConfirmListener(e -> {
            venueZoneService.delete(editing.getId());
            Notification.show("Zone deleted.", 2000, Notification.Position.BOTTOM_END);
            clearForm(); refreshGrid(); refreshCanvas();
        });
        dlg.open();
    }

    private void clearForm() {
        editing = null;
        labelField.clear(); typeField.clear();
        posxField.setValue(0); posyField.setValue(0);
        widthField.setValue(100); heightField.setValue(60);
        rotationField.setValue(0); colorField.clear();
        deleteBtn.setEnabled(false); zoneGrid.deselectAll();
    }

    private void refreshGrid()   { zoneGrid.setItems(venueZoneService.getAll()); }
    private void refreshCanvas() {
        if (canvas != null) {
            canvas.setZones(venueZoneService.getAll());
            canvas.setTables(tableDetailService.getAllTables());
            canvas.draw();
        }
    }

    // ── Inner canvas ─────────────────────────────────────────────────────────

    @Tag("canvas")
    public static class FloorCanvas extends Div {

        private static final int COLS    = 3;
        private static final int CELL_W  = 160;
        private static final int CELL_H  = 160;
        private static final int TABLE_TOP = 50;

        private List<TableDetail> tables;
        private List<VenueZone> zones;

        public FloorCanvas(List<TableDetail> tables, List<VenueZone> zones) {
            this.tables = tables; this.zones = zones;
        }

        public void setZones(List<VenueZone> zones) { this.zones = zones; }
        public void setTables(List<TableDetail> tables) { this.tables = tables; }

        @Override
        protected void onAttach(AttachEvent e) { super.onAttach(e); draw(); }

        public void draw() {
            String zonesJson  = buildZonesJson();
            String tablesJson = buildTablesJson();

            // Persistent-controller: first call wires listeners, subsequent calls only push new data+redraw.
            String js = """
                (function(canvas) {
                  const newZones  = %s;
                  const newTables = %s;

                  if (canvas.__ctl) { canvas.__ctl.update(newZones, newTables); return; }

                  // ── First-time setup ────────────────────────────────────────────────
                  const state = { zones: newZones, tables: newTables, drag: null, offX: 0, offY: 0, rafId: null };
                  const ctx   = canvas.getContext('2d');
                  const TR = 36, SR = 10;

                  function resize() {
                    const maxW = (canvas.parentElement ? canvas.parentElement.clientWidth : 800) - 10;
                    canvas.width  = Math.max(maxW, 600);
                    canvas.height = Math.max(window.innerHeight * 0.8, 600);
                  }

                  function zoneColor(z) {
                    if (z.color) return z.color;
                    const m = {STAGE:'#7c3aed',DANCE_FLOOR:'#0ea5e9',BAR:'#f59e0b',STANDING:'#10b981',CUSTOM:'#94a3b8'};
                    return m[z.zoneType] || '#94a3b8';
                  }

                  function drawAll() {
                    state.rafId = null;
                    ctx.clearRect(0, 0, canvas.width, canvas.height);

                    // Legend
                    const zoneLeg = [{l:'Stage',c:'#7c3aed'},{l:'Dance Floor',c:'#0ea5e9'},{l:'Bar',c:'#f59e0b'},{l:'Standing',c:'#10b981'},{l:'Custom',c:'#94a3b8'}];
                    const seatLeg = [{l:'Available',c:'#4CAF50'},{l:'Reserved',c:'#F44336'},{l:'Checked-in',c:'#2196F3'},{l:'Unavailable',c:'#bdbdbd'}];
                    ctx.font = '11px Arial'; ctx.textBaseline = 'middle'; ctx.textAlign = 'left';
                    ctx.fillStyle = '#475467'; ctx.fillText('Zones:', 8, 13);
                    zoneLeg.forEach((it, i) => { const x = 52 + i * 90; ctx.fillStyle = it.c; ctx.globalAlpha = 0.35; ctx.fillRect(x, 7, 14, 10); ctx.globalAlpha = 1; ctx.strokeStyle = it.c; ctx.lineWidth = 1; ctx.strokeRect(x, 7, 14, 10); ctx.fillStyle = '#111'; ctx.fillText(it.l, x + 16, 13); });
                    ctx.fillStyle = '#475467'; ctx.fillText('Seats:', 8, 29);
                    seatLeg.forEach((it, i) => { const x = 52 + i * 90; ctx.fillStyle = it.c; ctx.fillRect(x, 23, 14, 10); ctx.fillStyle = '#111'; ctx.fillText(it.l, x + 16, 29); });

                    // Zones
                    for (const z of state.zones) {
                      ctx.save();
                      ctx.translate(z.px + z.w / 2, z.py + z.h / 2);
                      ctx.rotate(z.rot * Math.PI / 180);
                      ctx.translate(-z.w / 2, -z.h / 2);
                      const col = zoneColor(z);
                      ctx.globalAlpha = 0.22; ctx.fillStyle = col;
                      ctx.beginPath(); if (ctx.roundRect) ctx.roundRect(0, 0, z.w, z.h, 8); else ctx.rect(0, 0, z.w, z.h); ctx.fill();
                      ctx.globalAlpha = 1;
                      ctx.strokeStyle = state.drag === z ? '#ef4444' : col;
                      ctx.lineWidth   = state.drag === z ? 2.5 : 1.5;
                      ctx.setLineDash([6, 3]); ctx.stroke(); ctx.setLineDash([]);
                      ctx.fillStyle = col; ctx.font = 'bold 13px Arial'; ctx.textAlign = 'center'; ctx.textBaseline = 'middle';
                      ctx.fillText(z.label, z.w / 2, z.h / 2);
                      ctx.restore();
                    }

                    // Tables + seats
                    for (const t of state.tables) {
                      const cx = t.px, cy = t.py;
                      ctx.beginPath(); ctx.arc(cx, cy, TR, 0, 2 * Math.PI);
                      ctx.fillStyle = '#FF9800'; ctx.fill();
                      ctx.strokeStyle = state.drag === t ? '#ef4444' : '#555';
                      ctx.lineWidth   = state.drag === t ? 2.5 : 1.5; ctx.stroke();
                      ctx.fillStyle = '#000'; ctx.font = 'bold 11px Arial'; ctx.textAlign = 'center'; ctx.textBaseline = 'middle';
                      ctx.fillText(t.name, cx, cy);
                      const sc = t.seats.length;
                      const orbitR = Math.max(TR + SR + 10, Math.ceil(sc * SR / Math.PI));
                      t.seats.forEach((s, i) => {
                        const angle = 2 * Math.PI * i / Math.max(sc, 1) - Math.PI / 2;
                        const sx = cx + Math.cos(angle) * orbitR, sy = cy + Math.sin(angle) * orbitR;
                        const colors = {AVAILABLE:'#4CAF50',RESERVED:'#F44336',CHECKED_IN:'#2196F3',UNAVAILABLE:'#bdbdbd'};
                        ctx.beginPath(); ctx.arc(sx, sy, SR, 0, 2 * Math.PI);
                        ctx.fillStyle = colors[s.status] || '#4CAF50'; ctx.fill();
                        ctx.strokeStyle = '#444'; ctx.lineWidth = 0.8; ctx.stroke();
                        ctx.fillStyle = 'white'; ctx.font = 'bold 8px Arial'; ctx.textAlign = 'center'; ctx.textBaseline = 'middle';
                        ctx.fillText(s.no, sx, sy);
                      });
                    }
                  }

                  function schedDraw() {
                    if (!state.rafId) state.rafId = requestAnimationFrame(drawAll);
                  }

                  function hitTest(mx, my) {
                    for (let i = state.zones.length - 1; i >= 0; i--) {
                      const z = state.zones[i];
                      if (mx >= z.px && mx <= z.px + z.w && my >= z.py && my <= z.py + z.h) return z;
                    }
                    for (const t of state.tables) {
                      const dx = mx - t.px, dy = my - t.py;
                      if (dx * dx + dy * dy <= TR * TR) return t;
                    }
                    return null;
                  }

                  function evPos(e) {
                    const r = canvas.getBoundingClientRect(), src = e.touches ? e.touches[0] : e;
                    return { x: src.clientX - r.left, y: src.clientY - r.top };
                  }

                  let startPx = 0, startPy = 0;
                  function onStart(e) {
                    const p = evPos(e), hit = hitTest(p.x, p.y);
                    if (hit) { state.drag = hit; state.offX = p.x - hit.px; state.offY = p.y - hit.py; startPx = hit.px; startPy = hit.py; if (e.cancelable) e.preventDefault(); }
                  }
                  function onMove(e) {
                    if (!state.drag) return;
                    const p = evPos(e);
                    state.drag.px = Math.max(0, p.x - state.offX);
                    state.drag.py = Math.max(0, p.y - state.offY);
                    schedDraw();
                    if (e.cancelable) e.preventDefault();
                  }
                  function onEnd() {
                    if (!state.drag) return;
                    const moved = Math.abs(state.drag.px - startPx) > 4 || Math.abs(state.drag.py - startPy) > 4;
                    if (moved) {
                      const evName = state.drag.kind === 'table' ? 'table-moved' : 'zone-moved';
                      canvas.dispatchEvent(new CustomEvent(evName, { bubbles: true, detail: { id: state.drag.id, x: Math.round(state.drag.px), y: Math.round(state.drag.py) } }));
                    } else if (state.drag.kind === 'zone') {
                      canvas.dispatchEvent(new CustomEvent('zone-clicked', { bubbles: true, detail: { id: state.drag.id } }));
                    }
                    state.drag = null; canvas.style.cursor = 'default'; schedDraw();
                  }

                  canvas.addEventListener('mousedown', onStart);
                  canvas.addEventListener('mousemove', e => {
                    if (state.drag) { onMove(e); }
                    else { const p = evPos(e); canvas.style.cursor = hitTest(p.x, p.y) ? 'move' : 'default'; }
                  });
                  canvas.addEventListener('mouseup',   onEnd);
                  canvas.addEventListener('mouseleave', () => { if (state.drag) { state.drag = null; schedDraw(); } canvas.style.cursor = 'default'; });
                  canvas.addEventListener('touchstart', onStart, { passive: false });
                  canvas.addEventListener('touchmove',  onMove,  { passive: false });
                  canvas.addEventListener('touchend',   onEnd);

                  canvas.__ctl = {
                    update(z, t) { state.zones = z; state.tables = t; resize(); schedDraw(); }
                  };

                  resize();
                  schedDraw();
                })(this);
                """.formatted(zonesJson, tablesJson);

            getElement().executeJs(js);
        }

        private String buildZonesJson() {
            StringBuilder sb = new StringBuilder("[");
            for (VenueZone z : zones) {
                String color = z.getColor() != null && !z.getColor().isBlank() ? z.getColor() : "";
                String type  = z.getZoneType() != null ? z.getZoneType().name() : "CUSTOM";
                sb.append(String.format(
                    "{id:%d,kind:'zone',label:'%s',zoneType:'%s',px:%d,py:%d,w:%d,h:%d,rot:%d,color:'%s'},",
                    z.getId(),
                    escJs(z.getLabel()),
                    type,
                    z.getPosx() != null ? z.getPosx() : 0,
                    z.getPosy() != null ? z.getPosy() : 0,
                    z.getWidth() != null ? z.getWidth() : 100,
                    z.getHeight() != null ? z.getHeight() : 60,
                    z.getRotation() != null ? z.getRotation() : 0,
                    escJs(color)
                ));
            }
            if (sb.charAt(sb.length()-1) == ',') sb.deleteCharAt(sb.length()-1);
            sb.append("]");
            return sb.toString();
        }

        private String buildTablesJson() {
            StringBuilder sb = new StringBuilder("[");
            int col = 0, row = 0;
            for (TableDetail t : tables) {
                int px, py;
                if (t.getPosX() != null && t.getPosY() != null) {
                    px = t.getPosX(); py = t.getPosY();
                } else {
                    // Fallback auto-grid layout for tables that haven't been manually positioned yet
                    px = CELL_W / 2 + col * CELL_W + 20;
                    py = TABLE_TOP + CELL_H / 2 + row * CELL_H;
                }
                col++;
                if (col >= COLS) { col = 0; row++; }

                sb.append(String.format("{id:%d,kind:'table',name:'%s',px:%d,py:%d,seats:[",
                        t.getId(), escJs(t.getTableName()), px, py));
                var seats = t.getSeatDetails();
                if (seats != null) {
                    for (var s : seats) {
                        sb.append(String.format("{no:'%s',status:'%s'},",
                                escJs(s.getSeatNo()),
                                s.getSeatStatus() != null ? s.getSeatStatus().name() : "AVAILABLE"));
                    }
                    if (!seats.isEmpty()) sb.deleteCharAt(sb.length()-1);
                }
                sb.append("]},");
            }
            if (sb.charAt(sb.length()-1) == ',') sb.deleteCharAt(sb.length()-1);
            sb.append("]");
            return sb.toString();
        }

        private static String escJs(String s) {
            if (s == null) return "";
            return s.replace("\\","\\\\").replace("'","\\'").replace("\n"," ");
        }
    }
}
