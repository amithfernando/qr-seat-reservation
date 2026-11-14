import { BrowserQRCodeReader } from '@zxing/library';

/**
 * Minimal, robust QR scanner for the gate.
 * - One camera stream at a time, back camera preferred (facingMode "environment").
 * - Per-frame "no QR found" misses are ignored; only real start-up failures are reported.
 * - Stops the camera when hidden/removed and restarts it when the page becomes visible again.
 *
 * Attributes: active (start/stop), facing ("environment" | "user").
 * Events: qr-scanned {value}, scanner-started, scanner-error {name, message}.
 */
class QrScanner extends HTMLElement {
  static get observedAttributes() {
    return ['active', 'facing'];
  }

  constructor() {
    super();
    this._running = false;
    this._starting = null;
    this._last = null;
    this._lastAt = 0;
    this._onVisibility = () => {
      if (document.hidden) this._stopStream();
      else if (this._wanted()) this._start();
    };
  }

  connectedCallback() {
    if (!this._video) {
      const v = document.createElement('video');
      v.setAttribute('playsinline', '');
      v.setAttribute('muted', '');
      v.setAttribute('autoplay', '');
      v.muted = true;
      v.playsInline = true;
      v.style.cssText = 'width:100%;height:100%;object-fit:cover;display:block;background:#0f172a;';
      this._video = v;
      this.appendChild(v);
    }
    document.addEventListener('visibilitychange', this._onVisibility);
    if (this._wanted()) this._start();
  }

  disconnectedCallback() {
    document.removeEventListener('visibilitychange', this._onVisibility);
    this._stopStream();
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (!this.isConnected || oldValue === newValue) return;
    if (name === 'active') {
      if (this._wanted()) this._start(); else this._stopStream();
    } else if (name === 'facing' && this._wanted()) {
      this._stopStream();
      this._start();
    }
  }

  _wanted() {
    return this.hasAttribute('active') && this.getAttribute('active') !== 'false';
  }

  async _start() {
    if (this._running || this._starting) return this._starting;
    if (!window.isSecureContext || !navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
      this._error('NotSupportedError', 'Camera needs HTTPS and a browser with camera support.');
      return;
    }
    const facing = this.getAttribute('facing') || 'environment';
    const constraints = {
      audio: false,
      video: { facingMode: { ideal: facing }, width: { ideal: 1280 }, height: { ideal: 720 } }
    };
    this._reader = new BrowserQRCodeReader(150);
    this._starting = this._reader
      .decodeFromConstraints(constraints, this._video, (result) => {
        if (result) this._emit(result.getText());
      })
      .then(() => {
        this._running = true;
        this.dispatchEvent(new CustomEvent('scanner-started'));
      })
      .catch((e) => {
        this._stopStream();
        this._error((e && e.name) || 'Error', (e && e.message) || String(e));
      })
      .finally(() => {
        this._starting = null;
      });
    return this._starting;
  }

  _stopStream() {
    this._running = false;
    try {
      if (this._reader) this._reader.reset();
    } catch (e) {
      /* ignore */
    }
    this._reader = null;
    const s = this._video && this._video.srcObject;
    if (s && s.getTracks) s.getTracks().forEach((t) => t.stop());
    if (this._video) this._video.srcObject = null;
  }

  _emit(text) {
    const now = Date.now();
    if (text === this._last && now - this._lastAt < 2500) return;
    this._last = text;
    this._lastAt = now;
    if (navigator.vibrate) navigator.vibrate(60);
    this.dispatchEvent(new CustomEvent('qr-scanned', { detail: { value: text } }));
  }

  _error(name, message) {
    this.dispatchEvent(new CustomEvent('scanner-error', { detail: { name, message } }));
  }

  /** Lets the same ticket be scanned again immediately (e.g. after closing its result). */
  clearLast() {
    this._last = null;
  }
}

if (!customElements.get('qr-scanner')) customElements.define('qr-scanner', QrScanner);
