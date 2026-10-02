package civilink.ui;

import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.scene.control.ScrollPane;
import java.util.Optional;

public class UIUtils {

    // ── Brand colors – lighter teal-blue instead of navy ──
    public static final String BLUE_DARK   = "#2563a8";   // lighter brand blue
    public static final String BLUE_MID    = "#4a90d9";   // mid accent
    public static final String BLUE_LIGHT  = "#e8f0fb";   // tint for sidebar active
    public static final String NAV_BG      = "#2563a8";   // nav bar background
    public static final String BG_LIGHT    = "#f0f4f9";   // page background
    public static final String WHITE       = "white";
    public static final String BORDER_GRAY = "#dde3ed";

    // ── Alerts ────────────────────────────────────────────
    public static void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    public static void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    public static boolean showConfirm(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        Optional<ButtonType> r = a.showAndWait();
        return r.isPresent() && r.get() == ButtonType.OK;
    }

    // ── Nav bar (no bell) ─────────────────────────────────
    public static HBox buildNavBar(String roleLabel, String username, Runnable onLogout) {
        return buildNavBarWithBell(roleLabel, username, -1, onLogout);
    }

    // ── Nav bar WITH bell ─────────────────────────────────
    public static HBox buildNavBarWithBell(String roleLabel, String username,
                                            int userID, Runnable onLogout) {
        Label brand = new Label("CiviLink");
        brand.setFont(Font.font("Arial", FontWeight.BOLD, 17));
        brand.setTextFill(Color.WHITE);

        Label divider = new Label("|");
        divider.setTextFill(Color.web("#ffffff50"));
        divider.setFont(Font.font("Arial", 15));

        Label role = new Label(roleLabel);
        role.setFont(Font.font("Arial", 13));
        role.setTextFill(Color.web("#cce0ff"));

        HBox leftSide = new HBox(10, brand, divider, role);
        leftSide.setAlignment(Pos.CENTER_LEFT);

        // Avatar chip
        String initials = username.length() >= 2
            ? username.substring(0, 2).toUpperCase() : username.toUpperCase();
        Label avatar = new Label(initials);
        avatar.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        avatar.setTextFill(Color.web(BLUE_DARK));
        avatar.setStyle("-fx-background-color:#62d9b0;-fx-background-radius:20;-fx-padding:4 8;");

        Label uname = new Label(username);
        uname.setFont(Font.font("Arial", 12));
        uname.setTextFill(Color.web("#cce0ff"));

        HBox rightSide = new HBox(12);
        rightSide.setAlignment(Pos.CENTER_RIGHT);

        // Bell (only if userID provided)
        if (userID > 0) {
            Button btnBell = new Button("🔔");
            btnBell.setStyle("-fx-background-color:#ffffff18;-fx-text-fill:white;" +
                             "-fx-font-size:15;-fx-background-radius:8;-fx-padding:5 10;-fx-cursor:hand;");

            Label badge = new Label();
            badge.setStyle("-fx-background-color:#e24b4a;-fx-text-fill:white;" +
                           "-fx-background-radius:10;-fx-font-size:10;-fx-padding:1 5;");
            badge.setVisible(false);

            civilink.service.NotificationService ns =
                civilink.service.NotificationService.getInstance();
            java.util.List<civilink.model.Notification> unread = ns.getUnreadNotifications(userID);
            if (!unread.isEmpty()) {
                badge.setText(String.valueOf(unread.size()));
                badge.setVisible(true);
            }
            StackPane bellPane = new StackPane(btnBell, badge);
            StackPane.setAlignment(badge, Pos.TOP_RIGHT);
            btnBell.setOnAction(e -> showNotifPopup(btnBell, userID, badge));
            rightSide.getChildren().add(bellPane);
        }

        Button btnLogout = new Button("Logout");
        btnLogout.setStyle("-fx-background-color:#ffffff20;-fx-text-fill:white;" +
                           "-fx-font-size:12;-fx-padding:6 16;-fx-background-radius:8;-fx-cursor:hand;");
        btnLogout.setOnMouseEntered(e -> btnLogout.setStyle(
            "-fx-background-color:white;-fx-text-fill:" + BLUE_DARK +
            ";-fx-font-size:12;-fx-padding:6 16;-fx-background-radius:8;-fx-cursor:hand;"));
        btnLogout.setOnMouseExited(e -> btnLogout.setStyle(
            "-fx-background-color:#ffffff20;-fx-text-fill:white;" +
            "-fx-font-size:12;-fx-padding:6 16;-fx-background-radius:8;-fx-cursor:hand;"));
        btnLogout.setOnAction(e -> onLogout.run());

        rightSide.getChildren().addAll(avatar, uname, btnLogout);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox nav = new HBox(16, leftSide, spacer, rightSide);
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.setPadding(new Insets(13, 20, 13, 20));
        nav.setStyle("-fx-background-color:" + NAV_BG + ";");
        return nav;
    }

