package civilink.ui;

import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.Duration;

/**
 * SplashScreen – entry point for the unified launcher.
 *
 * Flow:
 *   1. 2-second animated splash (logo + fade-in text)
 *   2. Landing screen with two tiles:
 *      "Citizen Portal"  → CitizenLoginScreen
 *      "Staff Portal"    → StaffLoginScreen
 */
public class SplashScreen extends Application {

    @Override
    public void start(Stage stage) {
        showSplash(stage);
    }

    // ─────────────────────────────────────────────────────
    //  SPLASH  (2 seconds)
    // ─────────────────────────────────────────────────────

    private void showSplash(Stage stage) {
        stage.initStyle(StageStyle.UNDECORATED);

        // Background – dark with subtle radial glow
        Pane bg = new Pane();
        bg.setPrefSize(600, 380);
        bg.setStyle("-fx-background-color:#0d1f35;");

        // Decorative circles
        Circle cOuter = new Circle(300, 190, 260);
        cOuter.setFill(Color.TRANSPARENT);
        cOuter.setStroke(Color.web("#2563a820"));
        cOuter.setStrokeWidth(1);

        Circle cMid = new Circle(300, 190, 180);
        cMid.setFill(Color.TRANSPARENT);
        cMid.setStroke(Color.web("#2563a815"));
        cMid.setStrokeWidth(1);

        bg.getChildren().addAll(cOuter, cMid);

        // Logo image
        ImageView logo = new ImageView();
        try {
            Image img = new Image(
                SplashScreen.class.getResourceAsStream("civilink_logo.png"));
            logo.setImage(img);
        } catch (Exception e) {
            System.err.println("[SplashScreen] Logo image not found: " + e.getMessage());
        }
        logo.setFitWidth(130);
        logo.setFitHeight(130);
        logo.setPreserveRatio(true);
        logo.setSmooth(true);

        // App name – two-tone like logo
        TextFlow nameFlow = new TextFlow();
        nameFlow.setTextAlignment(TextAlignment.CENTER);
        Text tCivi = new Text("CIVI");
        tCivi.setFont(Font.font("Arial", FontWeight.BOLD, 38));
        tCivi.setFill(Color.web("#4fc3a1"));   // green from logo
        Text tLink = new Text("LINK");
        tLink.setFont(Font.font("Arial", FontWeight.BOLD, 38));
        tLink.setFill(Color.web("#4a90d9"));   // blue from logo
        nameFlow.getChildren().addAll(tCivi, tLink);

        Label desc1 = new Label("Public Service Delivery Management Information System");
        desc1.setFont(Font.font("Arial", 13));
        desc1.setTextFill(Color.web("#a0bcd8"));
        desc1.setTextAlignment(TextAlignment.CENTER);
        desc1.setAlignment(Pos.CENTER);

        Label desc2 = new Label("Linking Public and Government");
        desc2.setFont(Font.font("Arial", FontPosture.ITALIC, 12));
        desc2.setTextFill(Color.web("#62d9b0"));
        desc2.setTextAlignment(TextAlignment.CENTER);
        desc2.setAlignment(Pos.CENTER);

        // Loading bar
        Rectangle barBg = new Rectangle(300, 4);
        barBg.setArcWidth(4); barBg.setArcHeight(4);
        barBg.setFill(Color.web("#ffffff15"));

        Rectangle barFill = new Rectangle(0, 4);
        barFill.setArcWidth(4); barFill.setArcHeight(4);
        barFill.setFill(Color.web("#4fc3a1"));

        StackPane barStack = new StackPane(barBg, barFill);
        barStack.setAlignment(Pos.CENTER_LEFT);
        barStack.setMaxWidth(300);

        Label loadingLbl = new Label("Initializing CiviLink...");
        loadingLbl.setFont(Font.font("Arial", 11));
        loadingLbl.setTextFill(Color.web("#4a6a8a"));

        // Version tag
        Label version = new Label("v1.0  |  FAST NUCES Islamabad");
        version.setFont(Font.font("Arial", 10));
        version.setTextFill(Color.web("#2a4a6a"));

        VBox content = new VBox(12,
            logo, nameFlow, desc1, desc2,
            new Region() {{ setPrefHeight(10); }},
            barStack, loadingLbl,
            new Region() {{ setPrefHeight(6); }},
            version
        );
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(40));

