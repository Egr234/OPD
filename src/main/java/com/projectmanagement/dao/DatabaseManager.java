package com.projectmanagement.dao;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    private static Connection connection;
    private static final Properties properties = new Properties();

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try (InputStream input = DatabaseManager.class.getClassLoader()
                .getResourceAsStream("database.properties")) {

            if (input == null) {
                System.out.println("⚠️ Файл database.properties не найден, используются значения по умолчанию");
                setDefaultProperties();
            } else {
                properties.load(input);
                System.out.println("✅ Конфигурация БД загружена из database.properties");
            }

            Class.forName(properties.getProperty("db.driver"));
            System.out.println("✅ Драйвер " + properties.getProperty("db.driver") + " зарегистрирован");

        } catch (Exception e) {
            System.err.println("❌ Ошибка загрузки конфигурации: " + e.getMessage());
            e.printStackTrace();
            setDefaultProperties();
        }
    }

    private static void setDefaultProperties() {
        properties.setProperty("db.url", "jdbc:mysql://localhost:3306/");
        properties.setProperty("db.user", "root");
        properties.setProperty("db.password", "rootroot");
        properties.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        properties.setProperty("db.name", "project_management_system");
    }

    public static void initializeDatabase() {
        System.out.println("\n🔄 Инициализация базы данных...");

        String baseUrl = properties.getProperty("db.url");
        String dbName = properties.getProperty("db.name");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");

        Connection serverConn = null;
        Statement stmt = null;

        try {
            System.out.println("📡 Подключение к MySQL серверу...");
            serverConn = DriverManager.getConnection(baseUrl, user, password);
            stmt = serverConn.createStatement();

            System.out.println("🗄️  Создание/проверка базы данных '" + dbName + "'...");
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName +
                    " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            System.out.println("✅ База данных '" + dbName + "' создана/проверена");

            closeResources(null, stmt, serverConn);

            System.out.println("🔗 Подключение к базе данных '" + dbName + "'...");
            String fullUrl = baseUrl + dbName + "?useSSL=false&serverTimezone=UTC";
            serverConn = DriverManager.getConnection(fullUrl, user, password);
            stmt = serverConn.createStatement();

            // УДАЛИТЕ СТАРЫЕ ТАБЛИЦЫ ЕСЛИ ОНИ СУЩЕСТВУЮТ
            // УДАЛИТЕ СТАРЫЕ ТАБЛИЦЫ ЕСЛИ ОНИ СУЩЕСТВУЮТ
            try {
                stmt.executeUpdate("DROP TABLE IF EXISTS task_comments");
                stmt.executeUpdate("DROP TABLE IF EXISTS tasks");
                stmt.executeUpdate("DROP TABLE IF EXISTS projects");
                stmt.executeUpdate("DROP TABLE IF EXISTS assignees");
                System.out.println("🗑️  Старые таблицы удалены");
            } catch (SQLException e) {
                System.out.println("ℹ️  Старых таблиц не существует или их не удалось удалить");
            }

            // СОЗДАЙТЕ ТАБЛИЦЫ С ПРАВИЛЬНОЙ СТРУКТУРОЙ
            createTables(stmt);
            insertTestData(stmt);

            System.out.println("🎉 База данных успешно инициализирована!\n");

        } catch (SQLException e) {
            System.err.println("\n❌ Ошибка при инициализации БД: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, stmt, serverConn);
        }
    }

    private static void createTables(Statement stmt) throws SQLException {
        System.out.println("📊 Создание таблиц...");

        // 1. Таблица исполнителей
        String createAssignees = """
    CREATE TABLE IF NOT EXISTS assignees (
        assignee_id INT PRIMARY KEY AUTO_INCREMENT,
        full_name VARCHAR(100) NOT NULL,
        email VARCHAR(100) UNIQUE,
        position VARCHAR(50),
        department VARCHAR(50),
        phone VARCHAR(20),
        is_active BOOLEAN DEFAULT TRUE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    """;

        // 2. Таблица проектов
        String createProjects = """
    CREATE TABLE IF NOT EXISTS projects (
        project_id INT PRIMARY KEY AUTO_INCREMENT,
        project_name VARCHAR(100) NOT NULL,
        description TEXT,
        start_date DATE,
        end_date DATE,
        manager VARCHAR(100),
        budget DECIMAL(12, 2) DEFAULT 0.00,
        status VARCHAR(20) DEFAULT 'PLANNING',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    """;

        // 3. Таблица задач - ОБРАТИТЕ ВНИМАНИЕ НА assignee_id
        String createTasks = """
    CREATE TABLE IF NOT EXISTS tasks (
        task_id INT PRIMARY KEY AUTO_INCREMENT,
        project_id INT NOT NULL,
        assignee_id INT NULL,
        task_name VARCHAR(150) NOT NULL,
        description TEXT,
        priority VARCHAR(10) CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
        status VARCHAR(20) DEFAULT 'NEW',
        estimated_hours INT DEFAULT 0,
        actual_hours INT DEFAULT 0,
        deadline DATE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        FOREIGN KEY (project_id) REFERENCES projects(project_id) ON DELETE CASCADE,
        FOREIGN KEY (assignee_id) REFERENCES assignees(assignee_id) ON DELETE SET NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
    """;

        stmt.executeUpdate(createAssignees);
        System.out.println("✅ Таблица 'assignees' создана/проверена");

        stmt.executeUpdate(createProjects);
        System.out.println("✅ Таблица 'projects' создана/проверена");

        stmt.executeUpdate(createTasks);
        System.out.println("✅ Таблица 'tasks' создана/проверена (с колонкой assignee_id)");
    }

    private static void insertTestData(Statement stmt) throws SQLException {
        System.out.println("📝 Проверка/добавление тестовых данных...");

        // Проверяем, есть ли уже данные
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM projects");
        rs.next();
        int projectCount = rs.getInt("count");

        if (projectCount == 0) {
            System.out.println("➕ Добавление тестовых данных...");

            // Исполнители
            String insertAssignees = """
            INSERT IGNORE INTO assignees (full_name, email, position, department) VALUES
            ('Иванов Иван Иванович', 'ivanov@company.com', 'Руководитель проекта', 'IT'),
            ('Петров Петр Петрович', 'petrov@company.com', 'Разработчик', 'IT'),
            ('Сидорова Анна Сергеевна', 'sidorova@company.com', 'Дизайнер', 'Дизайн'),
            ('Козлов Дмитрий Владимирович', 'kozlov@company.com', 'Аналитик', 'Бизнес-анализ'),
            ('Орлова Елена Михайловна', 'orlova@company.com', 'Тестировщик', 'QA')
            """;

            // Проекты
            String insertProjects = """
            INSERT IGNORE INTO projects (project_name, description, manager, status) VALUES
            ('Разработка веб-сайта', 'Создание корпоративного сайта', 'Иванов И.И.', 'IN_PROGRESS'),
            ('Мобильное приложение', 'iOS/Android приложение', 'Петров П.П.', 'PLANNING'),
            ('Внедрение CRM системы', 'Внедрение системы управления клиентами', 'Сидорова А.А.', 'IN_PROGRESS')
            """;

            stmt.executeUpdate(insertAssignees);
            stmt.executeUpdate(insertProjects);

            // Получаем ID добавленных проектов
            rs = stmt.executeQuery("SELECT project_id FROM projects ORDER BY project_id");
            int[] projectIds = new int[3];
            int i = 0;
            while (rs.next()) {
                projectIds[i++] = rs.getInt("project_id");
            }

            // Задачи с правильной колонкой assignee_id
            String insertTasks = String.format("""
            INSERT IGNORE INTO tasks (project_id, assignee_id, task_name, description, priority, status, estimated_hours, deadline) VALUES
            (%d, 3, 'Дизайн интерфейса', 'Создание макетов страниц', 'HIGH', 'IN_PROGRESS', 40, '2024-12-31'),
            (%d, 2, 'Разработка API', 'Создание REST API', 'HIGH', 'NEW', 60, '2024-12-25'),
            (%d, 5, 'Прототипирование', 'Создание прототипа приложения', 'MEDIUM', 'NEW', 30, '2025-01-15'),
            (%d, 4, 'Анализ требований', 'Сбор и анализ требований заказчика', 'HIGH', 'COMPLETED', 20, '2024-11-30')
            """,
                    projectIds[0], projectIds[0], projectIds[1], projectIds[2]);

            stmt.executeUpdate(insertTasks);

            System.out.println("✅ Тестовые данные успешно добавлены");
        } else {
            System.out.println("ℹ️  База данных уже содержит данные (" + projectCount + " проектов)");
        }

        rs.close();
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String fullUrl = properties.getProperty("db.url") +
                    properties.getProperty("db.name") +
                    "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            String user = properties.getProperty("db.user");
            String password = properties.getProperty("db.password");

            try {
                connection = DriverManager.getConnection(fullUrl, user, password);
                System.out.println("🔗 Подключение к БД установлено");
            } catch (SQLException e) {
                System.err.println("❌ Не удалось подключиться к БД: " + e.getMessage());
                System.err.println("URL: " + fullUrl);
                throw e;
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("✅ Подключение к БД закрыто");
                connection = null;
            } catch (SQLException e) {
                System.err.println("❌ Ошибка при закрытии подключения: " + e.getMessage());
            }
        }
    }

    public static void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        try {
            if (rs != null && !rs.isClosed()) rs.close();
            if (stmt != null && !stmt.isClosed()) stmt.close();
            if (conn != null && !conn.isClosed() && conn != connection) conn.close();
        } catch (SQLException e) {
            System.err.println("⚠️  Ошибка при закрытии ресурсов: " + e.getMessage());
        }
    }

    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Подключение к БД работает нормально");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Тест подключения не пройден: " + e.getMessage());
        }
        return false;
    }
}