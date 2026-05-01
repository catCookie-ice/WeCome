-- ============================================
-- 为 news 表添加 author_id 字段
-- 执行时间：2026-05-01
-- 说明：用于关联发布者（members 表的 id）
-- ============================================

-- 添加 author_id 字段
ALTER TABLE news 
ADD COLUMN author_id INT COMMENT '发布者ID，关联 members 表的 id';

-- 可选：为 author_id 添加索引，提高查询性能
CREATE INDEX idx_author_id ON news(author_id);

-- 可选：添加外键约束（如果需要强制引用完整性）
-- ALTER TABLE news 
-- ADD CONSTRAINT fk_news_author 
-- FOREIGN KEY (author_id) REFERENCES members(id) 
-- ON DELETE SET NULL 
-- ON UPDATE CASCADE;
