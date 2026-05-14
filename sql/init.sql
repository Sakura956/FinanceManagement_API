-- =====================================================
-- 个人财务管理系统 - 数据库初始化脚本
-- 数据库名: finance_management
-- 创建日期: 2026-05-10
-- =====================================================

-- 1. 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS finance_management
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE finance_management;

-- =====================================================
-- 2. 建表
-- =====================================================

-- -----------------------------------------------------
-- 2.1 用户表 (sys_user)
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
                            `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
                            `phone`         VARCHAR(11)     NOT NULL                 COMMENT '手机号',
                            `password`      VARCHAR(128)    NOT NULL                 COMMENT '密码（BCrypt加密存储）',
                            `nickname`      VARCHAR(50)     DEFAULT NULL             COMMENT '昵称',
                            `role`          VARCHAR(20)     NOT NULL DEFAULT 'USER'  COMMENT '角色：ADMIN-管理员, USER-普通用户',
                            `status`        TINYINT         NOT NULL DEFAULT 0       COMMENT '状态：0-正常, 1-封禁',
                            `avatar_url`    VARCHAR(500)    DEFAULT NULL             COMMENT '头像地址',
                            `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (`id`),
                            UNIQUE INDEX `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';


-- -----------------------------------------------------
-- 2.2 账单分类表 (bill_category)
-- -----------------------------------------------------
DROP TABLE IF EXISTS `bill_category`;
CREATE TABLE `bill_category` (
                                 `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '分类ID',
                                 `name`          VARCHAR(50)     NOT NULL                 COMMENT '分类名称',
                                 `type`          TINYINT         NOT NULL                 COMMENT '类型：0-支出, 1-收入',
                                 `icon`          VARCHAR(100)    DEFAULT NULL             COMMENT '图标标识',
                                 `sort_order`    INT             NOT NULL DEFAULT 0       COMMENT '排序序号',
                                 `is_default`    TINYINT         NOT NULL DEFAULT 0       COMMENT '是否系统预设：0-否, 1-是',
                                 `status`        TINYINT         NOT NULL DEFAULT 0       COMMENT '状态：0-启用, 1-禁用',
                                 `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`id`),
                                 INDEX `idx_type_status` (`type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单分类表';


