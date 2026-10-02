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
 * CitizenApp – Citizen-only login portal.
 * No staff login option visible here at all.
 * Launched from SplashScreen → landing → "Citizen Portal" tile.
 */
public class CitizenApp {

    private Stage stage;

    public void launch() {
        stage = new Stage();
        stage.setTitle("CiviLink – Citizen Portal");
        stage.setResizable(false);
        stage.setScene(buildScene());
        stage.centerOnScreen();

        // Fade in
        stage.getScene().getRoot().setOpacity(0);
        stage.show();
        FadeTransition ft = new FadeTransition(Duration.millis(350),
            stage.getScene().getRoot());
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private Scene buildScene() {
        // ── Left panel – green accent for citizen ─────────
        VBox left = new VBox(20);
        left.setPrefWidth(240);
        left.setAlignment(Pos.CENTER);
        left.setPadding(new Insets(48, 28, 48, 28));
        left.setStyle("-fx-background-color:#0f6e56;");

        // Logo
        ImageView logo = new ImageView();
        try {
            logo.setImage(new Image(
                CitizenApp.class.getResourceAsStream("civilink_logo.png")));
        } catch (Exception ignored) {}
        logo.setFitWidth(90); logo.setFitHeight(90);
        logo.setPreserveRatio(true); logo.setSmooth(true);

        TextFlow brand = new TextFlow();
        Text tC = new Text("CIVI"); tC.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        tC.setFill(Color.web("#62d9b0"));
        Text tL = new Text("LINK"); tL.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        tL.setFill(Color.WHITE);
        brand.getChildren().addAll(tC, tL);
        brand.setTextAlignment(TextAlignment.CENTER);

        Label portal = new Label("Citizen Portal");
        portal.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        portal.setTextFill(Color.web("#62d9b0"));
        portal.setStyle("-fx-background-color:#ffffff20;-fx-background-radius:20;-fx-padding:4 14;");

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:#ffffff20;");

        VBox feats = new VBox(12,
            feat("Submit public service complaints"),
            feat("Track complaint status anytime"),
            feat("View full complaint timeline"),
            feat("Reopen incorrectly resolved cases")
        );

        left.getChildren().addAll(logo, brand, portal, sep, feats);

        // ── Right panel – login form ──────────────────────
        VBox right = new VBox(0);
        right.setAlignment(Pos.CENTER);
        right.setPadding(new Insets(50, 46, 50, 46));
        right.setStyle("-fx-background-color:#f8fdfb;");

        Label head = new Label("Citizen Login");
        head.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        head.setTextFill(Color.web("#0f6e56"));

        Label sub = new Label("Sign in to report and track public service issues.");
        sub.setFont(Font.font("Arial", 12));
        sub.setTextFill(Color.web("#888888"));
        sub.setPadding(new Insets(4, 0, 24, 0));
        sub.setWrapText(true); sub.setMaxWidth(290);

        Label lUser = fldLbl("Username"); lUser.setPadding(new Insets(0, 0, 6, 0));
        TextField tfUser = inputField("Enter your username", "#0f6e56");
        tfUser.setMaxWidth(290);

        Label lPass = fldLbl("Password"); lPass.setPadding(new Insets(14, 0, 6, 0));
        PasswordField pfPass = passField("#0f6e56");
        pfPass.setMaxWidth(290);

        Label lblErr = new Label("");
        lblErr.setTextFill(Color.web("#e24b4a"));
        lblErr.setFont(Font.font("Arial", 12));
        lblErr.setPadding(new Insets(6, 0, 0, 0));

        Button btnLogin = new Button("Sign In  →");
        btnLogin.setPrefWidth(290); btnLogin.setPrefHeight(42);
        btnLogin.setStyle("-fx-background-color:#0f6e56;-fx-text-fill:white;" +
                          "-fx-font-size:14;-fx-font-weight:bold;-fx-background-radius:8;-fx-cursor:hand;");
        btnLogin.setOnMouseEntered(e -> btnLogin.setStyle(
            "-fx-background-color:#1d9e75;-fx-text-fill:white;" +
            "-fx-font-size:14;-fx-font-weight:bold;-fx-background-radius:8;-fx-cursor:hand;"));
        btnLogin.setOnMouseExited(e -> btnLogin.setStyle(
            "-fx-background-color:#0f6e56;-fx-text-fill:white;" +
            "-fx-font-size:14;-fx-font-weight:bold;-fx-background-radius:8;-fx-cursor:hand;"));

        // Register link
        Hyperlink lnkReg = new Hyperlink("New here? Create a citizen account");
        lnkReg.setFont(Font.font("Arial", 12));
        lnkReg.setTextFill(Color.web("#0f6e56"));
        lnkReg.setBorder(Border.EMPTY);
        lnkReg.setPadding(new Insets(8, 0, 0, 0));

        // Back to portal selection
        Button btnBack = new Button("← Back to Portal Selection");
        btnBack.setStyle("-fx-background-color:transparent;-fx-text-fill:#aaaaaa;" +
                         "-fx-font-size:11;-fx-cursor:hand;-fx-border-color:transparent;");
        btnBack.setPadding(new Insets(12, 0, 0, 0));
        btnBack.setOnAction(e -> {
            stage.close();
            new SplashScreen().showLandingPublic();
        });

        // Login action — only CITIZEN role accepted
        btnLogin.setOnAction(e -> {
            lblErr.setText("");
            String u = tfUser.getText().trim(); String p = pfPass.getText();
            if (u.isEmpty() || p.isEmpty()) { lblErr.setText("Please enter username and password."); return; }
            User usr = AuthController.getInstance().login(u, p);
            if (usr == null) { lblErr.setText("Invalid credentials or account inactive."); return; }
            if (!"CITIZEN".equals(usr.getRole())) {
                lblErr.setText("This portal is for citizens only.\nPlease use the Staff Portal.");
                return;
            }
            new CitizenDashboard((Citizen) usr, stage).show();
        });

        lnkReg.setOnAction(e -> new RegisterScreen(stage).show());

        right.getChildren().addAll(
            head, sub,
            lUser, tfUser,
            lPass, pfPass,
            lblErr,
            new Region() {{ setPrefHeight(14); }},
            btnLogin, lnkReg, btnBack
        );

        HBox root = new HBox(left, right);
        HBox.setHgrow(right, Priority.ALWAYS);
        return new Scene(root, 620, 480);
    }

    // ── Helpers ───────────────────────────────────────────
    private HBox feat(String text) {
        Label ic = new Label("✓");
        ic.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        ic.setTextFill(Color.web("#62d9b0")); ic.setMinWidth(18);
        Label lb = new Label(text); lb.setFont(Font.font("Arial", 12));
        lb.setTextFill(Color.web("#b8f0e0")); lb.setWrapText(true);
        HBox row = new HBox(8, ic, lb); row.setAlignment(Pos.TOP_LEFT); return row;
    }

    private TextField inputField(String prompt, String focusColor) {
        TextField tf = new TextField(); tf.setPromptText(prompt);
        String base = "-fx-font-size:13;-fx-padding:10 12;-fx-background-radius:8;" +
                      "-fx-border-radius:8;-fx-border-color:#c8e6d8;-fx-border-width:1.5;-fx-background-color:white;";
        String focus = "-fx-font-size:13;-fx-padding:10 12;-fx-background-radius:8;" +
                       "-fx-border-radius:8;-fx-border-color:" + focusColor + ";-fx-border-width:1.5;-fx-background-color:white;";
        tf.setStyle(base);
        tf.focusedProperty().addListener((o, ov, nv) -> tf.setStyle(nv ? focus : base));
        return tf;
    }

    private PasswordField passField(String focusColor) {
        PasswordField pf = new PasswordField(); pf.setPromptText("Enter your password");
        String base = "-fx-font-size:13;-fx-padding:10 12;-fx-background-radius:8;" +
                      "-fx-border-radius:8;-fx-border-color:#c8e6d8;-fx-border-width:1.5;-fx-background-color:white;";
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
