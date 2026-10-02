package civilink.ui;

import civilink.controller.*;
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
import java.time.LocalDate;
import java.util.List;

/**
 * ManagerDashboard
 * Sidebar drives ALL navigation — no duplicate tabs/buttons in the main area.
 * Each sidebar click swaps the entire center content pane.
 * UC8  – Assign Complaint
 * UC9  – Monitor Officer Workload
 * UC10 – Overdue Complaints
 * UC11 – Generate Reports
 * UC12 – Manage Officer Accounts
 */
public class ManagerDashboard {

    private final Manager manager;
    private final Stage   primaryStage;
    private Stage         dashStage;

    private BorderPane centerPane;  // swapped by sidebar
    private final ObservableList<Complaint> tableData = FXCollections.observableArrayList();
    private TableView<Complaint> mainTable;

    // Live stat labels
    private Label statTotal, statResolved, statOverdue, statUnassigned;

    public ManagerDashboard(Manager manager, Stage primaryStage) {
        this.manager = manager; this.primaryStage = primaryStage;
    }

    public void show() {
        dashStage = new Stage();
        dashStage.setTitle("CiviLink – Manager Dashboard");
        dashStage.setScene(buildScene());
        dashStage.setMinWidth(1100);
        dashStage.setMinHeight(660);
        dashStage.show();
        primaryStage.hide();
        int flagged = ComplaintController.getInstance().checkAndFlagOverdue();
        if (flagged > 0)
            UIUtils.showInfo("Overdue Alert", flagged + " complaint(s) flagged as overdue.");
        loadAll();
    }

    private Scene buildScene() {
        HBox nav = UIUtils.buildNavBarWithBell("Manager Dashboard",
            manager.getUsername(), manager.getManagerID(), this::logout);

        // ── Sidebar – unique items only ───────────────────
        Button btnAll        = UIUtils.sidebarButton("📋", "All Complaints");
        Button btnUnassigned = UIUtils.sidebarButton("📥", "Unassigned Queue");
        Button btnOverdue    = UIUtils.sidebarButton("⚠", "Overdue");
        Button btnWorkload   = UIUtils.sidebarButton("📊", "Officer Workload");
        Button btnReports    = UIUtils.sidebarButton("📈", "Reports");
        Button btnOfficers   = UIUtils.sidebarButton("👥", "Manage Officers");

        List<Button> sideAll = List.of(
            btnAll, btnUnassigned, btnOverdue, btnWorkload, btnReports, btnOfficers);
        UIUtils.activateSideBtn(btnAll);

        Label navHead = new Label("Management");
        navHead.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        navHead.setTextFill(Color.web("#aaaaaa"));
        navHead.setPadding(new Insets(0, 0, 8, 16));

        VBox sidebar = new VBox(4, navHead,
            btnAll, btnUnassigned, btnOverdue, btnWorkload, btnReports, btnOfficers);
        sidebar.setPadding(new Insets(20, 8, 20, 8));
        sidebar.setPrefWidth(210);
        sidebar.setStyle("-fx-background-color:white;-fx-border-color:#e8eef5;-fx-border-width:0 1 0 0;");

        // ── Center swappable pane ─────────────────────────
        centerPane = new BorderPane();
        centerPane.setCenter(buildAllComplaintsPane("All Complaints"));
        centerPane.setStyle("-fx-background-color:" + UIUtils.BG_LIGHT + ";");

        // Sidebar wiring
        btnAll.setOnAction(e -> {
            activateSide(btnAll, sideAll);
            centerPane.setCenter(buildAllComplaintsPane("All Complaints"));
            loadAll();
        });
        btnUnassigned.setOnAction(e -> {
            activateSide(btnUnassigned, sideAll);
            centerPane.setCenter(buildAllComplaintsPane("Unassigned Queue"));
            tableData.clear();
            tableData.addAll(ComplaintController.getInstance().getUnassignedComplaints());
        });
        btnOverdue.setOnAction(e -> {
            activateSide(btnOverdue, sideAll);
            centerPane.setCenter(buildAllComplaintsPane("⚠  Overdue Complaints"));
            tableData.clear();
            tableData.addAll(ComplaintController.getInstance().getOverdueComplaints());
        });
        btnWorkload.setOnAction(e -> {
            activateSide(btnWorkload, sideAll);
            centerPane.setCenter(buildWorkloadPane());
        });
        btnReports.setOnAction(e -> {
            activateSide(btnReports, sideAll);
            centerPane.setCenter(buildReportsPane());
        });
        btnOfficers.setOnAction(e -> {
            activateSide(btnOfficers, sideAll);
            centerPane.setCenter(buildOfficersPane());
        });

        BorderPane root = new BorderPane();
        root.setTop(nav);
        root.setLeft(sidebar);
        root.setCenter(centerPane);
        return new Scene(root, 1120, 660);
    }