    // ── Notification popup ────────────────────────────────
    private static void showNotifPopup(Button anchor, int userID, Label badge) {
        civilink.service.NotificationService ns = civilink.service.NotificationService.getInstance();
        java.util.List<civilink.model.Notification> notifs = ns.getUnreadNotifications(userID);

        javafx.stage.Stage popup = new javafx.stage.Stage();
        popup.initStyle(javafx.stage.StageStyle.UNDECORATED);
        popup.setAlwaysOnTop(true);

        HBox hdr = new HBox();
        hdr.setPadding(new Insets(12, 16, 12, 16));
        hdr.setStyle("-fx-background-color:" + NAV_BG + ";-fx-background-radius:10 10 0 0;");
        Label hdrLbl = new Label("🔔  Notifications");
        hdrLbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        hdrLbl.setTextFill(Color.WHITE);
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:white;-fx-font-size:13;-fx-cursor:hand;");
        closeBtn.setOnAction(e -> popup.close());
        Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);
        hdr.getChildren().addAll(hdrLbl, s, closeBtn);

        VBox listBox = new VBox(0);
        if (notifs.isEmpty()) {
            Label empty = new Label("No new notifications");
            empty.setPadding(new Insets(20, 16, 20, 16));
            empty.setTextFill(Color.web("#888888"));
            empty.setFont(Font.font("Arial", 13));
            listBox.getChildren().add(empty);
        } else {
            for (civilink.model.Notification n : notifs) {
                listBox.getChildren().add(notifItem(n, ns, badge, notifs));
                listBox.getChildren().add(new Separator() {{
                    setStyle("-fx-background-color:#f0f0f0;");
                }});
            }
        }

        ScrollPane sp = new ScrollPane(listBox);
        sp.setFitToWidth(true); sp.setMaxHeight(320);
        sp.setStyle("-fx-background-color:white;-fx-border-color:transparent;");

        Button markAll = new Button("Mark all as read");
        markAll.setPrefWidth(320);
        markAll.setStyle("-fx-background-color:#f0f4f8;-fx-text-fill:" + BLUE_DARK +
                         ";-fx-font-size:12;-fx-padding:9;-fx-cursor:hand;-fx-background-radius:0 0 10 10;");
        markAll.setOnAction(e -> {
            for (civilink.model.Notification n : notifs) ns.markRead(n.getNotificationID());
            badge.setVisible(false);
            listBox.getChildren().clear();
            Label empty = new Label("All caught up!");
            empty.setPadding(new Insets(20, 16, 20, 16));
            empty.setTextFill(Color.web("#888888"));
            listBox.getChildren().add(empty);
        });

        VBox content = new VBox(0, hdr, sp, markAll);
        content.setPrefWidth(320);
        content.setStyle("-fx-background-color:white;-fx-border-color:#dde3ed;" +
                         "-fx-border-width:1;-fx-background-radius:10;-fx-border-radius:10;");

