package civilink.ui;

import civilink.controller.AuthController;
import civilink.model.*;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * StaffApp – Staff-only login portal (Officers & Managers).
 * No citizen registration or citizen-facing options visible.
 * Launched from SplashScreen → landing → "Staff Portal" tile.
 */
public class StaffApp {

    private Stage stage;

    public void launch() {
        stage = new Stage();
        stage.setTitle("CiviLink – Staff Portal");
        stage.setResizable(false);
        stage.setScene(buildScene());
        stage.centerOnScreen();

        stage.getScene().getRoot().setOpacity(0);
        stage.show();
        FadeTransition ft = new FadeTransition(Duration.millis(350),
            stage.getScene().getRoot());
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private Scene buildScene() {
        // ── Left panel – blue accent for staff ────────────
        VBox left = new VBox(20);
        left.setPrefWidth(240);
        left.setAlignment(Pos.CENTER);
        left.setPadding(new Insets(48, 28, 48, 28));
        left.setStyle("-fx-background-color:#0c3d7a;");

        // Logo
        ImageView logo = new ImageView();
        try {
            logo.setImage(new Image(
                StaffApp.class.getResourceAsStream("civilink_logo.png")));
        } catch (Exception ignored) {}
        logo.setFitWidth(90); logo.setFitHeight(90);
        logo.setPreserveRatio(true); logo.setSmooth(true);

        TextFlow brand = new TextFlow();
        Text tC = new Text("CIVI"); tC.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        tC.setFill(Color.web("#4a90d9"));
        Text tL = new Text("LINK"); tL.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        tL.setFill(Color.WHITE);
        brand.getChildren().addAll(tC, tL);
        brand.setTextAlignment(TextAlignment.CENTER);

        Label portal = new Label("Staff Portal");
        portal.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        portal.setTextFill(Color.web("#4a90d9"));
        portal.setStyle("-fx-background-color:#ffffff20;-fx-background-radius:20;-fx-padding:4 14;");

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:#ffffff20;");

        VBox feats = new VBox(12,
            feat("Manage and assign complaints"),
            feat("Update complaint statuses"),
            feat("Monitor officer workloads"),
            feat("Generate performance reports"),
            feat("Manage officer accounts")
        );

        left.getChildren().addAll(logo, brand, portal, sep, feats);

        // ── Right panel – login form ──────────────────────
        VBox right = new VBox(0);
        right.setAlignment(Pos.CENTER);
        right.setPadding(new Insets(50, 46, 50, 46));
        right.setStyle("-fx-background-color:#f8fafd;");

        Label head = new Label("Staff Login");
        head.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        head.setTextFill(Color.web("#0c3d7a"));

        Label sub = new Label("Authorized personnel only.\nOfficers and Managers sign in here.");
        sub.setFont(Font.font("Arial", 12));
        sub.setTextFill(Color.web("#888888"));
        sub.setPadding(new Insets(4, 0, 24, 0));
        sub.setLineSpacing(3);

        // Role selector (Officer / Manager) — internal only
        ToggleGroup rg = new ToggleGroup();
        ToggleButton tbOfficer  = new ToggleButton("Officer");
        ToggleButton tbManager  = new ToggleButton("Manager");
        tbOfficer.setToggleGroup(rg); tbManager.setToggleGroup(rg);
        tbOfficer.setSelected(true);
        styledTab(tbOfficer, true); styledTab(tbManager, false);
        tbOfficer.setPrefWidth(120); tbManager.setPrefWidth(120);

        HBox tabs = new HBox(0, tbOfficer, tbManager);
        tabs.setStyle("-fx-background-color:#e8eef5;-fx-background-radius:8;-fx-padding:3;");

        rg.selectedToggleProperty().addListener((obs, old, nw) -> {
            if (nw == null) { rg.selectToggle(old); return; }
            styledTab(tbOfficer, nw == tbOfficer);
            styledTab(tbManager, nw == tbManager);
        });

        Label lUser = fldLbl("Staff Username"); lUser.setPadding(new Insets(20, 0, 6, 0));
        TextField tfUser = inputField("Enter your username", "#0c3d7a");
        tfUser.setMaxWidth(290);

        Label lPass = fldLbl("Password"); lPass.setPadding(new Insets(14, 0, 6, 0));
        PasswordField pfPass = passField("#0c3d7a");
        pfPass.setMaxWidth(290);

        Label lblErr = new Label("");
        lblErr.setTextFill(Color.web("#e24b4a"));
        lblErr.setFont(Font.font("Arial", 12));
        lblErr.setPadding(new Insets(6, 0, 0, 0));
        lblErr.setWrapText(true); lblErr.setMaxWidth(290);

        Button btnLogin = new Button("Sign In  →");
        btnLogin.setPrefWidth(290); btnLogin.setPrefHeight(42);
        btnLogin.setStyle("-fx-background-color:#0c3d7a;-fx-text-fill:white;" +
                          "-fx-font-size:14;-fx-font-weight:bold;-fx-background-radius:8;-fx-cursor:hand;");
        btnLogin.setOnMouseEntered(e -> btnLogin.setStyle(
            "-fx-background-color:#2563a8;-fx-text-fill:white;" +
            "-fx-font-size:14;-fx-font-weight:bold;-fx-background-radius:8;-fx-cursor:hand;"));
        btnLogin.setOnMouseExited(e -> btnLogin.setStyle(
            "-fx-background-color:#0c3d7a;-fx-text-fill:white;" +
            "-fx-font-size:14;-fx-font-weight:bold;-fx-background-radius:8;-fx-cursor:hand;"));

        // Security note
        Label secNote = new Label("🔒  This portal is restricted to authorized staff only.");
        secNote.setFont(Font.font("Arial", FontPosture.ITALIC, 11));
        secNote.setTextFill(Color.web("#aaaaaa"));
        secNote.setPadding(new Insets(10, 0, 0, 0));
        secNote.setWrapText(true); secNote.setMaxWidth(290);

        // Back to portal selection
        Button btnBack = new Button("← Back to Portal Selection");
        btnBack.setStyle("-fx-background-color:transparent;-fx-text-fill:#aaaaaa;" +
                         "-fx-font-size:11;-fx-cursor:hand;-fx-border-color:transparent;");
        btnBack.setPadding(new Insets(8, 0, 0, 0));
        btnBack.setOnAction(e -> {
            stage.close();
            new SplashScreen().showLandingPublic();
        });

        // Login action — OFFICER or MANAGER only, CITIZEN rejected
        btnLogin.setOnAction(e -> {
            lblErr.setText("");
            String u = tfUser.getText().trim(); String p = pfPass.getText();
            if (u.isEmpty() || p.isEmpty()) { lblErr.setText("Please enter username and password."); return; }
            User usr = AuthController.getInstance().login(u, p);
            if (usr == null) { lblErr.setText("Invalid credentials or account inactive."); return; }
            if ("CITIZEN".equals(usr.getRole())) {
                lblErr.setText("Citizen accounts cannot access the Staff Portal.\nPlease use the Citizen Portal.");
                return;
            }
            // Route to correct dashboard
            if (usr instanceof Officer) {
                new OfficerDashboard((Officer) usr, stage).show();
            } else if (usr instanceof Manager) {
                new ManagerDashboard((Manager) usr, stage).show();
            }
        });

        right.getChildren().addAll(
            head, sub, tabs,
            lUser, tfUser,
            lPass, pfPass,
            lblErr,
            new Region() {{ setPrefHeight(14); }},
            btnLogin, secNote, btnBack
        );

        HBox root = new HBox(left, right);
        HBox.setHgrow(right, Priority.ALWAYS);
        return new Scene(root, 620, 500);
    }

    // ── Helpers ───────────────────────────────────────────
    private HBox feat(String text) {
        Label ic = new Label("✓");
        ic.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        ic.setTextFill(Color.web("#4a90d9")); ic.setMinWidth(18);
        Label lb = new Label(text); lb.setFont(Font.font("Arial", 12));
        lb.setTextFill(Color.web("#b0ccf0")); lb.setWrapText(true);
        HBox row = new HBox(8, ic, lb); row.setAlignment(Pos.TOP_LEFT); return row;
    }

    private void styledTab(ToggleButton b, boolean sel) {
        b.setStyle(sel
            ? "-fx-background-color:#0c3d7a;-fx-text-fill:white;" +
              "-fx-font-size:12;-fx-font-weight:bold;-fx-background-radius:6;-fx-padding:7 14;"
            : "-fx-background-color:transparent;-fx-text-fill:#888888;" +
              "-fx-font-size:12;-fx-background-radius:6;-fx-padding:7 14;");
    }

    private TextField inputField(String prompt, String focusColor) {
        TextField tf = new TextField(); tf.setPromptText(prompt);
        String base = "-fx-font-size:13;-fx-padding:10 12;-fx-background-radius:8;" +
                      "-fx-border-radius:8;-fx-border-color:#c8d8f0;-fx-border-width:1.5;-fx-background-color:white;";
        String focus = "-fx-font-size:13;-fx-padding:10 12;-fx-background-radius:8;" +
                       "-fx-border-radius:8;-fx-border-color:" + focusColor + ";-fx-border-width:1.5;-fx-background-color:white;";
        tf.setStyle(base);
        tf.focusedProperty().addListener((o, ov, nv) -> tf.setStyle(nv ? focus : base));
        return tf;
    }

    private PasswordField passField(String focusColor) {
        PasswordField pf = new PasswordField(); pf.setPromptText("Enter your password");
        String base = "-fx-font-size:13;-fx-padding:10 12;-fx-background-radius:8;" +
                      "-fx-border-radius:8;-fx-border-color:#c8d8f0;-fx-border-width:1.5;-fx-background-color:white;";
        String focus = "-fx-font-size:13;-fx-padding:10 12;-fx-background-radius:8;" +
                       "-fx-border-radius:8;-fx-border-color:" + focusColor + ";-fx-border-width:1.5;-fx-background-color:white;";
        pf.setStyle(base);
        pf.focusedProperty().addListener((o, ov, nv) -> pf.setStyle(nv ? focus : base));
        return pf;
    }

    private Label fldLbl(String t) {
        Label l = new Label(t); l.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        l.setTextFill(Color.web("#444444")); return l;
    }
}
