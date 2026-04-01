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