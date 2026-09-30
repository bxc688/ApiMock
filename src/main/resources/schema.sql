-- ApiMock 数据库初始化脚本

-- 项目表
CREATE TABLE IF NOT EXISTS project (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    name            TEXT    NOT NULL,
    description     TEXT,
    created_at      TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
    updated_at      TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);

CREATE INDEX IF NOT EXISTS idx_project_name ON project(name);

-- 插入默认项目
INSERT OR IGNORE INTO project (id, name, description) VALUES (1, '默认项目', '系统默认项目');

-- Mock 接口定义表
CREATE TABLE IF NOT EXISTS mock_api (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id      INTEGER NOT NULL DEFAULT 1,
    name            TEXT    NOT NULL,
    method          TEXT    NOT NULL,
    path            TEXT    NOT NULL,
    enabled         INTEGER NOT NULL DEFAULT 1,
    response_status INTEGER NOT NULL DEFAULT 200,
    response_headers TEXT,
    response_body   TEXT,
    response_type   TEXT    NOT NULL DEFAULT 'static',  -- 'static' | 'dynamic' | 'file'
    response_file_path TEXT,  -- 文件流模式：文件路径（UUID 重命名后）
    response_file_name TEXT,  -- 文件流模式：原始文件名
    response_content_type TEXT,  -- 文件流模式：Content-Type
    dynamic_rules   TEXT,  -- 动态响应规则（JSON 数组）
    delay_ms        INTEGER NOT NULL DEFAULT 0,
    error_type      TEXT,
    description     TEXT,
    created_at      TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
    updated_at      TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
    UNIQUE(project_id, method, path),
    FOREIGN KEY (project_id) REFERENCES project(id)
);

CREATE INDEX IF NOT EXISTS idx_mock_api_project ON mock_api(project_id);
CREATE INDEX IF NOT EXISTS idx_mock_api_method_path ON mock_api(method, path);
CREATE INDEX IF NOT EXISTS idx_mock_api_enabled ON mock_api(enabled);

-- 请求记录表
CREATE TABLE IF NOT EXISTS mock_request_record (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id      INTEGER NOT NULL DEFAULT 1,
    mock_api_id     INTEGER,
    method          TEXT    NOT NULL,
    path            TEXT    NOT NULL,
    client_ip       TEXT,
    request_headers TEXT,
    request_body    TEXT,
    query_params    TEXT,
    response_status INTEGER,
    response_body   TEXT,
    matched         INTEGER NOT NULL DEFAULT 0,
    created_at      TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
    FOREIGN KEY (project_id) REFERENCES project(id)
);

CREATE INDEX IF NOT EXISTS idx_record_project ON mock_request_record(project_id);
CREATE INDEX IF NOT EXISTS idx_record_api_id ON mock_request_record(mock_api_id);
CREATE INDEX IF NOT EXISTS idx_record_created ON mock_request_record(created_at);

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    username        TEXT    NOT NULL UNIQUE,
    password        TEXT    NOT NULL,
    created_at      TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);

-- 默认管理员用户（密码为 admin，BCrypt 加密）
INSERT OR IGNORE INTO user (id, username, password) VALUES (1, 'admin', '$2a$10$kH9msznCbC/55yQpgqatNOx8pXM/z6PsYD4Exd6QtTl1nXs3Q3u46');