    // ── All / Unassigned / Overdue complaints pane ────────
    @SuppressWarnings("unchecked")
    private VBox buildAllComplaintsPane(String title) {
        // Stat cards row (only on All Complaints)
        statTotal      = statNum("—");
        statResolved   = statNum("—");
        statOverdue    = statNum("—");
        statUnassigned = statNum("—");

        HBox stats = new HBox(14,
            statCard("Total Complaints",  statTotal,      "#2563a8"),
            statCard("Resolved",          statResolved,   "#0f6e56"),
            statCard("Overdue",           statOverdue,    "#791f1f"),
            statCard("Unassigned",        statUnassigned, "#633806")
        );
        stats.setPadding(new Insets(20, 20, 16, 20));

        // Table header – Assign button is here (not on sidebar)
        Label head = new Label(title);
        head.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        head.setTextFill(Color.web(UIUtils.BLUE_DARK));

        Button btnAssign  = UIUtils.primaryButton("Assign to Officer");
        Button btnRefresh = UIUtils.secondaryButton("⟳  Refresh");

        btnAssign.setOnAction(e -> {
            Complaint sel = mainTable.getSelectionModel().getSelectedItem();
            if (sel == null) { UIUtils.showError("No Selection", "Select a complaint first."); return; }
            showAssignDialog(sel);
        });
        btnRefresh.setOnAction(e -> loadAll());

        HBox tblBar = new HBox(10, head,
            new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }},
            btnAssign, btnRefresh);
        tblBar.setAlignment(Pos.CENTER_LEFT);
        tblBar.setPadding(new Insets(0, 20, 12, 20));

        // Table
        TableColumn<Complaint, String> colID   = col("Tracking ID",  c -> c.getTrackingID(), 150);
        TableColumn<Complaint, String> colStat = col("Status",       c -> c.getStatus(),     120);
        TableColumn<Complaint, String> colLoc  = col("Location",     c -> c.getLocation(),   155);
        TableColumn<Complaint, String> colDate = col("Submitted",
            c -> c.getSubmissionDate().toString().replace("T"," ").substring(0,16), 140);
        TableColumn<Complaint, String> colDue  = col("Expected By",
            c -> c.getExpectedResDate() != null ? c.getExpectedResDate().toString() : "N/A", 110);
        TableColumn<Complaint, String> colOD   = col("Overdue",
            c -> c.isOverdue() ? "⚠ YES" : "—", 80);

        mainTable = new TableView<>(tableData);
        mainTable.getColumns().addAll(colID, colStat, colLoc, colDate, colDue, colOD);
        mainTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        mainTable.setPlaceholder(new Label("No complaints found."));
        mainTable.setStyle("-fx-background-color:white;");
        VBox.setVgrow(mainTable, Priority.ALWAYS);

        mainTable.setRowFactory(t -> new TableRow<>() {
            @Override protected void updateItem(Complaint c, boolean empty) {
                super.updateItem(c, empty);
                if (c == null || empty) { setStyle(""); return; }
                if (c.isOverdue())                    setStyle("-fx-background-color:#fff5f5;");
                else if ("RESOLVED".equals(c.getStatus())) setStyle("-fx-background-color:#f5fbf0;");
                else if ("PENDING".equals(c.getStatus()))  setStyle("-fx-background-color:#fffbf0;");
                else setStyle("");
            }
        });

        VBox pane = new VBox(0, stats, tblBar, mainTable);
        pane.setStyle("-fx-background-color:" + UIUtils.BG_LIGHT + ";");
        VBox.setVgrow(mainTable, Priority.ALWAYS);
        return pane;
    }

    private void loadAll() {
        tableData.clear();
        List<Complaint> all = ComplaintController.getInstance().getAllComplaints();
        tableData.addAll(all);
        if (statTotal != null) {
            statTotal.setText(String.valueOf(all.size()));
            statResolved.setText(String.valueOf(all.stream().filter(c -> "RESOLVED".equals(c.getStatus())).count()));
            statOverdue.setText(String.valueOf(all.stream().filter(Complaint::isOverdue).count()));
            statUnassigned.setText(String.valueOf(all.stream().filter(c -> "PENDING".equals(c.getStatus())).count()));
        }
    }

    // ── UC9 Workload pane (replaces popup) ────────────────
    private VBox buildWorkloadPane() {
        VBox pane = new VBox(16);
        pane.setPadding(new Insets(28));
        pane.setStyle("-fx-background-color:" + UIUtils.BG_LIGHT + ";");

        Label head = UIUtils.sectionHeading("📊  Officer Workload Dashboard");

        List<int[]> summary = AssignmentController.getInstance().getWorkloadSummary();
        List<Officer> officers = AuthController.getInstance().getAllActiveOfficers();

        if (summary.isEmpty()) {
            pane.getChildren().addAll(head,
                new Label("No workload data available yet. Assign some complaints first.") {{
                    setFont(Font.font("Arial", 13)); setTextFill(Color.web("#888888"));
                }});
            return pane;
        }

        int maxActive = summary.stream().mapToInt(a -> a[1]).max().orElse(1);
        VBox barsBox = new VBox(20);

        for (int[] row : summary) {
            int oid = row[0]; int active = row[1]; int resolved = row[2];
            String name = officers.stream().filter(o -> o.getOfficerID() == oid)
                .findFirst().map(o -> o.getName() + "  (" + o.getDepartment() + ")")
                .orElse("Officer " + oid);

            HBox nameRow = new HBox(10,
                new Label(name) {{ setFont(Font.font("Arial", FontWeight.BOLD, 13));
                                   setTextFill(Color.web(UIUtils.BLUE_DARK)); }},
                new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }},
                new Label(active + " active") {{ setFont(Font.font("Arial", 12));
                                                 setTextFill(Color.web("#791f1f")); }},
                new Label("  " + resolved + " resolved") {{ setFont(Font.font("Arial", 12));
                                                             setTextFill(Color.web("#27500a")); }}
            );
            nameRow.setAlignment(Pos.CENTER_LEFT);

            double pct = maxActive > 0 ? (double) active / maxActive : 0;
            HBox barBg = new HBox();
            barBg.setStyle("-fx-background-color:#e8eef5;-fx-background-radius:6;");
            barBg.setPrefHeight(12);
            HBox barFill = new HBox();
            barFill.setStyle("-fx-background-color:" + (active > 5 ? "#e24b4a" : UIUtils.BLUE_MID) +
                             ";-fx-background-radius:6;");
            barFill.setPrefWidth(pct * 600); barFill.setPrefHeight(12);
            barBg.getChildren().add(barFill);

            VBox item = new VBox(6, nameRow, barBg);
            item.setPadding(new Insets(14, 18, 14, 18));
            item.setStyle("-fx-background-color:white;-fx-background-radius:10;" +
                          "-fx-border-color:#e8eef5;-fx-border-width:1;-fx-border-radius:10;");
            barsBox.getChildren().add(item);
        }

        ScrollPane sp = new ScrollPane(barsBox);
        sp.setFitToWidth(true); sp.setStyle("-fx-background-color:transparent;-fx-border-color:transparent;");
        VBox.setVgrow(sp, Priority.ALWAYS);

        pane.getChildren().addAll(head, sp);
        VBox.setVgrow(sp, Priority.ALWAYS);
        return pane;
    }

    // ── UC11 Reports pane ─────────────────────────────────
    private VBox buildReportsPane() {
        VBox pane = new VBox(14);
        pane.setPadding(new Insets(28));
        pane.setStyle("-fx-background-color:" + UIUtils.BG_LIGHT + ";");

        Label head = UIUtils.sectionHeading("📈  Generate Performance Report");

        TextField tfFrom = UIUtils.styledField("From (yyyy-MM-dd)", 200);
        TextField tfTo   = UIUtils.styledField("To   (yyyy-MM-dd)", 200);
        tfFrom.setText(LocalDate.now().minusDays(30).toString());
        tfTo.setText(LocalDate.now().toString());

        HBox dateRow = new HBox(10,
            new Label("From:") {{ setFont(Font.font("Arial", FontWeight.BOLD, 12)); setTextFill(Color.web("#444444")); }},
            tfFrom,
            new Label("To:") {{ setFont(Font.font("Arial", FontWeight.BOLD, 12)); setTextFill(Color.web("#444444")); }},
            tfTo);
        dateRow.setAlignment(Pos.CENTER_LEFT);

        TextArea taResult = new TextArea();
        taResult.setEditable(false);
        taResult.setStyle("-fx-font-family:monospace;-fx-font-size:13;-fx-background-radius:10;");
        VBox.setVgrow(taResult, Priority.ALWAYS);

        Label lblErr = new Label(""); lblErr.setTextFill(Color.web("#e24b4a"));
        Button btnGen = UIUtils.primaryButton("Generate Report");
        btnGen.setOnAction(e -> {
            lblErr.setText("");
            if (tfFrom.getText().isBlank() || tfTo.getText().isBlank()) {
                lblErr.setText("Both dates are required."); return;
            }
            taResult.setText(ReportController.getInstance()
                .generateReport(tfFrom.getText().trim(), tfTo.getText().trim(), manager.getManagerID()));
        });

        pane.getChildren().addAll(head, dateRow, btnGen, lblErr, taResult);
        VBox.setVgrow(taResult, Priority.ALWAYS);
        return pane;
    }

    // ── UC12 Manage Officers pane ─────────────────────────
    private VBox buildOfficersPane() {
        VBox pane = new VBox(16);
        pane.setPadding(new Insets(28));
        pane.setStyle("-fx-background-color:" + UIUtils.BG_LIGHT + ";");

        Label head = UIUtils.sectionHeading("👥  Officer Account Management");

        // ── Create card ───────────────────────────────────
        Label createHead = new Label("Add New Officer");
        createHead.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        createHead.setTextFill(Color.web(UIUtils.BLUE_MID));

        TextField tfName  = UIUtils.styledField("Full Name",  210);
        TextField tfEmail = UIUtils.styledField("Email",      210);
        TextField tfPhone = UIUtils.styledField("e.g. 0300-1234567", 210);
        TextField tfUname = UIUtils.styledField("Username",   210);

        // ── Department dropdown – same categories as citizen complaint form ──
        ComboBox<String> cbDept = new ComboBox<>();
        cbDept.getItems().addAll(
            "Sanitation",
            "Road Damage",
            "Water Supply",
            "Electricity",
            "Street Lighting",
            "Noise Complaint",
            "Other"
        );
        cbDept.setPromptText("Select Department"); cbDept.setPrefWidth(210);
        cbDept.setStyle("-fx-font-size:13;-fx-padding:4;-fx-background-radius:8;" +
                        "-fx-border-color:#dde3ed;-fx-border-radius:8;");

        PasswordField pfPwd = new PasswordField();
        pfPwd.setPromptText("Temporary Password"); pfPwd.setPrefWidth(210);
        pfPwd.setStyle("-fx-font-size:13;-fx-padding:10 12;-fx-background-radius:8;" +
                       "-fx-border-color:#dde3ed;-fx-border-width:1.5;-fx-background-color:white;");

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(8);
        grid.addRow(0, gLbl("Name *"), tfName, gLbl("Email *"), tfEmail);
        grid.addRow(1, gLbl("Phone"),  tfPhone, gLbl("Username *"), tfUname);
        grid.addRow(2, gLbl("Password *"), pfPwd, gLbl("Department *"), cbDept);

        Label lblCreateErr = new Label(""); lblCreateErr.setTextFill(Color.web("#e24b4a"));
        Button btnCreate = UIUtils.primaryButton("Create Officer Account");
        btnCreate.setOnAction(e -> {
            if (cbDept.getValue() == null) {
                lblCreateErr.setText("Please select a department."); return;
            }
            boolean ok = AuthController.getInstance().createOfficerAccount(
                tfName.getText().trim(), tfEmail.getText().trim(), tfPhone.getText().trim(),
                tfUname.getText().trim(), pfPwd.getText(), cbDept.getValue(), manager);
            if (ok) {
                UIUtils.showInfo("Officer Created ✓", "Account created successfully.");
                tfName.clear(); tfEmail.clear(); tfPhone.clear();
                tfUname.clear(); pfPwd.clear(); cbDept.setValue(null);
                lblCreateErr.setText("");
            } else lblCreateErr.setText("Creation failed. Username/email may already exist.");
        });

        VBox createCard = new VBox(10, createHead, grid, lblCreateErr, btnCreate);
        createCard.setPadding(new Insets(18));
        createCard.setStyle("-fx-background-color:white;-fx-background-radius:12;" +
                            "-fx-border-color:#e8eef5;-fx-border-width:1;-fx-border-radius:12;");

        // ── Deactivate card ───────────────────────────────
        Label deactHead = new Label("Deactivate Officer");
        deactHead.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        deactHead.setTextFill(Color.web("#e24b4a"));

        List<Officer> active = AuthController.getInstance().getAllActiveOfficers();
        ComboBox<String> cbDeact = new ComboBox<>();
        active.forEach(o -> cbDeact.getItems().add(o.getOfficerID() + " | " + o.getName() + " (" + o.getDepartment() + ")"));
        cbDeact.setPromptText("Select officer to deactivate"); cbDeact.setPrefWidth(380);
        cbDeact.setStyle("-fx-font-size:13;-fx-padding:4;-fx-background-radius:8;-fx-border-color:#dde3ed;-fx-border-radius:8;");

        Button btnDeact = UIUtils.dangerButton("Deactivate");
        btnDeact.setOnAction(e -> {
            if (cbDeact.getValue() == null) { UIUtils.showError("No Selection", "Select an officer."); return; }
            if (!UIUtils.showConfirm("Confirm", "Deactivate this officer?\nActive complaints will return to the unassigned queue.")) return;
            int oid = Integer.parseInt(cbDeact.getValue().split(" \\| ")[0].trim());
            boolean ok = AuthController.getInstance().deactivateOfficer(oid, manager);
            if (ok) {
                UIUtils.showInfo("Deactivated", "Officer deactivated. Complaints reassigned.");
                cbDeact.getItems().remove(cbDeact.getValue());
            } else UIUtils.showError("Failed", "Deactivation failed.");
        });

        HBox deactRow = new HBox(10, cbDeact, btnDeact);
        deactRow.setAlignment(Pos.CENTER_LEFT);

        VBox deactCard = new VBox(10, deactHead, deactRow);
        deactCard.setPadding(new Insets(18));
        deactCard.setStyle("-fx-background-color:white;-fx-background-radius:12;" +
                           "-fx-border-color:#fbeaf0;-fx-border-width:1;-fx-border-radius:12;");

        pane.getChildren().addAll(head, createCard, deactCard);
        return pane;
    }

    // ── UC8 Assign dialog ─────────────────────────────────
    private void showAssignDialog(Complaint c) {
        Stage sub = new Stage(); sub.setTitle("Assign Complaint"); sub.initOwner(dashStage);
        VBox form = modalShell("👥  Assign: " + c.getTrackingID());

        HBox chip = new HBox(10);
        chip.setPadding(new Insets(10, 14, 10, 14));
        chip.setStyle("-fx-background-color:#f0f4f8;-fx-background-radius:8;");
        chip.getChildren().addAll(
            boldLbl("Location: "), new Label(c.getLocation()),
            boldLbl("  Status: "), UIUtils.statusBadge(c.getStatus()));
        chip.setAlignment(Pos.CENTER_LEFT);

        List<Officer> officers = AuthController.getInstance().getAllActiveOfficers();
        if (officers.isEmpty()) {
            UIUtils.showError("No Officers", "No active officers. Add officers first."); return;
        }
        ComboBox<String> cbOfficer = new ComboBox<>();
        officers.forEach(o -> cbOfficer.getItems().add(
            o.getOfficerID() + " | " + o.getName() + " (" + o.getDepartment() + ")  —  " +
            AuthController.getInstance().getOfficerWorkloadCount(o.getOfficerID()) + " active"));
        cbOfficer.setPromptText("Select an officer"); cbOfficer.setPrefWidth(460);
        cbOfficer.setStyle("-fx-font-size:13;-fx-padding:4;-fx-background-radius:8;-fx-border-color:#dde3ed;-fx-border-radius:8;");

        Label lblErr = new Label(""); lblErr.setTextFill(Color.web("#e24b4a"));
        Button btnAsgn = UIUtils.primaryButton("Confirm Assignment");
        Button btnCan  = UIUtils.secondaryButton("Cancel");
        btnCan.setOnAction(e -> sub.close());

        btnAsgn.setOnAction(e -> {
            if (cbOfficer.getValue() == null) { lblErr.setText("Please select an officer."); return; }
            int oid = Integer.parseInt(cbOfficer.getValue().split(" \\| ")[0].trim());
            int aid = AssignmentController.getInstance()
                .assignComplaint(c.getComplaintID(), oid, manager.getManagerID());
            if (aid != -1) {
                UIUtils.showInfo("Assigned ✓", "Complaint assigned.\nOfficer has been notified.");
                sub.close(); loadAll();
            } else lblErr.setText("Assignment failed. Try again.");
        });

        form.getChildren().addAll(chip,
            UIUtils.fieldLabel("Select Officer (active workload shown) *"),
            cbOfficer, lblErr, new HBox(10, btnAsgn, btnCan));
        sub.setScene(new Scene(form, 520, 310)); sub.show();
    }

    // ── Helpers ───────────────────────────────────────────
    private void activateSide(Button btn, List<Button> all) {
        all.forEach(UIUtils::deactivateSideBtn);
        UIUtils.activateSideBtn(btn);
    }

    private HBox statCard(String label, Label numLbl, String color) {
        numLbl.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        numLbl.setTextFill(Color.web(color));
        Label lbl = new Label(label); lbl.setFont(Font.font("Arial", 12));
        lbl.setTextFill(Color.web("#888888"));
        VBox box = new VBox(4, numLbl, lbl);
        box.setPadding(new Insets(16, 20, 16, 20));
        box.setStyle("-fx-background-color:white;-fx-background-radius:10;" +
                     "-fx-border-color:#e8eef5;-fx-border-width:1;-fx-border-radius:10;" +
                     "-fx-border-left-width:4;-fx-border-left-color:" + color + ";");
        HBox.setHgrow(box, Priority.ALWAYS);
        return new HBox(box) {{ HBox.setHgrow(this, Priority.ALWAYS); }};
    }

    private Label statNum(String v) { return new Label(v); }

    private VBox modalShell(String title) {
        Label lbl = new Label(title); lbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lbl.setTextFill(Color.web(UIUtils.BLUE_DARK));
        Separator sep = new Separator(); sep.setPadding(new Insets(4,0,4,0));
        VBox box = new VBox(10, lbl, sep);
        box.setPadding(new Insets(28)); box.setStyle("-fx-background-color:white;");
        return box;
    }

    private Label boldLbl(String t) {
        Label l = new Label(t); l.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        l.setTextFill(Color.web("#444444")); return l;
    }

    private Label gLbl(String t) {
        Label l = new Label(t); l.setFont(Font.font("Arial", 12));
        l.setTextFill(Color.web("#555555")); return l;
    }

    private <T> TableColumn<T, String> col(String title,
            java.util.function.Function<T, String> fn, double w) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        c.setPrefWidth(w); return c;
    }

    private void logout() { dashStage.close(); primaryStage.show(); }
}