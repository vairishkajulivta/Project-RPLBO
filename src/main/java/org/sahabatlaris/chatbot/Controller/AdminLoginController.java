package org.sahabatlaris.chatbot.Controller;

import org.sahabatlaris.chatbot.ui.AppUI;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller untuk halaman Login Admin.
 * Hanya dipakai oleh AdminApp – tidak ada tombol "Masuk sebagai Pengguna".
 */
public class AdminLoginController {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;
    @FXML private Label         togglePassword;

    private boolean showingPassword = false;

    @FXML
    public void initialize() {
        if (errorLabel != null) errorLabel.setText("");
    }

    /** Tombol "Masuk" – validasi kredensial admin. */
    @FXML
    public void handleLogin() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText();

        if (errorLabel != null) errorLabel.setText("");

        if (user.isEmpty() || pass.isEmpty()) {
            setError("Username dan password tidak boleh kosong.");
            return;
        }

        if (user.equals(ADMIN_USERNAME) && pass.equals(ADMIN_PASSWORD)) {
            try {
                Stage stage = (Stage) usernameField.getScene().getWindow();
                new AppUI().showAdmin(stage);
            } catch (Exception e) {
                e.printStackTrace();
                setError("Gagal membuka panel admin: " + e.getMessage());
            }
        } else {
            setError("Username atau password salah.");
            passwordField.clear();
        }
    }

    /** Toggle visibilitas password (ikon mata). */
    @FXML
    public void togglePasswordVisibility() {
        showingPassword = !showingPassword;
        if (togglePassword != null) {
            togglePassword.setText(showingPassword ? "\uD83D\uDE48" : "\uD83D\uDC41");
        }
    }

    private void setError(String msg) {
        if (errorLabel != null) errorLabel.setText(msg);
    }
}
