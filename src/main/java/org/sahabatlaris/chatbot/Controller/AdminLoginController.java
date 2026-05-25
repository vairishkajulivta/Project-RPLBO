package org.sahabatlaris.chatbot.Controller;

import org.sahabatlaris.chatbot.ui.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AdminLoginController {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField     plainPasswordField;
    @FXML private Label         errorLabel;
    @FXML private Label         togglePassword;

    private boolean showingPassword = false;

    @FXML
    public void initialize() {
        if (errorLabel != null) errorLabel.setText("");

        // Menghubungkan isi PasswordField dan TextField agar selalu sama
        if (plainPasswordField != null && passwordField != null) {
            plainPasswordField.textProperty().bindBidirectional(passwordField.textProperty());

            // Sembunyikan kotak teks biasa saat pertama kali dibuka
            plainPasswordField.setVisible(false);
            plainPasswordField.setManaged(false);
        }
    }

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
                new Main().showAdmin(stage);
            } catch (Exception e) {
                e.printStackTrace();
                setError("Gagal membuka panel admin: " + e.getMessage());
            }
        } else {
            setError("Username atau password salah.");
            passwordField.clear();
        }
    }

    /** Toggle visibilitas password (ikon mata/monyet). */
    @FXML
    public void togglePasswordVisibility() {
        showingPassword = !showingPassword;

        if (togglePassword != null) {
            // Mengganti ikon
            togglePassword.setText(showingPassword ? "🙈" : "👁");
        }

        // Mengganti kotak yang ditampilkan
        if (plainPasswordField != null && passwordField != null) {
            if (showingPassword) {
                // Tampilkan teks biasa, sembunyikan titik-titik
                plainPasswordField.setVisible(true);
                plainPasswordField.setManaged(true);
                passwordField.setVisible(false);
                passwordField.setManaged(false);
            } else {
                // Tampilkan titik-titik, sembunyikan teks biasa
                plainPasswordField.setVisible(false);
                plainPasswordField.setManaged(false);
                passwordField.setVisible(true);
                passwordField.setManaged(true);
            }
        }
    }

    private void setError(String msg) {
        if (errorLabel != null) errorLabel.setText(msg);
    }

    @FXML
    public void handleKembali() {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            new org.sahabatlaris.chatbot.ui.Main().showPilihMode(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}