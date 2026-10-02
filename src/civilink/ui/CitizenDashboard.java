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
 * CitizenDashboard
 * UC1 – Submit Complaint
 * UC2 – Track Status (via recent complaints table — no separate tab needed)
 * UC3 – View Timeline
 * UC4 – Reopen Complaint
 *
 * NOTE: "Track Complaint" tab removed — the recent complaints table already
 * shows status for all the citizen's complaints, eliminating the redundancy.
 * Citizens can click any row to view full details including status.
 */
public class CitizenDashboard {

    private final Citizen citizen;
    private final Stage   primaryStage;
    private Stage         dashStage;
    private final ObservableList<Complaint> myComplaints = FXCollections.observableArrayList();

    public CitizenDashboard(Citizen citizen, Stage primaryStage) {
        this.citizen = citizen; this.primaryStage = primaryStage;
    }

    public void show() {
        dashStage = new Stage();
        dashStage.setTitle("CiviLink – Citizen Portal");
        dashStage.setScene(buildScene());
        dashStage.setMinWidth(900);
        dashStage.setMinHeight(600);
        dashStage.show();
        primaryStage.hide();
        loadMyComplaints();
    }

    private Scene buildScene() {
        HBox nav = UIUtils.buildNavBarWithBell("Citizen Portal",
            citizen.getUsername(), citizen.getCitizenID(), this::logout);

        // ── Welcome strip ─────────────────────────────────
        HBox welcome = new HBox();
        welcome.setPadding(new Insets(20, 28, 16, 28));
        welcome.setStyle("-fx-background-color:white;-fx-border-color:#e8eef5;-fx-border-width:0 0 1 0;");
        Label greet = new Label("Hello, " + citizen.getName() + "! 👋");
        greet.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        greet.setTextFill(Color.web(UIUtils.BLUE_DARK));
        Label hint = new Label("   Submit complaints and track their progress below.");
        hint.setFont(Font.font("Arial", 13));
        hint.setTextFill(Color.web("#888888"));
        welcome.getChildren().addAll(greet, hint);
        welcome.setAlignment(Pos.CENTER_LEFT);

        // ── 3 action cards (Track removed – table handles it) ──
        HBox cards = new HBox(14,
            actionCard("📋", "Submit Complaint",
                "Report a new public service issue",
                "#2563a8", "#e8f0fb", e -> showSubmitForm()),
            actionCard("📅", "View Full Timeline",
                "See chronological history of any complaint",
                "#3c3489", "#eeedfe", e -> showTimelineForm()),
            actionCard("🔄", "Reopen Complaint",
                "Dispute an incorrectly resolved complaint",
                "#791f1f", "#fcebeb", e -> showReopenForm())
        );
        cards.setPadding(new Insets(20, 28, 0, 28));

        // ── Complaints table ──────────────────────────────
        // This replaces the separate "Track" tab — click any row for full details
        Label tblHead = UIUtils.sectionHeading("My Complaints  (click any row for details)");

        TableView<Complaint> table = buildTable();
        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Complaint sel = table.getSelectionModel().getSelectedItem();
                if (sel != null) showDetails(sel);
            }
        });
        VBox.setVgrow(table, Priority.ALWAYS);

        Button btnRefresh = UIUtils.secondaryButton("⟳  Refresh");
        Button btnDetails = UIUtils.secondaryButton("View Details");
        Button btnTimeline = UIUtils.secondaryButton("View Timeline");

        btnRefresh.setOnAction(e -> loadMyComplaints());
        btnDetails.setOnAction(e -> {
            Complaint sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { UIUtils.showError("No Selection", "Please click a complaint first."); return; }
            showDetails(sel);
        });
        btnTimeline.setOnAction(e -> {
            Complaint sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) showTimelineForComplaint(sel);
            else showTimelineForm();
        });

        HBox tblBar = new HBox(10, tblHead,
            new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }},
            btnTimeline, btnDetails, btnRefresh);
        tblBar.setAlignment(Pos.CENTER_LEFT);

        VBox tblSection = new VBox(10, tblBar, table);
        tblSection.setPadding(new Insets(20, 28, 20, 28));
        tblSection.setStyle("-fx-background-color:white;");
        VBox.setVgrow(tblSection, Priority.ALWAYS);

        VBox body = new VBox(0, welcome, cards,
            new Region() {{ setPrefHeight(16); setStyle("-fx-background-color:" + UIUtils.BG_LIGHT + ";"); }},
            tblSection);
        body.setStyle("-fx-background-color:" + UIUtils.BG_LIGHT + ";");
        VBox.setVgrow(tblSection, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setTop(nav); root.setCenter(body);
        return new Scene(root, 960, 620);
    }

    @SuppressWarnings("unchecked")
    private TableView<Complaint> buildTable() {
        TableColumn<Complaint, String> colID   = col("Tracking ID",  c -> c.getTrackingID(), 150);
        TableColumn<Complaint, String> colLoc  = col("Location",     c -> c.getLocation(),   155);
        TableColumn<Complaint, String> colDate = col("Submitted",
            c -> c.getSubmissionDate().toString().replace("T"," ").substring(0,16), 140);
        TableColumn<Complaint, String> colExp  = col("Expected By",
            c -> c.getExpectedResDate() != null ? c.getExpectedResDate().toString() : "N/A", 110);
        TableColumn<Complaint, String> colStat = col("Status",       c -> c.getStatus(),     120);
        TableColumn<Complaint, String> colOD   = col("Overdue",
            c -> c.isOverdue() ? "⚠ YES" : "—", 80);

        TableView<Complaint> tv = new TableView<>(myComplaints);
        tv.getColumns().addAll(colID, colLoc, colDate, colExp, colStat, colOD);
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tv.setPlaceholder(new Label("No complaints submitted yet. Use 'Submit Complaint' above."));
        tv.setStyle("-fx-background-color:white;");

        // Color-code rows
        tv.setRowFactory(t -> new TableRow<>() {
            @Override protected void updateItem(Complaint c, boolean empty) {
                super.updateItem(c, empty);
                if (c == null || empty) { setStyle(""); return; }
                if (c.isOverdue())                    setStyle("-fx-background-color:#fff5f5;");
                else if ("RESOLVED".equals(c.getStatus())) setStyle("-fx-background-color:#f5fbf0;");
                else if ("REOPENED".equals(c.getStatus())) setStyle("-fx-background-color:#fff0f5;");
                else                                       setStyle("");
            }
        });
        return tv;
    }

    private void loadMyComplaints() {
        myComplaints.clear();
        ComplaintController.getInstance().getAllComplaints().stream()
            .filter(c -> c.getCitizenID() == citizen.getCitizenID())
            .forEach(myComplaints::add);
    }

    // ── Complaint details popup (replaces Track tab) ───────
    private void showDetails(Complaint c) {
        Stage sub = new Stage(); sub.setTitle("Complaint Details"); sub.initOwner(dashStage);
        VBox form = modalShell("🔍  Complaint Details");

        VBox detBox = new VBox(0,
            detRow("Tracking ID",  c.getTrackingID()),
            detRow("Status",       c.getStatus()),
            detRow("Location",     c.getLocation()),
            detRow("Description",  c.getDescription()),
            detRow("Submitted",    c.getSubmissionDate().toString().replace("T"," ").substring(0,16)),
            detRow("Expected By",  c.getExpectedResDate() != null ? c.getExpectedResDate().toString() : "N/A"),
            detRow("Overdue",      c.isOverdue() ? "⚠ YES" : "No")
        );
        detBox.setStyle("-fx-background-color:#f8fbff;-fx-background-radius:10;" +
                        "-fx-border-color:#e8eef5;-fx-border-radius:10;-fx-border-width:1;");

        // Reopen button (only if RESOLVED)
        if ("RESOLVED".equals(c.getStatus())) {
            Button btnReo = UIUtils.dangerButton("Reopen this Complaint");
            btnReo.setOnAction(e -> { sub.close(); showReopenForComplaint(c.getTrackingID()); });
            form.getChildren().addAll(detBox, btnReo, closeBtn(sub));
        } else {
            form.getChildren().addAll(detBox, closeBtn(sub));
        }
        sub.setScene(new Scene(form, 480, 420)); sub.show();
    }

    // ── UC1 Submit ────────────────────────────────────────
    private void showSubmitForm() {
        Stage sub = new Stage(); sub.setTitle("Submit Complaint"); sub.initOwner(dashStage);
        VBox form = modalShell("📋  Submit a New Complaint");

        ComboBox<String> cbCat = new ComboBox<>();
        cbCat.getItems().addAll("1:Sanitation","2:Road Damage","3:Water Supply",
            "4:Electricity","5:Street Lighting","6:Noise Complaint","7:Other");
        cbCat.setPromptText("Select category"); cbCat.setPrefWidth(400);
        cbCat.setStyle("-fx-font-size:13;-fx-padding:4;-fx-background-radius:8;-fx-border-color:#dde3ed;-fx-border-radius:8;");

        TextField tfLoc  = UIUtils.styledField("e.g. F-7 Markaz, Block 3", 400);
        TextArea taDesc  = UIUtils.styledArea("Describe the issue in detail...", 400, 100);
        Label lblErr     = errLbl();

        Button btnSub = UIUtils.primaryButton("Submit Complaint");
        Button btnCan = UIUtils.secondaryButton("Cancel");
        btnCan.setOnAction(e -> sub.close());

        btnSub.setOnAction(e -> {
            lblErr.setText("");
            if (cbCat.getValue() == null || taDesc.getText().isBlank() || tfLoc.getText().isBlank()) {
                lblErr.setText("All fields are required."); return;
            }
            int catID = Integer.parseInt(cbCat.getValue().split(":")[0]);
            String tid = ComplaintController.getInstance().createComplaint(
                citizen.getCitizenID(), catID,
                taDesc.getText().trim(), tfLoc.getText().trim(), getSLA(catID));
            if (tid != null) {
                UIUtils.showInfo("Complaint Submitted ✓",
                    "Your complaint was submitted successfully!\n\nTracking ID: " + tid +
                    "\n\nIt will appear in your complaints table below.");
                sub.close(); loadMyComplaints();
            } else lblErr.setText("Submission failed. Please try again.");
        });

        form.getChildren().addAll(
            UIUtils.fieldLabel("Category *"), cbCat,
            UIUtils.fieldLabel("Location *"), tfLoc,
            UIUtils.fieldLabel("Description *"), taDesc,
            lblErr, new HBox(10, btnSub, btnCan));
        sub.setScene(new Scene(form, 460, 490)); sub.show();
    }

    // ── UC3 Timeline (from action card) ───────────────────
    private void showTimelineForm() {
        Stage sub = new Stage(); sub.setTitle("View Timeline"); sub.initOwner(dashStage);
        VBox form = modalShell("📅  Complaint Full Timeline");

        TextField tfID = UIUtils.styledField("Enter Tracking ID  (e.g. CVL-A1B2C3D4)", 420);
        Label lblErr = errLbl();
        VBox tlBox = new VBox(0);
        tlBox.setVisible(false);

        Button btnLoad  = UIUtils.primaryButton("Load Timeline");
        Button btnClose = UIUtils.secondaryButton("Close");
        btnClose.setOnAction(e -> sub.close());

        btnLoad.setOnAction(e -> {
            lblErr.setText(""); tlBox.getChildren().clear(); tlBox.setVisible(false);
            Complaint c = ComplaintController.getInstance().trackComplaint(tfID.getText().trim());
            if (c == null) { lblErr.setText("Complaint not found."); return; }
            populateTimeline(tlBox, c);
            tlBox.setVisible(true);
        });

        ScrollPane sp = new ScrollPane(tlBox);
        sp.setFitToWidth(true); sp.setPrefHeight(220);
        sp.setStyle("-fx-background-color:transparent;-fx-border-color:transparent;");

        form.getChildren().addAll(UIUtils.fieldLabel("Tracking ID *"), tfID,
            new HBox(10, btnLoad, btnClose), lblErr, sp);
        sub.setScene(new Scene(form, 500, 460)); sub.show();
    }

    // ── UC3 Timeline (from table row button) ──────────────
    private void showTimelineForComplaint(Complaint c) {
        Stage sub = new Stage();
        sub.setTitle("Timeline – " + c.getTrackingID()); sub.initOwner(dashStage);
        VBox form = modalShell("📅  Timeline: " + c.getTrackingID());

        VBox tlBox = new VBox(0);
        populateTimeline(tlBox, c);

        ScrollPane sp = new ScrollPane(tlBox);
        sp.setFitToWidth(true); sp.setPrefHeight(260);
        sp.setStyle("-fx-background-color:transparent;-fx-border-color:transparent;");

        form.getChildren().addAll(sp, closeBtn(sub));
        sub.setScene(new Scene(form, 500, 420)); sub.show();
    }

    private void populateTimeline(VBox tlBox, Complaint c) {
        List<ComplaintTimeline> tl = ComplaintController.getInstance().getTimeline(c.getComplaintID());
        tlBox.setStyle("-fx-background-color:#f8fbff;-fx-background-radius:10;" +
                       "-fx-border-color:#e8eef5;-fx-border-radius:10;-fx-border-width:1;");
        if (tl.isEmpty()) {
            tlBox.getChildren().add(tlItem("PENDING", "Complaint submitted and pending review.", ""));
        } else {
            for (ComplaintTimeline t : tl) {
                tlBox.getChildren().add(tlItem(t.getStatus(),
                    t.getNotes() != null ? t.getNotes() : "",
                    t.getUpdateTime().toString().replace("T"," ").substring(0,16)));
            }
        }
    }

    // ── UC4 Reopen (from action card) ─────────────────────
    private void showReopenForm() {
        showReopenForComplaint(null);
    }

    private void showReopenForComplaint(String prefillID) {
        Stage sub = new Stage(); sub.setTitle("Reopen Complaint"); sub.initOwner(dashStage);
        VBox form = modalShell("🔄  Reopen a Resolved Complaint");

        Label info = new Label("Only complaints with RESOLVED status can be reopened.");
        info.setFont(Font.font("Arial", FontPosture.ITALIC, 12));
        info.setTextFill(Color.web("#888888"));

        TextField tfID = UIUtils.styledField("Enter Tracking ID", 420);
        if (prefillID != null) tfID.setText(prefillID);

        TextArea taReason = UIUtils.styledArea("Reason for reopening (required)...", 420, 90);
        Label lblErr = errLbl();

        Button btnReo = UIUtils.dangerButton("Reopen Complaint");
        Button btnCan = UIUtils.secondaryButton("Cancel");
        btnCan.setOnAction(e -> sub.close());

        btnReo.setOnAction(e -> {
            lblErr.setText("");
            String tid = tfID.getText().trim(); String reason = taReason.getText().trim();
            if (tid.isEmpty() || reason.isEmpty()) {
                lblErr.setText("Tracking ID and reason are both required."); return;
            }
            boolean ok = ComplaintController.getInstance()
                .reopenComplaint(tid, reason, citizen.getCitizenID());
            if (ok) {
                UIUtils.showInfo("Complaint Reopened",
                    "Complaint " + tid + " has been reopened.\nThe assigned officer and manager have been notified.");
                sub.close(); loadMyComplaints();
            } else lblErr.setText("Could not reopen. Complaint may not be in RESOLVED status.");
        });

        form.getChildren().addAll(info,
            UIUtils.fieldLabel("Tracking ID *"), tfID,
            UIUtils.fieldLabel("Reason *"), taReason,
            lblErr, new HBox(10, btnReo, btnCan));
        sub.setScene(new Scene(form, 470, 400)); sub.show();
    }

    // ── Helpers ───────────────────────────────────────────
    private VBox actionCard(String icon, String title, String desc,
                             String dark, String light,
                             javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Label ic = new Label(icon); ic.setFont(Font.font(26));
        Label ttl = new Label(title);
        ttl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        ttl.setTextFill(Color.web(dark));
        Label dsc = new Label(desc);
        dsc.setFont(Font.font("Arial", 11)); dsc.setWrapText(true);
        dsc.setTextFill(Color.web("#777777"));
        Button btn = new Button("Open →");
        btn.setStyle("-fx-background-color:" + dark + ";-fx-text-fill:white;" +
                     "-fx-font-size:11;-fx-padding:5 12;-fx-background-radius:6;-fx-cursor:hand;");
        btn.setOnAction(action);
        VBox card = new VBox(8, ic, ttl, dsc, btn);
        card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color:white;-fx-background-radius:12;" +
                      "-fx-border-color:" + dark + "40;-fx-border-width:1;-fx-border-radius:12;" +
                      "-fx-border-top-width:4;-fx-border-top-color:" + dark + ";");
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color:" + light + ";-fx-background-radius:12;" +
            "-fx-border-color:" + dark + "60;-fx-border-width:1;-fx-border-radius:12;" +
            "-fx-border-top-width:4;-fx-border-top-color:" + dark + ";"));
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color:white;-fx-background-radius:12;" +
            "-fx-border-color:" + dark + "40;-fx-border-width:1;-fx-border-radius:12;" +
            "-fx-border-top-width:4;-fx-border-top-color:" + dark + ";"));
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private VBox tlItem(String status, String note, String time) {
        Label dot = new Label("●");
        dot.setFont(Font.font(9));
        dot.setTextFill(Color.web(
            "RESOLVED".equals(status) ? "#27500a" :
            "PENDING".equals(status)  ? "#633806" :
            "REOPENED".equals(status) ? "#72243e" : UIUtils.BLUE_MID));
        Label st = new Label(status);
        st.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        st.setTextFill(Color.web(UIUtils.BLUE_DARK));
        HBox topRow = new HBox(8, dot, st); topRow.setAlignment(Pos.CENTER_LEFT);
        VBox item = new VBox(3, topRow);
        if (!note.isEmpty()) {
            Label nl = new Label(note); nl.setFont(Font.font("Arial", 12));
            nl.setTextFill(Color.web("#555555")); nl.setPadding(new Insets(0,0,0,22));
            nl.setWrapText(true); item.getChildren().add(nl);
        }
        if (!time.isEmpty()) {
            Label tl = new Label(time); tl.setFont(Font.font("Arial", FontPosture.ITALIC, 11));
            tl.setTextFill(Color.web("#aaaaaa")); tl.setPadding(new Insets(0,0,0,22));
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

    private Button closeBtn(Stage s) {
        Button b = UIUtils.secondaryButton("Close"); b.setOnAction(e -> s.close()); return b;
    }

    private Label errLbl() {
        Label l = new Label(""); l.setTextFill(Color.web("#e24b4a"));
        l.setFont(Font.font("Arial", 12)); l.setWrapText(true); return l;
    }

    private <T> TableColumn<T, String> col(String title,
            java.util.function.Function<T, String> fn, double w) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setCellValueFactory(d -> new SimpleStringProperty(fn.apply(d.getValue())));
        c.setPrefWidth(w); return c;
    }

    private int getSLA(int id) {
        int[] sla = {5,10,7,3,7,2,7};
        return (id >= 1 && id <= 7) ? sla[id-1] : 7;
    }

    private void logout() { dashStage.close(); primaryStage.show(); }
}
