package com.qrseatreservation.ui.view.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.shared.Registration;

/** Server-side wrapper for the {@code <qr-scanner>} camera QR reader (see frontend/qr-scanner.js). */
@Tag("qr-scanner")
@NpmPackage(value = "@zxing/library", version = "^0.21.3")
@JsModule("./qr-scanner.js")
public class QrScanner extends Component implements HasSize, HasStyle {

    private boolean frontCamera;

    public void setActive(boolean active) {
        if (active) getElement().setAttribute("active", "");
        else getElement().removeAttribute("active");
    }

    public boolean isActive() {
        return getElement().hasAttribute("active");
    }

    /** Toggles between the back and the front camera. */
    public void switchCamera() {
        frontCamera = !frontCamera;
        getElement().setAttribute("facing", frontCamera ? "user" : "environment");
    }

    /** Allows the same code to be reported again straight away. */
    public void clearLastScan() {
        getElement().callJsFunction("clearLast");
    }

    public Registration addScanListener(SerializableConsumer<String> listener) {
        return getElement().addEventListener("qr-scanned", e -> listener.accept(e.getEventData().getString("event.detail.value")))
                .addEventData("event.detail.value");
    }

    public Registration addStartedListener(SerializableRunnable listener) {
        return getElement().addEventListener("scanner-started", e -> listener.run());
    }

    /** Called with (error name, message) when the camera cannot be started. */
    public Registration addErrorListener(SerializableBiConsumer<String, String> listener) {
        return getElement().addEventListener("scanner-error", e -> listener.accept(
                        e.getEventData().getString("event.detail.name"),
                        e.getEventData().getString("event.detail.message")))
                .addEventData("event.detail.name")
                .addEventData("event.detail.message");
    }
}
