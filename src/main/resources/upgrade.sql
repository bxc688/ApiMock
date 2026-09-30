-- ApiMock 数据库增量升级脚本
-- 执行方式：按顺序执行以下 ALTER 语句，兼容旧库升级

-- 2026-09-30: 请求记录表新增 client_ip 列（记录请求来源 IP）
ALTER TABLE mock_request_record ADD COLUMN client_ip TEXT;