        StackPane root = new StackPane(bg, content);

        // ── Animations ──────────────────────────────────
        // Fade in whole splash
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), root);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);

        // Loading bar fill animation
        Timeline barAnim = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(barFill.widthProperty(), 0)),
            new KeyFrame(Duration.millis(1800),
                new KeyValue(barFill.widthProperty(), 300,
                    Interpolator.EASE_BOTH))
        );

        // Logo subtle scale pulse
        ScaleTransition pulse = new ScaleTransition(Duration.millis(800), logo);
        pulse.setFromX(0.85); pulse.setFromY(0.85);
        pulse.setToX(1.0); pulse.setToY(1.0);
        pulse.setInterpolator(Interpolator.EASE_OUT);

        // Text fade in (delayed)
        FadeTransition textFade = new FadeTransition(Duration.millis(600), content);
        textFade.setFromValue(0); textFade.setToValue(1);
        textFade.setDelay(Duration.millis(300));

        // Fade out then switch to landing
        FadeTransition fadeOut = new FadeTransition(Duration.millis(350), root);
        fadeOut.setFromValue(1); fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.millis(1900));
        fadeOut.setOnFinished(e -> {
            stage.close();
            showLanding();
        });

        Scene scene = new Scene(root, 600, 380);
        scene.setFill(Color.web("#0d1f35"));
        stage.setScene(scene);

        // Center on screen
        stage.centerOnScreen();
        stage.show();

        fadeIn.play();
        pulse.play();
        textFade.play();
        barAnim.play();
        fadeOut.play();
    }

    // ─────────────────────────────────────────────────────
    //  LANDING  (two portal tiles)
    // ─────────────────────────────────────────────────────

    private void showLanding() {
        Stage landing = new Stage();
        landing.setTitle("CiviLink – Select Portal");
        landing.setResizable(false);

        // ── Header ───────────────────────────────────────
        // Logo small
        ImageView logoSmall = new ImageView();
        try {
            logoSmall.setImage(new Image(
                SplashScreen.class.getResourceAsStream("civilink_logo.png")));
        } catch (Exception ignored) {}
        logoSmall.setFitWidth(48); logoSmall.setFitHeight(48);
        logoSmall.setPreserveRatio(true);

        TextFlow brand = new TextFlow();
        Text tC = new Text("CIVI");
        tC.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        tC.setFill(Color.web("#4fc3a1"));
        Text tL = new Text("LINK");
        tL.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        tL.setFill(Color.web("#2563a8"));
        brand.getChildren().addAll(tC, tL);

        Label tagline = new Label("Select your portal to continue");
        tagline.setFont(Font.font("Arial", 12));
        tagline.setTextFill(Color.web("#888888"));

        VBox brandText = new VBox(2, brand, tagline);
        brandText.setAlignment(Pos.CENTER_LEFT);

        HBox header = new HBox(14, logoSmall, brandText);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(24, 32, 20, 32));
        header.setStyle("-fx-background-color:white;-fx-border-color:#e8eef5;-fx-border-width:0 0 1 0;");

        // ── Portal tiles ──────────────────────────────────
        VBox citizenTile = portalTile(
            "👥",
            "Citizen Portal",
            "Submit complaints, track\nstatus, view timelines,\nand reopen resolved issues.",
            "#4fc3a1",   // green accent (matches logo)
            "#f0fbf7",
            "#0f6e56",
            () -> {
                landing.close();
                new CitizenApp().launch();
            }
        );

        VBox staffTile = portalTile(
            "🏛",
            "Staff Portal",
            "Officer & Manager login.\nAssign complaints, update\nstatus, generate reports.",
            "#2563a8",   // blue accent (matches logo)
            "#eef4fb",
            "#0c3d7a",
            () -> {
                landing.close();
                new StaffApp().launch();
            }
        );

        HBox tiles = new HBox(20, citizenTile, staffTile);
        tiles.setPadding(new Insets(28, 32, 28, 32));
        tiles.setAlignment(Pos.CENTER);

        // ── Footer ────────────────────────────────────────
        Label footer = new Label(
            "Public Service Delivery Management Information System  |  FAST NUCES Islamabad");
        footer.setFont(Font.font("Arial", 11));
        footer.setTextFill(Color.web("#aaaaaa"));
        HBox footerBox = new HBox(footer);
        footerBox.setAlignment(Pos.CENTER);
        footerBox.setPadding(new Insets(12, 0, 16, 0));
        footerBox.setStyle("-fx-background-color:white;-fx-border-color:#e8eef5;-fx-border-width:1 0 0 0;");

        VBox root = new VBox(0, header, tiles, footerBox);
        root.setStyle("-fx-background-color:#f4f8fb;");

        // Fade in
        root.setOpacity(0);
        Scene scene = new Scene(root, 640, 420);
        landing.setScene(scene);
        landing.centerOnScreen();
        landing.show();

        FadeTransition ft = new FadeTransition(Duration.millis(400), root);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    // ── Portal tile builder ───────────────────────────────
    private VBox portalTile(String icon, String title, String desc,
                             String accent, String bgColor, String darkAccent,
                             Runnable onLaunch) {
        Label icLbl = new Label(icon);
        icLbl.setFont(Font.font(44));

        Label ttl = new Label(title);
        ttl.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        ttl.setTextFill(Color.web(darkAccent));

        Label dsc = new Label(desc);
        dsc.setFont(Font.font("Arial", 12));
        dsc.setTextFill(Color.web("#555555"));
        dsc.setLineSpacing(3);
        dsc.setWrapText(true);
        dsc.setAlignment(Pos.CENTER);
        dsc.setTextAlignment(TextAlignment.CENTER);

        Button btn = new Button("Enter Portal  →");
        btn.setStyle(
            "-fx-background-color:" + accent + ";-fx-text-fill:white;" +
            "-fx-font-size:13;-fx-font-weight:bold;-fx-padding:10 24;" +
            "-fx-background-radius:8;-fx-cursor:hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color:" + darkAccent + ";-fx-text-fill:white;" +
            "-fx-font-size:13;-fx-font-weight:bold;-fx-padding:10 24;" +
            "-fx-background-radius:8;-fx-cursor:hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color:" + accent + ";-fx-text-fill:white;" +
            "-fx-font-size:13;-fx-font-weight:bold;-fx-padding:10 24;" +
            "-fx-background-radius:8;-fx-cursor:hand;"));
        btn.setOnAction(e -> onLaunch.run());

        // Accent top bar
        Rectangle topBar = new Rectangle(260, 4);
        topBar.setArcWidth(4); topBar.setArcHeight(4);
        topBar.setFill(Color.web(accent));

        VBox tile = new VBox(14, icLbl, ttl, dsc, btn);
        tile.setAlignment(Pos.CENTER);
        tile.setPrefWidth(260);
        tile.setPadding(new Insets(28, 24, 28, 24));
        tile.setStyle(
            "-fx-background-color:" + bgColor + ";" +
            "-fx-background-radius:14;" +
            "-fx-border-color:" + accent + "50;" +
            "-fx-border-width:1;" +
            "-fx-border-radius:14;"
        );

        // Hover animation
        tile.setOnMouseEntered(e -> tile.setStyle(
            "-fx-background-color:white;" +
            "-fx-background-radius:14;" +
            "-fx-border-color:" + accent + ";" +
            "-fx-border-width:2;" +
            "-fx-border-radius:14;" +
            "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.10),12,0,0,4);"));
        tile.setOnMouseExited(e -> tile.setStyle(
            "-fx-background-color:" + bgColor + ";" +
            "-fx-background-radius:14;" +
            "-fx-border-color:" + accent + "50;" +
            "-fx-border-width:1;" +
            "-fx-border-radius:14;"));

        // Wrap with top accent bar
        VBox wrapper = new VBox(0, topBar, tile);
        wrapper.setStyle("-fx-background-radius:14;");
        return tile;
    }

    /**
     * Called by CitizenApp/StaffApp "Back" buttons to return to the landing screen.
     * Public so sibling classes can call it.
     */
    public void showLandingPublic() {
        showLanding();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
