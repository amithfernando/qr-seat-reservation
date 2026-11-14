# QR Seat Reservation — User Manual

**Version 1.0 · May 2026**

---

## Table of Contents

1. [Overview](#1-overview)
2. [Access & Login](#2-access--login)
3. [Navigation](#3-navigation)
4. [Dashboard (Home)](#4-dashboard-home)
5. [Management — Tables](#5-management--tables)
6. [Management — Sellers](#6-management--sellers)
7. [Management — Reservations](#7-management--reservations)
8. [Management — Users](#8-management--users)
9. [Management — Ticket Categories](#9-management--ticket-categories)
10. [Management — Settings](#10-management--settings)
11. [Operations — Check In](#11-operations--check-in)
12. [Operations — Seating Layout](#12-operations--seating-layout)
13. [Operations — Reports & Metrics](#13-operations--reports--metrics)
14. [Roles & Permissions](#14-roles--permissions)
15. [Typical Workflow (End-to-End)](#15-typical-workflow-end-to-end)
16. [Troubleshooting](#16-troubleshooting)
17. [Events (Multiple Campaigns)](#17-events-multiple-campaigns)

---

## 1. Overview

**QR Seat Reservation** is a web-based event seat reservation and check-in system. It manages:

- Venue tables and seat inventory
- Ticket sellers and their sales
- Guest reservations with auto-assigned seats
- QR-coded tickets that are generated and used at the door
- Real-time check-in via QR scanner or manual ticket lookup
- Financial and operational reporting

The system is designed to work on desktop and mobile browsers. For camera-based QR scanning on iPhone, access the site over **HTTPS**.

---

## 2. Access & Login

Open your browser and navigate to the application URL (e.g. `https://your-domain.example` or the IP address on port 80/443).

You are taken to the **Login** screen. Enter your username and password, then press **Sign in**.

| Default Username | Password   | Role      |
|-----------------|------------|-----------|
| `admin`         | `password` | ADMIN     |
| `entrance`      | `password` | ENTRANCE  |

> **Security note:** Change the default passwords immediately after first login via **Management → Users**.

If your credentials are wrong, a red error banner appears. Re-enter and try again.

---

## 3. Navigation

After login you choose an event (see section 17). The app uses a dark theme designed for phones first.

| Area | Description |
|------|-------------|
| **Top bar** | Menu (☰), app name, the **current event chip** (tap it to switch event) and your user menu |
| **Left drawer** | All pages, grouped by section. On phones it slides in from the left and closes after you pick a page |
| **Bottom tab bar** *(phones only)* | Home, Bookings *(admin)*, Scan, Seats and Reports *(admin)* — one tap away with your thumb |
| **Page area** | Content of the selected page |

### Navigation sections

**Main** — Home (event dashboard)

**Management** *(ADMIN only)* — Events, Reservations, Tables, Sellers, Users

**Tickets** *(ADMIN only)* — Ticket Categories, Ticket Layout Editor, Settings

**Floor** — Seating Layout, Floor Layout Editor *(ADMIN only)*

**Operations** — Check In, Reports *(ADMIN only)*

### Home dashboard

- **Event card** — name, venue, date countdown, status, occupancy ring and the main actions (Scan tickets, New reservation).
- **Key figures** — tickets sold, checked in; admins also see money collected and outstanding.
- **Get this event ready** *(admins, until done)* — checklist: generate tickets, add tables, add sellers, first reservation.
- **Seat status** and **Ticket inventory** — warns when tickets are running out.
- **Quick actions**, **Tables** (swipe sideways on phones), **Recent reservations**, and for admins **Sales by category** and **Top sellers**; entrance staff see **Door progress** instead.

### Signing out

Open the user menu (your initials, top-right) and choose **Sign out**. Confirm in the dialog. The same menu offers **Switch event**.

---

## 4. Dashboard (Home)

The Home view gives a real-time snapshot of event status.

### Stat cards — row 1 (entity counts)

| Card | What it shows |
|------|---------------|
| Reservations | Total number of reservations in the system |
| Tables | Total tables configured |
| Sellers | Total registered sellers |

### Stat cards — row 2 (seat status)

| Card | What it shows |
|------|---------------|
| Available seats | Seats that have not yet been reserved |
| Reserved seats | Seats linked to a reservation (not yet checked in) |
| Checked-in seats | Seats whose holders have entered the event |

### Quick actions

| Button | Navigates to |
|--------|--------------|
| New Reservation | Reservations form |
| Check In | Check In view |
| Seating Layout | Visual seat map |

### Recent reservations table

Lists the 10 most recently created reservations with reference number, status, seller, seat count, and description.

---

## 5. Management — Tables

> **Role required:** ADMIN

Tables represent physical seating areas in the venue. Each table contains a number of seats.

### Viewing tables

All configured tables are listed with: Table name, Description, Total seats, Available, Unavailable, Reserved.

Use the **search box** to filter by name or description.

### Adding a table

1. Click **Add table**.
2. Fill in the form:

   | Field | Description |
   |-------|-------------|
   | Table name | Display name (e.g. "Table A1") |
   | Table description | Optional notes |
   | Total seats | Total physical seats at this table |
   | Available seats | Seats that can be reserved (≤ total) |
   | Unavailable seats | Blocked or unusable seats (e.g. broken) |

3. Click **Save**.

The system auto-creates individual seat records for the table.

### Editing a table

Right-click a row in the grid and choose **Edit**. Modify the fields and click **Save**.

### Deleting a table

Right-click a row and choose **Delete**. A confirmation dialog prevents accidental deletion.

> **Note:** Deleting a table that has active reservations will fail. Cancel those reservations first.

---

## 6. Management — Sellers

> **Role required:** ADMIN

Sellers are agents or staff responsible for selling tickets. Every reservation must be linked to a seller.

### Adding a seller

1. Click **Add seller**.
2. Fill in: Name *(required)*, Address, Email, Phone Number, Description.
3. Click **Save**.

### Editing / Deleting

Right-click a row and choose **Edit** or **Delete**.

Search the seller list with the search box (filters on name, email, address, description).

---

## 7. Management — Reservations

> **Role required:** ADMIN

A reservation groups one or more seats together under a single booking reference. Seats are auto-assigned from the selected table.

### Creating a reservation

1. Click **New reservation**.
2. Complete the form:

   | Field | Description |
   |-------|-------------|
   | Table | Select the table to assign seats from |
   | Seller | The seller handling this booking |
   | FULL tickets | Number of full-price seats |
   | HALF tickets | Number of half-price seats |
   | INVITATION tickets | Complimentary seats (auto-marked PAID, excluded from revenue) |
   | Description | Optional note (guest name, group, etc.) |

   The label below the table selector shows how many seats are still available.

3. Click **Save**. The system:
   - Auto-assigns available seats from the chosen table
   - Generates a unique reference number
   - Creates a QR ticket for each seat

### Viewing reservation details

Click any row in the grid to open the details dialog. From here you can:

- See seat assignments and ticket numbers
- **Mark as Paid** — changes reservation status to PAID, which enables check-in
- **Download tickets** — downloads the pre-generated QR ticket image(s) as a ZIP

### Filtering

Type in the search box to filter by reference number, seller name, description, or status.

### Reservation statuses

| Status | Meaning |
|--------|---------|
| PENDING | Created, not yet paid |
| PAID | Payment confirmed — check-in is enabled |
| CANCELLED | Cancelled, seats released |

---

## 8. Management — Users

> **Role required:** ADMIN

Manage system login accounts.

### Adding a user

1. Click **New**.
2. Enter: Username, New Password, Role, Enabled (checkbox).
3. Click **Save**.

### Editing a user

Click a row to open the edit dialog. You can change the password, role, or enabled status.

### Deleting a user

Open the edit dialog for the user and click **Delete**. Confirmation is required.

### Roles

| Role | Access |
|------|--------|
| ADMIN | Full access to all sections |
| ENTRANCE | Check In and Seating Layout only (no revenue or reports) |

---

## 9. Management — Ticket Categories

> **Role required:** ADMIN

Ticket categories define the price tiers for seats in a reservation.

### Default categories

| Category | Default price | Notes |
|----------|--------------|-------|
| FULL | €25.00 | Standard full-price ticket |
| HALF | €12.50 | Half-price ticket |
| INVITATION | €0.00 | Complimentary, excluded from revenue |
| STAFF | €0.00 | Staff pass |

### Adding a custom category

1. Click **Add Category**.
2. Fill in: Name, Price (€), Description, Active (toggle).
3. Click **Save**.

### Editing / Deactivating

Click the pencil icon on any row. Deactivating a category (unchecking **Active**) hides it from reservation forms without deleting it.

---

## 10. Management — Settings

> **Role required:** ADMIN

Settings control event metadata, the visual layout canvas, and QR ticket generation parameters.

### Tab: Event

| Field | Description |
|-------|-------------|
| Event name | Printed on QR tickets |
| Venue | Printed on QR tickets |

### Tab: Layout

Controls how the Seating Layout canvas renders:

| Field | Description |
|-------|-------------|
| Table size | Pixel width/height of each table block |
| Seat size | Pixel width/height of each seat circle |
| No. of columns | How many table columns to display across |

### Tab: Ticket & QR

Controls QR code positioning on the ticket image:

| Field | Description |
|-------|-------------|
| Font size | Text size on the ticket |
| QR size | Scale factor for the QR code (1–50) |
| QR X / QR Y | Position of the QR code on the ticket background image |
| Text X / Text Y | Position of the ticket text |

### Tab: Ticket Numbering

| Field | Description |
|-------|-------------|
| Ticket prefix | Optional prefix (e.g. `EVT-`) |
| No. of digits | Number of digits in the ticket number |
| Max no. of tickets | Maximum tickets that can be generated |

### Base image upload

Upload a PNG or JPEG image to use as the ticket background. The QR code and text are overlaid on top of it.

After upload a preview is shown. Click **Save** to commit.

### Generate Tickets button

Triggers pre-generation of QR ticket images for all existing reservations. Use this after changing QR size or position settings to refresh all tickets.

---

## 11. Operations — Check In

> **Role required:** ADMIN or ENTRANCE

The Check In view is used at the event entrance to scan guest tickets and mark them as checked in.

### Summary badges (top of page)

Shows live counts of: Checked-in, Available, Unavailable, Reserved seats.

### Opening the check-in dialog

Click **New checking**. A dialog opens with:

- **QR scanner** — uses your device camera to scan a ticket QR code
- **Manual search** — find a ticket by ticket number, table, or seller

When a ticket is recognised, the **Seat details** dialog opens automatically.

### Seat details dialog

| Field | Description |
|-------|-------------|
| Ticket # | Unique ticket identifier |
| Type | Ticket category (FULL, HALF, INVITATION, etc.) |
| Seller | Who sold this ticket |
| Table | Table assignment |
| Seat | Seat number |
| Status | Current status (PAID, CHECKED_IN, etc.) |

**Check-in** button (red) — marks the seat as CHECKED_IN. Only enabled when the reservation is PAID.

**Undo Check-in** button — appears if the seat is already checked in. Reverts it to PAID status.

**Close** — closes the dialog and resets the scanner.

### Bulk check-in counter

A running counter shows how many seats you have checked in during the current session.

### Recent check-ins list

The lower section of the page shows a grid of recently checked-in seats with timestamp. Click **Refresh** to update it.

### Filter controls

Filter the check-in list by ticket number, table, reservation status, or seller.

### Camera tips

- On **iPhone/iOS**, you must access the app via **HTTPS** for the camera to work.
- The scanner uses the rear camera by default.
- If scanning does not start, close and reopen the dialog.

---

## 12. Operations — Seating Layout

> **Role required:** ADMIN or ENTRANCE

A visual canvas showing all tables and their individual seats colour-coded by status.

### Seat colour legend

| Colour | Status |
|--------|--------|
| Green | Available |
| Orange / Yellow | Reserved |
| Red | Checked-in |
| Grey | Unavailable |

### Downloading the layout

Click **Download Layout** to save the current canvas as a PNG image (timestamped filename).

The canvas is scrollable if the venue is large. The number of columns and tile sizes are configured in **Settings → Layout**.

---

## 13. Operations — Reports & Metrics

> **Role required:** ADMIN

Provides financial summaries and operational statistics.

### Metric cards

**Financial Metrics**

| Metric | Description |
|--------|-------------|
| Total Revenue (Paid) | Sum of all ticket prices for PAID reservations |
| Tickets Sold (Paid) | Count of paid (non-complimentary) tickets |
| Complimentary (INVITATION) | Count of INVITATION-type tickets checked in |
| Avg Ticket Price | Average price per paid ticket |

**Analytical Dashboard**

Shows seat occupancy, check-in rate, and category-level breakdowns.

### Seller breakdowns

- **Seller Sales** — total tickets sold per seller
- **Seller Check-ins** — how many of each seller's tickets have been checked in

### Excel exports

All four export buttons generate `.xlsx` files for download:

| Button | Contents |
|--------|----------|
| Ticket List | All tickets with seat, table, seller, price, status |
| Seller Sales | Ticket counts and revenue per seller |
| Check-in List | All checked-in seats with timestamp |
| Seller Check-ins | Check-in counts per seller |

Click **Refresh Metrics** to reload all figures from the database.

---

## 14. Roles & Permissions

| Feature | ADMIN | ENTRANCE |
|---------|-------|----------|
| Home dashboard | ✅ | ✅ |
| Tables | ✅ | ❌ |
| Sellers | ✅ | ❌ |
| Reservations | ✅ | ❌ |
| Users | ✅ | ❌ |
| Ticket Categories | ✅ | ❌ |
| Settings | ✅ | ❌ |
| Check In | ✅ | ✅ |
| Seating Layout | ✅ | ✅ |
| Reports & Metrics | ✅ | ❌ |

---

## 15. Typical Workflow (End-to-End)

Below is the recommended sequence from setup to event day.

### Before the event

1. **Create tables** — Management → Tables. Add each seating area with its total and available seat count.
2. **Create sellers** — Management → Sellers. Add every agent who will sell tickets.
3. **Configure ticket categories** — Management → Ticket Categories. Adjust prices to match your event.
4. **Upload ticket background image** — Management → Settings → Base image. Upload your branded ticket design.
5. **Configure QR position** — Settings → Ticket & QR tab. Adjust `QR X`, `QR Y`, `Text X`, `Text Y` until the preview looks right. Save.
6. **Click Generate Tickets** — pre-generates QR images so they are ready to download immediately after booking.

### Taking reservations

7. Open **Management → Reservations** → **New reservation**.
8. Select the table and seller, enter ticket counts (FULL / HALF / INVITATION).
9. Save. A reference number is created automatically.
10. Open the reservation row, click **Mark as Paid** once payment is received.
11. Click **Download tickets** to get the QR ticket image(s) to print or send to the guest.

### On event day

12. Staff open **Operations → Check In** on their device (phone or tablet).
13. Click **New checking**, scan the guest's QR code, and click **Check-in**.
14. The counter and recent list update in real time.
15. Use **Operations → Seating Layout** for a live bird's-eye view of the venue.

### After the event

16. Go to **Operations → Reports & Metrics**.
17. Click **Refresh Metrics** for final totals.
18. Export Excel reports as needed.

---

## 16. Troubleshooting

### Camera does not start (iOS/iPhone)

**Cause:** iOS Safari requires HTTPS for camera access.

**Fix:** Access the app via `https://` (not `http://`). If using a custom domain, ensure the SSL certificate is valid.

### QR code not scanning on iPhone

**Cause:** iOS camera is strict about the QR code quiet zone (white border).

**Fix:** Go to **Settings → Ticket & QR**, increase the QR size or check that the ticket background leaves enough white space around the QR area. Re-generate tickets after saving.

### "Check-in allowed only when reservation is PAID" message

**Fix:** Go to **Reservations**, find the reservation, and click **Mark as Paid** before attempting check-in.

### Reservation creation fails — "No seats available"

**Fix:** Either choose a different table, or go to **Tables** and add more seats to the existing table.

### Tickets not downloading

**Fix:** Click **Generate Tickets** in Settings to pre-generate ticket images, then retry the download from the Reservations view.

### Page shows "Access denied"

**Cause:** Your account role does not have permission for that page.

**Fix:** Log in with an ADMIN account, or ask an admin to adjust your role in **Users**.

### Cannot delete a table or seller

**Cause:** The item is linked to existing reservations.

**Fix:** Cancel all associated reservations before deleting.

---

*For technical operations (Docker deployment, database backup/restore, SSL configuration), see the project's* `README.md` *and* `docs/` *folder.*

---

## 17. Events (Multiple Campaigns)

> **Role required:** ADMIN (menu **Management → Events**)

The system can run **several events at the same time** and keeps every previous event. Every event has its own settings and ticket
template, tickets, ticket categories, tables, seats, floor layout, sellers, reservations and reports. User logins
(admin / entrance) are shared by all events.

### Event statuses

| Status | Meaning |
|--------|---------|
| **DRAFT** | Being prepared. Admins can set it up (tables, sellers, tickets, …). Check-in is not possible. |
| **ACTIVE** | A running event. Several events can be active at once; check-in works for the event selected after login. |
| **ARCHIVED** | Finished. **Read-only**: reports and exports stay available; no changes, no check-in. Seller portal links stop working. |

### Selecting an event after login

After signing in, every user must **select an event** before any page opens. The list shows:

- **Admins:** all events that are not archived.
- **Other users (e.g. ENTRANCE):** only the non-archived events **assigned to them** in **Management → Users**
  (field *Assigned events*). A user with no assignment cannot open any event page.

Archived events are never listed here. The selected event is shown in the header; use **Switch event** to pick
another one. Removing an assignment or archiving an event takes effect immediately for logged-in users.

### Starting the next event

1. Take a database backup.
2. **Events → New Event**: enter name, venue and date. Optionally choose an event to **copy setup from** and tick what
   to copy: ticket template & layout settings, ticket categories & prices, tables/seats & floor layout, sellers.
   Tickets and reservations are never copied. Copied sellers get **new** portal links (same password) — send them the
   new links.
3. Click **Open** on the new draft to work on it (only your own session switches).
   Assign the event to the entrance users in **Users** so they can select it.
4. Adjust **Settings** (check the ticket prefix), generate tickets, prepare tables and sellers.
5. Back in **Events**, click **Activate** on the draft and type its name to confirm. Other active events stay active;
   tick **Also archive the currently active event(s)** if the previous event is over.

### Viewing a past event

Admins: click **Open** on an archived event in **Events** to view its reservations, seating and reports (read-only).
Use **Switch event** in the header to return.

### Security notes

- Ticket numbers are unique across **all** events, so a QR code from a previous event is always rejected at a later event.
- Check-in works only for ACTIVE events, and only for tickets of the event selected in that session. A ticket
  of another event is shown as "belongs to a different event".
- Only admins can manage events. Non-admin users can only select events assigned to them, never archived ones.
- There is no default event: every session must explicitly select an event it is allowed to access.
- On the first start after the upgrade, all existing data is automatically assigned to an event named after the
  current Settings event name, which stays ACTIVE, and all existing users are assigned to it (one time only).

