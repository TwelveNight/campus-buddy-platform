-- 在 help_info 表中添加 accepted_application_id 字段，用于记录当前接受的申请 ID
ALTER TABLE help_info 
ADD COLUMN accepted_application_id BIGINT DEFAULT NULL COMMENT '已接受的申请ID',
ADD CONSTRAINT fk_accepted_application FOREIGN KEY (accepted_application_id) REFERENCES help_application(application_id);