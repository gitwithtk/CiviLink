package civilink.ui;

import civilink.controller.ComplaintController;
import civilink.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import java.util.List;

/**
 * OfficerDashboard
 * Sidebar: My Tasks | View Timeline | My Profile
 * No duplicate tabs or buttons — sidebar is the only navigation.
 * UC6 – View Assigned Complaints
 * UC7 – Update Complaint Status
 * UC3 – View Timeline
 */
public class OfficerDashboard {

    private final Officer officer;
    private final Stage   primaryStage;
    private Stage         dashStage;

    private final ObservableList<Complaint> data = FXCollections.observableArrayList();
    private TableView<Complaint> table;

    // Center content pane (swapped by sidebar)
    private BorderPane body;

    public OfficerDashboard(Officer officer, Stage primaryStage) {
        this.officer = officer; this.primaryStage = primaryStage;
    }

    public void show() {
        dashStage = new Stage();
        dashStage.setTitle("CiviLink – Officer Dashboard");
        dashStage.setScene(buildScene());
        dashStage.setMinWidth(960);
        dashStage.setMinHeight(600);
        dashStage.show();
        primaryStage.hide();
        loadComplaints();
    }

    @SuppressWarnings("unchecked")
    private Scene buildScene() {
        HBox nav = UIUtils.buildNavBarWithBell(
            "Officer  |  " + officer.getDepartment(),
            officer.getUsername(), officer.getOfficerID(), this::logout);

        // ── Sidebar – 3 unique items, no duplicates ────────
        Button btnTasks   = UIUtils.sidebarButton("📋", "My Tasks");
        Button btnTimeline = UIUtils.sidebarButton("📅", "View Timeline");
        Button btnProfile  = UIUtils.sidebarButton("👤", "My Profile");

        List<Button> sideAll = List.of(btnTasks, btnTimeline, btnProfile);
        UIUtils.activateSideBtn(btnTasks);

        Label navHead = new Label("Navigation");
        navHead.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        navHead.setTextFill(Color.web("#aaaaaa"));
        navHead.setPadding(new Insets(0, 0, 8, 16));

        VBox sidebar = new VBox(4, navHead, btnTasks, btnTimeline, btnProfile);
        sidebar.setPadding(new Insets(20, 8, 20, 8));
        sidebar.setPrefWidth(205);
        sidebar.setStyle("-fx-background-color:white;-fx-border-color:#e8eef5;-fx-border-width:0 1 0 0;");

        // ── Body (swappable center) ───────────────────────
        body = new BorderPane();
        body.setCenter(buildTasksPane());
        body.setStyle("-fx-background-color:" + UIUtils.BG_LIGHT + ";");

        // Sidebar actions
        btnTasks.setOnAction(e -> {
            activateSide(btnTasks, sideAll);
            body.setCenter(buildTasksPane());
            loadComplaints();
        });
        btnTimeline.setOnAction(e -> {
            activateSide(btnTimeline, sideAll);
            body.setCenter(buildTimelinePane());
        });
        btnProfile.setOnAction(e -> {
            activateSide(btnProfile, sideAll);
            body.setCenter(buildProfilePane());
        });

        BorderPane root = new BorderPane();
        root.setTop(nav);
        root.setLeft(sidebar);
        root.setCenter(body);
        return new Scene(root, 960, 600);
    }