        javafx.geometry.Point2D pt = anchor.localToScreen(0, anchor.getHeight());
        popup.setX(pt.getX() - 250);
        popup.setY(pt.getY() + 6);
        popup.setScene(new javafx.scene.Scene(content));
        popup.show();
        popup.focusedProperty().addListener((o, ov, nv) -> { if (!nv) popup.close(); });
    }

    private static VBox notifItem(civilink.model.Notification n,
                                   civilink.service.NotificationService ns,
                                   Label badge,
                                   java.util.List<civilink.model.Notification> all) {
        VBox item = new VBox(4);
        item.setPadding(new Insets(12, 16, 12, 16));
        item.setStyle("-fx-background-color:#f8fbff;-fx-cursor:hand;");

        Label dot = new Label("●");
        dot.setTextFill(Color.web(BLUE_MID));
        dot.setFont(Font.font(9));

        Label msg = new Label(n.getMessage());
        msg.setFont(Font.font("Arial", 12));
        msg.setWrapText(true); msg.setMaxWidth(280);
        msg.setTextFill(Color.web("#333333"));

        HBox row = new HBox(8, dot, msg);
        row.setAlignment(Pos.TOP_LEFT);

        String t = n.getDate().toString().replace("T", " ");
        Label time = new Label(t.substring(0, Math.min(16, t.length())));
        time.setFont(Font.font("Arial", 11));
        time.setTextFill(Color.web("#aaaaaa"));

        item.getChildren().addAll(row, time);
        item.setOnMouseClicked(e -> {
            ns.markRead(n.getNotificationID());
            dot.setVisible(false);
            item.setStyle("-fx-background-color:white;-fx-cursor:hand;");
            long rem = all.stream().filter(x -> x.getNotificationID() != n.getNotificationID()).count();
            if (rem <= 0) badge.setVisible(false);
            else badge.setText(String.valueOf(rem));
        });
        item.setOnMouseEntered(e -> item.setStyle("-fx-background-color:#eaf3fb;-fx-cursor:hand;"));
        item.setOnMouseExited(e -> item.setStyle("-fx-background-color:#f8fbff;-fx-cursor:hand;"));
        return item;
    }

    // ── Status badge ──────────────────────────────────────
    public static Label statusBadge(String status) {
        Label lbl = new Label(status != null ? status.replace("_", " ") : "");
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        lbl.setPadding(new Insets(3, 10, 3, 10));
        String[] bc = badgeColors(status);
        lbl.setStyle("-fx-background-radius:20;-fx-background-color:" + bc[0] +
                     ";-fx-text-fill:" + bc[1] + ";");
        return lbl;
    }

    private static String[] badgeColors(String s) {
        if (s == null) return new String[]{"#eeeeee", "#666666"};
        switch (s) {
            case "PENDING":     return new String[]{"#faeeda", "#633806"};
            case "ASSIGNED":    return new String[]{"#e6f1fb", "#0c447c"};
            case "IN_PROGRESS": return new String[]{"#eeedfe", "#3c3489"};
            case "AWAITING":    return new String[]{"#f1efe8", "#444441"};
            case "RESOLVED":    return new String[]{"#eaf3de", "#27500a"};
            case "REOPENED":    return new String[]{"#fbeaf0", "#72243e"};
            case "CLOSED":      return new String[]{"#444444", "#ffffff"};
            default:            return new String[]{"#eeeeee", "#666666"};
        }
    }

    // ── Section heading ───────────────────────────────────
    public static Label sectionHeading(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        lbl.setTextFill(Color.web(BLUE_DARK));
        return lbl;
    }

    // ── Buttons ───────────────────────────────────────────
    public static Button primaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color:" + BLUE_DARK + ";-fx-text-fill:white;" +
                     "-fx-font-size:13;-fx-padding:8 20;-fx-background-radius:8;-fx-cursor:hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color:" + BLUE_MID + ";-fx-text-fill:white;" +
            "-fx-font-size:13;-fx-padding:8 20;-fx-background-radius:8;-fx-cursor:hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color:" + BLUE_DARK + ";-fx-text-fill:white;" +
            "-fx-font-size:13;-fx-padding:8 20;-fx-background-radius:8;-fx-cursor:hand;"));
        return btn;
    }

    public static Button secondaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color:#e8eef5;-fx-text-fill:" + BLUE_DARK + ";" +
                     "-fx-font-size:13;-fx-padding:8 18;-fx-background-radius:8;-fx-cursor:hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color:#d0dce8;-fx-text-fill:" + BLUE_DARK + ";" +
            "-fx-font-size:13;-fx-padding:8 18;-fx-background-radius:8;-fx-cursor:hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color:#e8eef5;-fx-text-fill:" + BLUE_DARK + ";" +
            "-fx-font-size:13;-fx-padding:8 18;-fx-background-radius:8;-fx-cursor:hand;"));
        return btn;
    }

    public static Button dangerButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color:#e24b4a;-fx-text-fill:white;" +
                     "-fx-font-size:13;-fx-padding:8 18;-fx-background-radius:8;-fx-cursor:hand;");
        return btn;
    }

    // ── Inputs ────────────────────────────────────────────
    public static TextField styledField(String prompt, double width) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(width);
        applyFieldStyle(tf);
        return tf;
    }

    public static TextArea styledArea(String prompt, double width, double height) {
        TextArea ta = new TextArea();
        ta.setPromptText(prompt); ta.setPrefSize(width, height); ta.setWrapText(true);
        ta.setStyle("-fx-font-size:13;-fx-padding:10;-fx-background-radius:8;" +
                    "-fx-border-color:" + BORDER_GRAY + ";-fx-border-radius:8;");
        return ta;
    }

    public static void applyFieldStyle(TextField tf) {
        String base = "-fx-font-size:13;-fx-padding:10 12;-fx-background-radius:8;" +
                      "-fx-border-radius:8;-fx-border-color:" + BORDER_GRAY + ";" +
                      "-fx-border-width:1.5;-fx-background-color:white;";
        String focus = "-fx-font-size:13;-fx-padding:10 12;-fx-background-radius:8;" +
                       "-fx-border-radius:8;-fx-border-color:" + BLUE_DARK + ";" +
                       "-fx-border-width:1.5;-fx-background-color:white;";
        tf.setStyle(base);
        tf.focusedProperty().addListener((o, ov, nv) -> tf.setStyle(nv ? focus : base));
    }

    // ── Card ──────────────────────────────────────────────
    public static VBox card(Node... children) {
        VBox box = new VBox(12, children);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color:white;-fx-background-radius:12;" +
                     "-fx-border-color:#e8eef5;-fx-border-width:1;-fx-border-radius:12;");
        return box;
    }

    // ── Sidebar button ────────────────────────────────────
    public static Button sidebarButton(String icon, String text) {
        Button btn = new Button(icon + "  " + text);
        btn.setPrefWidth(190);
        btn.setAlignment(Pos.CENTER_LEFT);
        String idle = "-fx-background-color:transparent;-fx-text-fill:#555555;" +
                      "-fx-font-size:13;-fx-padding:10 16;-fx-background-radius:8;-fx-cursor:hand;";
        btn.setStyle(idle);
        btn.setOnMouseEntered(e -> {
            if (!btn.getStyleClass().contains("sb-active"))
                btn.setStyle("-fx-background-color:" + BLUE_LIGHT + ";-fx-text-fill:" + BLUE_DARK + ";" +
                             "-fx-font-size:13;-fx-padding:10 16;-fx-background-radius:8;-fx-cursor:hand;");
        });
        btn.setOnMouseExited(e -> {
            if (!btn.getStyleClass().contains("sb-active")) btn.setStyle(idle);
        });
        return btn;
    }

    public static void activateSideBtn(Button btn) {
        btn.getStyleClass().add("sb-active");
        btn.setStyle("-fx-background-color:" + BLUE_LIGHT + ";-fx-text-fill:" + BLUE_DARK + ";" +
                     "-fx-font-size:13;-fx-font-weight:bold;-fx-padding:10 16 10 13;" +
                     "-fx-background-radius:8;-fx-cursor:hand;" +
                     "-fx-border-color:" + BLUE_DARK + ";-fx-border-width:0 0 0 3;-fx-border-radius:0;");
    }

    public static void deactivateSideBtn(Button btn) {
        btn.getStyleClass().remove("sb-active");
        btn.setStyle("-fx-background-color:transparent;-fx-text-fill:#555555;" +
                     "-fx-font-size:13;-fx-padding:10 16;-fx-background-radius:8;-fx-cursor:hand;");
    }

    // ── Field label ───────────────────────────────────────
    public static Label fieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lbl.setTextFill(Color.web("#444444"));
        VBox.setMargin(lbl, new Insets(10, 0, 4, 0));
        return lbl;
    }
}
