CREATE TABLE `chat_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `conversation_id` varchar(255) NOT NULL DEFAULT '' COMMENT '关联会话ID',
  `msg_type` varchar(20) NOT NULL DEFAULT '' COMMENT '消息类型 (user/assistant/system/custom)',
  `msg_content` text COMMENT '消息内容',
  `metadata` text COMMENT '元数据(模型名/tokens/延迟等)',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_chat_history_cid` (`conversation_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='AI对话历史';


CREATE TABLE `user_chat_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(64) NOT NULL  DEFAULT ''  COMMENT '用户ID',
  `conversation_id` varchar(64) NOT NULL  DEFAULT '' COMMENT '会话ID',
  `title` varchar(64) NOT NULL DEFAULT ''  COMMENT '对话标题',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_idx_conversation_id` (`conversation_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='用户对话会话';