    // ── UC6 Tasks pane ────────────────────────────────────
    @SuppressWarnings("unchecked")
    private VBox buildTasksPane() {
        // Stat row
        long overdueCount = data.stream().filter(Complaint::isOverdue).count();
        HBox stats = new HBox(14,
            miniStat("Active Tasks",   String.valueOf(data.size()),    "#2563a8"),
            miniStat("Overdue",        String.valueOf(overdueCount),    "#791f1f"),
            miniStat("Department",     officer.getDepartment(),         "#3c3489")
        );
        stats.setPadding(new Insets(20, 20, 16, 20));

        // Buttons – appear ONCE here (not on sidebar)
        Button btnRefresh = UIUtils.secondaryButton("⟳  Refresh");
        Button btnDetails = UIUtils.secondaryButton("Details");
        Button btnUpdate  = UIUtils.primaryButton("Update Status");

        btnRefresh.setOnAction(e -> loadComplaints());

        btnDetails.setOnAction(e -> {
            Complaint sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { UIUtils.showError("No Selection", "Please select a complaint."); return; }
            showDetails(sel);
        });

        // ─────────────────────────────────────────────────────────
        //  FIX (UI layer) – Update button handler
        //
        //  Before opening the update dialog we call canOfficerUpdate()
        //  which checks two things:
        //    1. Is the complaint in a state where updates are allowed?
        //       (blocks PENDING and REOPENED — those need manager to assign first)
        //    2. Is THIS officer the currently assigned officer?
        //       (blocks O1 after manager re-assigns to O2)
        //
        //  This gives the officer a clear error message instead of a
        //  silent failure, and avoids even opening the dialog.
        // ─────────────────────────────────────────────────────────
        btnUpdate.setOnAction(e -> {
            Complaint sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                UIUtils.showError("No Selection", "Please select a complaint.");
                return;
            }

            // ── NEW: check ownership and state ───────────
            String currentStatus = sel.getStatus();

            if ("PENDING".equals(currentStatus) || "REOPENED".equals(currentStatus)) {
                // Bug 1 fix: complaint is reopened but not yet re-assigned by manager
                UIUtils.showError("Update Not Allowed",
                    "This complaint is currently '" + currentStatus + "'.\n" +
                    "The manager must assign an officer to it before\n" +
                    "any status update is permitted.");
                return;
            }

            if (!ComplaintController.getInstance()
                    .canOfficerUpdate(sel.getComplaintID(), officer.getOfficerID())) {
                // Bug 2 fix: this officer is no longer the assigned officer
                UIUtils.showError("Update Not Allowed",
                    "You are not the currently assigned officer\n" +
                    "for complaint " + sel.getTrackingID() + ".\n\n" +
                    "It may have been re-assigned to another officer.");
                return;
            }
            // ─────────────────────────────────────────────

            showUpdateDialog(sel);
        });

