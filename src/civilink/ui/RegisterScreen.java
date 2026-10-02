package civilink.ui;

import civilink.controller.AuthController;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

public class RegisterScreen {

    private final Stage owner;
    public RegisterScreen(Stage owner) { this.owner = owner; }

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("CiviLink – Create Account");
        stage.initOwner(owner);

        Label title = new Label("Create Citizen Account");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.web(UIUtils.BLUE_DARK));

        Label sub = new Label("Join CiviLink to submit and track public service complaints.");
        sub.setFont(Font.font("Arial", 12)); sub.setTextFill(Color.web("#888888"));

        TextField tfCNIC  = iField("e.g. 42101-1234567-8"); tfCNIC.setPrefWidth(380);
        TextField tfEmail = iField("e.g. name@gmail.com");
        TextField tfPhone = iField("e.g. 0300-1234567  (optional)");
        TextField tfUname = iField("e.g. name_k  (no spaces)");

        PasswordField pfPass = new PasswordField();
        pfPass.setPromptText("Minimum 6 characters"); pfPass.setPrefWidth(380); stylePass(pfPass);

        PasswordField pfConf = new PasswordField();
        pfConf.setPromptText("Re-enter your password"); pfConf.setPrefWidth(380); stylePass(pfConf);

        Label phoneHint = hint("Format: 03XX-XXXXXXX (optional)");
        Label passHint  = hint("");
        Label confHint  = hint("");

        tfPhone.textProperty().addListener((o, ov, nv) -> {
            if (nv.isEmpty()) { phoneHint.setTextFill(Color.web("#aaaaaa")); phoneHint.setText("Format: 03XX-XXXXXXX (optional)"); return; }
            if (!nv.matches("[0-9\\-]*")) { tfPhone.setText(ov); return; }
            if (nv.matches("03[0-9]{2}-[0-9]{7}")) { phoneHint.setTextFill(Color.web("#27500a")); phoneHint.setText("✓ Valid phone number"); }
            else { phoneHint.setTextFill(Color.web("#633806")); phoneHint.setText("Format: 03XX-XXXXXXX"); }
        });

        pfPass.textProperty().addListener((o, ov, nv) -> {
            if (nv.isEmpty()) { passHint.setText(""); return; }
            if (nv.length() < 6) { passHint.setTextFill(Color.web("#e24b4a")); passHint.setText("Too short – min 6 characters"); }
            else if (nv.length() < 10) { passHint.setTextFill(Color.web("#633806")); passHint.setText("Moderate strength"); }
            else { passHint.setTextFill(Color.web("#27500a")); passHint.setText("✓ Strong password"); }
        });

        pfConf.textProperty().addListener((o, ov, nv) -> {
            if (nv.isEmpty()) { confHint.setText(""); return; }
            if (nv.equals(pfPass.getText())) { confHint.setTextFill(Color.web("#27500a")); confHint.setText("✓ Passwords match"); }
            else { confHint.setTextFill(Color.web("#e24b4a")); confHint.setText("Passwords do not match"); }
        });

        Label lblErr = new Label("");
        lblErr.setTextFill(Color.web("#e24b4a")); lblErr.setWrapText(true);
        lblErr.setMaxWidth(380); lblErr.setFont(Font.font("Arial", 12));

        Button btnCreate = new Button("Create Account");
        btnCreate.setPrefWidth(380); btnCreate.setPrefHeight(42);
        btnCreate.setStyle("-fx-background-color:" + UIUtils.BLUE_DARK + ";-fx-text-fill:white;" +
                           "-fx-font-size:13;-fx-font-weight:bold;-fx-background-radius:8;-fx-cursor:hand;");
        btnCreate.setOnMouseEntered(e -> btnCreate.setStyle(
            "-fx-background-color:" + UIUtils.BLUE_MID + ";-fx-text-fill:white;" +
            "-fx-font-size:13;-fx-font-weight:bold;-fx-background-radius:8;-fx-cursor:hand;"));
        btnCreate.setOnMouseExited(e -> btnCreate.setStyle(
            "-fx-background-color:" + UIUtils.BLUE_DARK + ";-fx-text-fill:white;" +
            "-fx-font-size:13;-fx-font-weight:bold;-fx-background-radius:8;-fx-cursor:hand;"));

        Button btnBack = new Button("Back to Login");
        btnBack.setPrefWidth(380);
        btnBack.setStyle("-fx-background-color:#e8eef5;-fx-text-fill:" + UIUtils.BLUE_DARK + ";" +
                         "-fx-font-size:13;-fx-padding:8;-fx-background-radius:8;-fx-cursor:hand;");
        btnBack.setOnAction(e -> stage.close());

        btnCreate.setOnAction(e -> {
            lblErr.setText("");
            String cnic = tfCNIC.getText().trim(), email = tfEmail.getText().trim();
            String phone = tfPhone.getText().trim(), uname = tfUname.getText().trim();
            String pass = pfPass.getText(), conf = pfConf.getText();
            if (cnic.isEmpty() || email.isEmpty() || uname.isEmpty() || pass.isEmpty()) {
                lblErr.setText("CNIC, email, username and password are required."); return; }
            if (!email.contains("@") || !email.contains(".")) {
                lblErr.setText("Please enter a valid email address."); return; }
            if (uname.contains(" ")) { lblErr.setText("Username cannot contain spaces."); return; }
            if (pass.length() < 6) { lblErr.setText("Password must be at least 6 characters."); return; }
            if (!pass.equals(conf)) { lblErr.setText("Passwords do not match."); return; }
            if (!phone.isEmpty() && !phone.matches("03[0-9]{2}-[0-9]{7}")) {
                lblErr.setText("Phone format must be: 03XX-XXXXXXX"); return; }
            boolean ok = AuthController.getInstance().registerCitizen(cnic, email, phone, uname, pass);
            if (ok) {
                UIUtils.showInfo("Account Created ✓",
                    "Welcome to CiviLink!\n\nYou can now log in with:\nUsername: " + uname);
                stage.close();
            } else lblErr.setText("Registration failed. Username or email may already exist.");
        });

        Label req = new Label("* Required fields");
        req.setFont(Font.font("Arial", FontPosture.ITALIC, 11)); req.setTextFill(Color.web("#aaaaaa"));

        VBox form = new VBox(5,
            title, sub, new Separator(),
            fLbl("CNIC *"),    tfCNIC,
            fLbl("Email Address *"), tfEmail,
            fLbl("Phone Number"),   tfPhone, phoneHint,
            fLbl("Username *"),     tfUname,
            fLbl("Password *"),     pfPass, passHint,
            fLbl("Confirm Password *"), pfConf, confHint,
            req, lblErr, new Region() {{ setPrefHeight(4); }},
            btnCreate, btnBack
        );
        form.setPadding(new Insets(30));
        form.setAlignment(Pos.CENTER_LEFT);
        form.setMaxWidth(420);
        form.setStyle("-fx-background-color:white;-fx-background-radius:12;");

        VBox root = new VBox(form); root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color:" + UIUtils.BG_LIGHT + ";");

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:" + UIUtils.BG_LIGHT + ";-fx-border-color:transparent;");

        stage.setScene(new Scene(scroll, 480, 620)); stage.show();
    }

    private TextField iField(String p) {
        TextField tf = new TextField(); tf.setPromptText(p); tf.setPrefWidth(380);
        String b = "-fx-font-size:13;-fx-padding:10 12;-fx-background-radius:8;" +
                   "-fx-border-radius:8;-fx-border-color:#dde3ed;-fx-border-width:1.5;-fx-background-color:white;";
        String f = b.replace("#dde3ed", UIUtils.BLUE_DARK);
        tf.setStyle(b); tf.focusedProperty().addListener((o,ov,nv) -> tf.setStyle(nv?f:b));
        return tf;
    }
    private void stylePass(PasswordField pf) {
        pf.setStyle("-fx-font-size:13;-fx-padding:10 12;-fx-background-radius:8;" +
                    "-fx-border-radius:8;-fx-border-color:#dde3ed;-fx-border-width:1.5;-fx-background-color:white;");
    }
    private Label hint(String t) {
        Label l = new Label(t); l.setFont(Font.font("Arial", FontPosture.ITALIC, 11));
        l.setTextFill(Color.web("#aaaaaa")); return l;
    }
    private Label fLbl(String t) {
        Label l = new Label(t); l.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        l.setTextFill(Color.web("#444444")); VBox.setMargin(l, new Insets(8,0,3,0)); return l;
    }
}