-- -----------------------------------------------------
-- 2.3 账单记录表 (bill_record)
-- -----------------------------------------------------
DROP TABLE IF EXISTS `bill_record`;
CREATE TABLE `bill_record` (
                               `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '记录ID',
                               `user_id`       BIGINT          NOT NULL                 COMMENT '用户ID',
                               `type`          TINYINT         NOT NULL                 COMMENT '类型：0-支出, 1-收入',
                               `amount`        DOUBLE(12,2)    NOT NULL                 COMMENT '金额',
                               `category_id`   BIGINT          NOT NULL                 COMMENT '分类ID',
                               `description`   VARCHAR(500)    DEFAULT NULL             COMMENT '备注描述',
                               `record_time`   DATETIME        NOT NULL                 COMMENT '记录时间（用户实际消费/收入时间）',
                               `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               PRIMARY KEY (`id`),
                               INDEX `idx_user_time` (`user_id`, `record_time`),
                               INDEX `idx_category` (`category_id`),
                               INDEX `idx_user_type` (`user_id`, `type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单记录表';


-- -----------------------------------------------------
-- 2.4 理财计划表 (finance_plan)
-- -----------------------------------------------------
DROP TABLE IF EXISTS `finance_plan`;
CREATE TABLE `finance_plan` (
                                `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '计划ID',
                                `user_id`           BIGINT          NOT NULL                 COMMENT '用户ID',
                                `name`              VARCHAR(100)    NOT NULL                 COMMENT '计划名称',
                                `initial_amount`    DOUBLE(12,2)    NOT NULL                 COMMENT '初始投入金额',
                                `current_value`     DOUBLE(12,2)    NOT NULL                 COMMENT '当前市值',
                                `expected_roi`      DOUBLE(5,2)     DEFAULT NULL             COMMENT '预期年化收益率(%)',
                                `start_date`        DATE            NOT NULL                 COMMENT '开始日期',
                                `end_date`          DATE            DEFAULT NULL             COMMENT '结束日期',
                                `status`            TINYINT         NOT NULL DEFAULT 0       COMMENT '状态：0-持有中, 1-已赎回',
                                `remark`            VARCHAR(500)    DEFAULT NULL             COMMENT '备注',
                                `create_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                PRIMARY KEY (`id`),
                                INDEX `idx_user_status` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='理财计划表';


-- -----------------------------------------------------
-- 2.5 备忘录表 (memo)
-- -----------------------------------------------------
DROP TABLE IF EXISTS `memo`;
CREATE TABLE `memo` (
                        `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '备忘录ID',
                        `user_id`       BIGINT          NOT NULL                 COMMENT '用户ID',
                        `title`         VARCHAR(200)    NOT NULL                 COMMENT '标题',
                        `content`       TEXT            DEFAULT NULL             COMMENT '内容',
                        `remind_time`   DATETIME        DEFAULT NULL             COMMENT '提醒时间',
                        `is_completed`  TINYINT         NOT NULL DEFAULT 0       COMMENT '是否完成：0-未完成, 1-已完成',
                        `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`),
                        INDEX `idx_user_remind` (`user_id`, `remind_time`),
                        INDEX `idx_user_completed` (`user_id`, `is_completed`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='备忘录表';


-- -----------------------------------------------------
-- 2.6 AI对话记录表 (ai_conversation)
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ai_conversation`;
CREATE TABLE `ai_conversation` (
                                   `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '对话记录ID',
                                   `user_id`       BIGINT          NOT NULL                 COMMENT '用户ID',
                                   `session_id`    VARCHAR(64)     NOT NULL                 COMMENT '会话ID（UUID，标识一次完整对话）',
                                   `role`          VARCHAR(20)     NOT NULL                 COMMENT '角色：user-用户, assistant-AI助手',
                                   `content`       TEXT            NOT NULL                 COMMENT '消息内容',
                                   `tokens_used`   INT             NOT NULL DEFAULT 0       COMMENT '消耗Token数（AI回复时记录）',
                                   `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   PRIMARY KEY (`id`),
                                   INDEX `idx_user_session` (`user_id`, `session_id`),
                                   INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话记录表';


-- =====================================================
-- 3. 插入初始数据
-- =====================================================

-- -----------------------------------------------------
-- 3.1 插入默认管理员账号
--     手机号: 13800000000
--     密码:   admin123  (BCrypt加密后)
-- -----------------------------------------------------
INSERT INTO `sys_user` (`phone`, `password`, `nickname`, `role`, `status`)
VALUES ('13800000000', '$2a$10$68jc8IFBPe.7Gg6LiKfq4eBEniHjogI7B9oEdjxkkanRmqQENHcoi', '系统管理员', 'ADMIN', 0);

-- -----------------------------------------------------
-- 3.2 插入默认支出分类 (type=0)
-- -----------------------------------------------------
INSERT INTO `bill_category` (`name`, `type`, `icon`, `sort_order`, `is_default`, `status`) VALUES
                                                                                               ('餐饮',   0, 'food',       1,  1, 0),
                                                                                               ('交通',   0, 'car',        2,  1, 0),
                                                                                               ('购物',   0, 'shopping',   3,  1, 0),
                                                                                               ('住房',   0, 'house',      4,  1, 0),
                                                                                               ('娱乐',   0, 'entertainment', 5, 1, 0),
                                                                                               ('医疗',   0, 'medical',    6,  1, 0),
                                                                                               ('教育',   0, 'education',  7,  1, 0),
                                                                                               ('通讯',   0, 'phone',      8,  1, 0),
                                                                                               ('服饰',   0, 'clothes',    9,  1, 0),
                                                                                               ('运动',   0, 'sport',      10, 1, 0),
                                                                                               ('旅行',   0, 'travel',     11, 1, 0),
                                                                                               ('人情',   0, 'gift',       12, 1, 0),
                                                                                               ('宠物',   0, 'pet',        13, 1, 0),
                                                                                               ('其他支出', 0, 'other',    99,  1, 0);

-- -----------------------------------------------------
-- 3.3 插入默认收入分类 (type=1)
-- -----------------------------------------------------
INSERT INTO `bill_category` (`name`, `type`, `icon`, `sort_order`, `is_default`, `status`) VALUES
                                                                                               ('工资',     1, 'salary',     1,  1, 0),
                                                                                               ('奖金',     1, 'bonus',      2,  1, 0),
                                                                                               ('理财收益',  1, 'finance',   3,  1, 0),
                                                                                               ('兼职',     1, 'parttime',   4,  1, 0),
                                                                                               ('红包',     1, 'redpacket',  5,  1, 0),
                                                                                               ('退款',     1, 'refund',     6,  1, 0),
                                                                                               ('报销',     1, 'reimburse',  7,  1, 0),
                                                                                               ('其他收入',  1, 'other',     99, 1, 0);

-- -----------------------------------------------------
-- 3.4 生成管理员密码加密（BCrypt在线生成示例）
--     实际部署时，请在应用中用BCryptPasswordEncoder.encode("admin123")生成替换
-- -----------------------------------------------------
-- 注意：上面3.1中插入的密文仅为格式占位。
-- 正确做法：
-- 方式1：启动项目后，写一个简单测试类生成密文，然后替换
-- 方式2：执行下面 UPDATE 语句替换（需要先生成真实密文）
--
-- 生成密文示例代码：
--   BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
--   String encodedPassword = encoder.encode("admin123");
--   System.out.println(encodedPassword);