        HBox tblBar = new HBox(10,
            UIUtils.sectionHeading("My Assigned Complaints"),
            new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }},
            btnDetails, btnUpdate, btnRefresh);
        tblBar.setAlignment(Pos.CENTER_LEFT);
        tblBar.setPadding(new Insets(0, 20, 12, 20));

        // Table
        TableColumn<Complaint, String> colID   = col("Tracking ID",  c -> c.getTrackingID(), 145);
        TableColumn<Complaint, String> colStat = col("Status",       c -> c.getStatus(),     120);
        TableColumn<Complaint, String> colLoc  = col("Location",     c -> c.getLocation(),   150);
        TableColumn<Complaint, String> colDate = col("Submitted",
            c -> c.getSubmissionDate().toString().replace("T"," ").substring(0,16), 140);
        TableColumn<Complaint, String> colDue  = col("Expected By",
            c -> c.getExpectedResDate() != null ? c.getExpectedResDate().toString() : "N/A", 110);
        TableColumn<Complaint, String> colOD   = col("Overdue",
            c -> c.isOverdue() ? "⚠ YES" : "—", 80);

        table = new TableView<>(data);
        table.getColumns().addAll(colID, colStat, colLoc, colDate, colDue, colOD);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("You currently have no pending tasks."));
        table.setStyle("-fx-background-color:white;");
        VBox.setVgrow(table, Priority.ALWAYS);

        table.setRowFactory(t -> new TableRow<>() {
            @Override protected void updateItem(Complaint c, boolean empty) {
                super.updateItem(c, empty);
                if (c == null || empty) { setStyle(""); return; }
                // ── NEW: highlight REOPENED rows in amber to signal "read-only" ──
                if ("REOPENED".equals(c.getStatus()))  setStyle("-fx-background-color:#fff8e7;");
                else if (c.isOverdue())                setStyle("-fx-background-color:#fff5f5;");
                else if ("RESOLVED".equals(c.getStatus())) setStyle("-fx-background-color:#f5fbf0;");
                else setStyle("");
            }
        });

        VBox pane = new VBox(0, stats, tblBar, table);
        pane.setStyle("-fx-background-color:" + UIUtils.BG_LIGHT + ";");
        VBox.setVgrow(table, Priority.ALWAYS);
        return pane;
    }

    // ── UC3 Timeline pane ─────────────────────────────────
    private VBox buildTimelinePane() {
        VBox pane = new VBox(16);
        pane.setPadding(new Insets(28));
        pane.setStyle("-fx-background-color:" + UIUtils.BG_LIGHT + ";");

        Label head = UIUtils.sectionHeading("📅  View Complaint Timeline");

        TextField tfID = UIUtils.styledField("Enter Tracking ID  (e.g. CVL-A1B2C3D4)", 500);
        Label lblErr = errLbl();

        VBox tlBox = new VBox(0);
        tlBox.setVisible(false);
        tlBox.setStyle("-fx-background-color:white;-fx-background-radius:10;" +
                       "-fx-border-color:#e8eef5;-fx-border-radius:10;-fx-border-width:1;");

        Button btnLoad = UIUtils.primaryButton("Load Timeline");
        btnLoad.setOnAction(e -> {
            lblErr.setText(""); tlBox.getChildren().clear(); tlBox.setVisible(false);
            Complaint c = ComplaintController.getInstance().trackComplaint(tfID.getText().trim());
            if (c == null) { lblErr.setText("Complaint not found."); return; }
            List<ComplaintTimeline> tl = ComplaintController.getInstance().getTimeline(c.getComplaintID());
            if (tl.isEmpty()) {
                tlBox.getChildren().add(tlItem("PENDING", "Complaint submitted and pending review.", ""));
            } else {
                for (ComplaintTimeline t : tl)
                    tlBox.getChildren().add(tlItem(t.getStatus(),
                        t.getNotes() != null ? t.getNotes() : "",
                        t.getUpdateTime().toString().replace("T"," ").substring(0,16)));
            }
            tlBox.setVisible(true);
        });

        ScrollPane sp = new ScrollPane(tlBox);
        sp.setFitToWidth(true); sp.setPrefHeight(320);
        sp.setStyle("-fx-background-color:transparent;-fx-border-color:transparent;");

        pane.getChildren().addAll(head,
            UIUtils.fieldLabel("Tracking ID *"), tfID, btnLoad, lblErr, sp);
        VBox.setVgrow(sp, Priority.ALWAYS);
        return pane;
    }

    // ── My Profile pane ───────────────────────────────────
    private VBox buildProfilePane() {
        VBox pane = new VBox(16);
        pane.setPadding(new Insets(28));
        pane.setStyle("-fx-background-color:" + UIUtils.BG_LIGHT + ";");

        Label head = UIUtils.sectionHeading("👤  My Profile");

        // Avatar circle with initials
        String initials = officer.getName().length() >= 2
            ? officer.getName().substring(0, 2).toUpperCase() : "OF";
        Label avatar = new Label(initials);
        avatar.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        avatar.setTextFill(Color.web(UIUtils.BLUE_DARK));
        avatar.setStyle("-fx-background-color:#ddeaf8;-fx-background-radius:50;" +
                        "-fx-padding:20 24;");

        VBox card = new VBox(12,
            profRow("Full Name",   officer.getName()),
            profRow("Username",    officer.getUsername()),
            profRow("Email",       officer.getEmail()),
            profRow("Phone",       officer.getPhone() != null ? officer.getPhone() : "—"),
            profRow("Department",  officer.getDepartment()),
            profRow("Role",        "Officer"),
            profRow("Status",      "Active")
        );
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color:white;-fx-background-radius:12;" +
                      "-fx-border-color:#e8eef5;-fx-border-width:1;-fx-border-radius:12;");
        card.setMaxWidth(460);

        HBox avatarRow = new HBox(20, avatar, card);
        avatarRow.setAlignment(Pos.TOP_LEFT);

        pane.getChildren().addAll(head, avatarRow);
        return pane;
    }

    private HBox profRow(String key, String val) {
        Label k = new Label(key + ":");
        k.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        k.setTextFill(Color.web("#666666")); k.setPrefWidth(110);
        Label v = new Label(val);
        v.setFont(Font.font("Arial", 13));
        v.setTextFill(Color.web("#222222"));
        HBox row = new HBox(10, k, v); row.setPadding(new Insets(6, 0, 6, 0));
        row.setStyle("-fx-border-color:#f0f4f8;-fx-border-width:0 0 1 0;");
        return row;
    }

    private void loadComplaints() {
        data.clear();
        data.addAll(ComplaintController.getInstance().getComplaintsForOfficer(officer.getOfficerID()));
    }

    // ── Details popup ─────────────────────────────────────
    private void showDetails(Complaint c) {
        Stage sub = new Stage(); sub.setTitle("Details"); sub.initOwner(dashStage);
        VBox form = modalShell("📋  Complaint Details");
        VBox det = new VBox(0,
            detRow("Tracking ID", c.getTrackingID()), detRow("Status", c.getStatus()),
            detRow("Location", c.getLocation()), detRow("Description", c.getDescription()),
            detRow("Submitted", c.getSubmissionDate().toString().replace("T"," ").substring(0,16)),
            detRow("Expected By", c.getExpectedResDate() != null ? c.getExpectedResDate().toString() : "N/A"),
            detRow("Overdue", c.isOverdue() ? "YES ⚠" : "No"));
        det.setStyle("-fx-background-color:#f8fbff;-fx-background-radius:10;" +
                     "-fx-border-color:#e8eef5;-fx-border-radius:10;-fx-border-width:1;");
        Button cl = UIUtils.secondaryButton("Close"); cl.setOnAction(e -> sub.close());
        form.getChildren().addAll(det, cl);
        sub.setScene(new Scene(form, 480, 420)); sub.show();
    }

    // ── UC7 Update status ─────────────────────────────────
    private void showUpdateDialog(Complaint c) {
        Stage sub = new Stage();
        sub.setTitle("Update – " + c.getTrackingID()); sub.initOwner(dashStage);
        VBox form = modalShell("✏️  Update Complaint Status");

        HBox curRow = new HBox(10,
            new Label("Current:") {{ setFont(Font.font("Arial", FontWeight.BOLD, 12));
                                     setTextFill(Color.web("#666666")); }},
            UIUtils.statusBadge(c.getStatus()));
        curRow.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> cbStatus = new ComboBox<>();
        cbStatus.getItems().addAll("IN_PROGRESS", "AWAITING", "RESOLVED");
        cbStatus.setPromptText("Select new status"); cbStatus.setPrefWidth(420);
        cbStatus.setStyle("-fx-font-size:13;-fx-padding:4;-fx-background-radius:8;-fx-border-color:#dde3ed;-fx-border-radius:8;");

        TextArea taNotes = UIUtils.styledArea("Add update notes (required if Resolving)...", 420, 90);
        Label hint = new Label("Resolution notes are mandatory when marking as RESOLVED.");
        hint.setFont(Font.font("Arial", FontPosture.ITALIC, 11));
        hint.setTextFill(Color.web("#888888"));
        Label lblErr = errLbl();

        Button btnUpd = UIUtils.primaryButton("Confirm Update");
        Button btnCan = UIUtils.secondaryButton("Cancel");
        btnCan.setOnAction(e -> sub.close());

        btnUpd.setOnAction(e -> {
            lblErr.setText("");
            String ns = cbStatus.getValue(); String notes = taNotes.getText().trim();
            if (ns == null) { lblErr.setText("Please select a new status."); return; }
            if ("RESOLVED".equals(ns) && notes.isEmpty()) {
                lblErr.setText("Resolution notes are required when marking as Resolved."); return;
            }
            boolean ok = ComplaintController.getInstance()
                .updateComplaintStatus(c.getComplaintID(), ns, notes, officer.getOfficerID());
            if (ok) {
                UIUtils.showInfo("Status Updated ✓", "Complaint " + c.getTrackingID() + "\nupdated to: " + ns);
                sub.close(); loadComplaints();
                body.setCenter(buildTasksPane());
            } else {
                // The controller blocked it (should not normally reach here because
                // we already checked canOfficerUpdate before opening this dialog,
                // but kept as a safety net)
                lblErr.setText("Update failed: you may no longer be the assigned officer.");
            }
        });

        form.getChildren().addAll(curRow,
            UIUtils.fieldLabel("New Status *"), cbStatus,
            UIUtils.fieldLabel("Notes"), taNotes,
            hint, lblErr, new HBox(10, btnUpd, btnCan));
        sub.setScene(new Scene(form, 470, 420)); sub.show();
    }

    // ── Helpers ───────────────────────────────────────────
    private void activateSide(Button btn, List<Button> all) {
        all.forEach(UIUtils::deactivateSideBtn);
        UIUtils.activateSideBtn(btn);
    }

    private HBox miniStat(String label, String val, String color) {
        Label num = new Label(val);
        num.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        num.setTextFill(Color.web(color));
        Label lbl = new Label(label);
        lbl.setFont(Font.font("Arial", 11)); lbl.setTextFill(Color.web("#888888"));
        VBox box = new VBox(3, num, lbl);
        box.setPadding(new Insets(14, 20, 14, 20));
        box.setStyle("-fx-background-color:white;-fx-background-radius:10;" +
                     "-fx-border-color:#e8eef5;-fx-border-width:1;-fx-border-radius:10;");
        HBox.setHgrow(box, Priority.ALWAYS);
        return new HBox(box) {{ HBox.setHgrow(this, Priority.ALWAYS); }};
    }

    private VBox tlItem(String status, String note, String time) {
        Label dot = new Label("●"); dot.setFont(Font.font(9));
        dot.setTextFill(Color.web(
            "RESOLVED".equals(status) ? "#27500a" : "PENDING".equals(status) ? "#633806" : UIUtils.BLUE_MID));
        Label st = new Label(status);
        st.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        st.setTextFill(Color.web(UIUtils.BLUE_DARK));
        HBox top = new HBox(8, dot, st); top.setAlignment(Pos.CENTER_LEFT);
        VBox item = new VBox(3, top);
        if (!note.isEmpty()) {
            Label nl = new Label(note); nl.setFont(Font.font("Arial", 12));
            nl.setTextFill(Color.web("#555555")); nl.setPadding(new Insets(0,0,0,20));
            nl.setWrapText(true); item.getChildren().add(nl);
        }
        if (!time.isEmpty()) {
            Label tl = new Label(time); tl.setFont(Font.font("Arial", FontPosture.ITALIC, 11));
            tl.setTextFill(Color.web("#aaaaaa")); tl.setPadding(new Insets(0,0,0,20));
            item.getChildren().add(tl);
        }
        item.setPadding(new Insets(10,14,10,14));
        item.setStyle("-fx-border-color:#e8eef5;-fx-border-width:0 0 1 0;");
        return item;
    }

    private HBox detRow(String key, String val) {
        Label k = new Label(key + ":"); k.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        k.setTextFill(Color.web("#666666")); k.setPrefWidth(110);
        Label v = new Label(val); v.setFont(Font.font("Arial", 12));
        v.setWrapText(true); v.setMaxWidth(300);
        HBox row = new HBox(8, k, v); row.setPadding(new Insets(8,14,8,14));
        row.setStyle("-fx-border-color:#e8eef5;-fx-border-width:0 0 1 0;");
        return row;
    }

    private VBox modalShell(String title) {
        Label lbl = new Label(title); lbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lbl.setTextFill(Color.web(UIUtils.BLUE_DARK));
        Separator sep = new Separator(); sep.setPadding(new Insets(4,0,4,0));
        VBox box = new VBox(10, lbl, sep);
        box.setPadding(new Insets(28)); box.setStyle("-fx-background-color:white;");
        return box;
    }

    private Label errLbl() {
        Label l = new Label(""); l.setTextFill(Color.web("#e24b4a"));
        l.setFont(Font.font("Arial", 12)); return l;
    }

    private <T> TableColumn<T, String> col(String title,
            java.util.function.Function<T, String> fn, double w) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        c.setPrefWidth(w); return c;
    }

    private void logout() { dashStage.close(); primaryStage.show(); }
}