DROP TABLE IF EXISTS task_comments;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS assignees;
DROP TABLE IF EXISTS tasks;

-- Добавить колонку assignee_id если она отсутствует
ALTER TABLE tasks ADD COLUMN assignee_id INT NULL AFTER project_id;

-- Добавить внешний ключ
ALTER TABLE tasks
ADD CONSTRAINT fk_tasks_assignee
FOREIGN KEY (assignee_id) REFERENCES assignees(assignee_id)
ON DELETE SET NULL;

-- Добавить индекс
CREATE INDEX idx_tasks_assignee_id ON tasks(assignee_id);

-- Создание таблицы разработчиков
CREATE TABLE assignees (
    assignee_id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    position VARCHAR(50),
    department VARCHAR(50),
    phone VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_full_name (full_name),
    INDEX idx_department (department)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Создание таблицы проектов
CREATE TABLE projects (
    project_id INT PRIMARY KEY AUTO_INCREMENT,
    project_name VARCHAR(100) NOT NULL,
    description TEXT,
    start_date DATE,
    end_date DATE,
    manager VARCHAR(100),
    budget DECIMAL(12, 2),
    status VARCHAR(20) DEFAULT 'PLANNING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_manager (manager)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Создание таблицы задач с правильной структурой
CREATE TABLE tasks (
    task_id INT PRIMARY KEY AUTO_INCREMENT,
    project_id INT NOT NULL,
    assignee_id INT, -- <-- ЭТА КОЛОНКА ДОЛЖНА БЫТЬ
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
    FOREIGN KEY (assignee_id) REFERENCES assignees(assignee_id) ON DELETE SET NULL, -- <-- И ВНЕШНИЙ КЛЮЧ
    INDEX idx_project_id (project_id),
    INDEX idx_priority (priority),
    INDEX idx_status (status),
    INDEX idx_deadline (deadline),
    INDEX idx_assignee_id (assignee_id) -- <-- И ИНДЕКС
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Вставка тестовых данных для исполнителей
INSERT INTO assignees (full_name, email, position, department) VALUES
('Иванов Иван Иванович', 'ivanov@company.com', 'Руководитель проекта', 'IT'),
('Петров Петр Петрович', 'petrov@company.com', 'Разработчик', 'IT'),
('Сидорова Анна Сергеевна', 'sidorova@company.com', 'Дизайнер', 'Дизайн'),
('Козлов Дмитрий Владимирович', 'kozlov@company.com', 'Аналитик', 'Бизнес-анализ'),
('Орлова Елена Михайловна', 'orlova@company.com', 'Тестировщик', 'QA'),
('Смирнов Алексей Николаевич', 'smirnov@company.com', 'Архитектор', 'IT'),
('Федорова Мария Ивановна', 'fedorova@company.com', 'Менеджер проектов', 'Управление'),
('Никитин Андрей Сергеевич', 'nikitin@company.com', 'DevOps инженер', 'IT');

-- Вставка тестовых проектов
INSERT INTO projects (project_name, description, manager, status, budget) VALUES
('Разработка веб-сайта', 'Создание корпоративного сайта компании', 'Иванов Иван Иванович', 'IN_PROGRESS', 500000.00),
('Мобильное приложение', 'Разработка iOS/Android приложения', 'Петров Петр Петрович', 'PLANNING', 750000.00),
('Внедрение CRM системы', 'Внедрение системы управления клиентами', 'Сидорова Анна Сергеевна', 'IN_PROGRESS', 300000.00),
('Обновление ИТ инфраструктуры', 'Модернизация серверного оборудования', 'Козлов Дмитрий Владимирович', 'IN_PROGRESS', 1200000.00),
('Обучение персонала', 'Проведение тренингов по новому ПО', 'Орлова Елена Михайловна', 'COMPLETED', 150000.00);

-- Вставка тестовых задач с привязкой к исполнителям
INSERT INTO tasks (project_id, assignee_id, task_name, description, priority, status, estimated_hours, deadline) VALUES
(1, 3, 'Дизайн интерфейса', 'Создание макетов страниц сайта', 'HIGH', 'IN_PROGRESS', 40, '2024-12-31'),
(1, 2, 'Разработка API', 'Создание REST API для сайта', 'HIGH', 'NEW', 60, '2024-12-25'),
(2, 5, 'Прототипирование', 'Создание прототипа приложения', 'MEDIUM', 'NEW', 30, '2025-01-15'),
(3, 4, 'Анализ требований', 'Сбор и анализ требований заказчика', 'HIGH', 'COMPLETED', 20, '2024-11-30');