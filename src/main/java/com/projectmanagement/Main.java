package com.projectmanagement;

import com.projectmanagement.dao.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("🚀 Запуск системы управления проектами");

            // 1. Инициализация базы данных
            System.out.println("🔄 Инициализация базы данных...");
            DatabaseManager.initializeDatabase();

            // 2. Проверка подключения
            boolean connected = DatabaseManager.testConnection();
            if (!connected) {
                showError("Ошибка БД", "Не удалось подключиться к базе данных");
                return;
            }

            // 3. Загрузка интерфейса
            System.out.println("📱 Загрузка интерфейса...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/projectmanagement/view/main-view.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1200, 700);

            // 4. Загрузка стилей
            try {
                String css = getClass().getResource("/css/style.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception e) {
                System.out.println("⚠️ CSS файл не найден, используются стили по умолчанию");
            }

            // 5. Настройка окна
            primaryStage.setTitle("Система управления проектами");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(600);
            primaryStage.show();

            System.out.println("✅ Приложение успешно запущено\n");

        } catch (Exception e) {
            System.err.println("❌ Критическая ошибка при запуске: " + e.getMessage());
            e.printStackTrace();
            showError("Ошибка запуска", e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        System.out.println("👋 Завершение работы приложения");
        DatabaseManager.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}