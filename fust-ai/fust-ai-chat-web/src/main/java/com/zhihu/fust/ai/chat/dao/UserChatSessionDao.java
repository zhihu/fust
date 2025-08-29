package com.zhihu.fust.ai.chat.dao;

import com.zhihu.fust.ai.chat.model.UserChatSessionModel;
import com.zhihu.fust.spring.mybatis.TemplateDao;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserChatSessionDao extends TemplateDao<UserChatSessionModel> {

    /**
     * 查找所有会话
     */
    @Select("SELECT * FROM user_chat_session ORDER BY updated_at DESC")
    @ResultMap("UserChatSessionModel")
    List<UserChatSessionModel> findAll();

    /**
     * 根据会话ID查找
     */
    @Select("SELECT * FROM user_chat_session WHERE conversation_id = #{conversationId}")
    @ResultMap("UserChatSessionModel")
    UserChatSessionModel findByConversationId(@Param("conversationId") String conversationId);

    /**
     * 根据会话ID删除
     */
    @Delete("DELETE FROM user_chat_session WHERE conversation_id = #{conversationId}")
    int deleteByConversationId(@Param("conversationId") String conversationId);

    /**
     * 更新会话
     */
    @Update("UPDATE user_chat_session SET title = #{title}, updated_at = #{updatedAt} WHERE conversation_id = #{conversationId}")
    int updateByConversationId(@Param("conversationId") String conversationId,
                               @Param("title") String title,
                               @Param("updatedAt") java.time.Instant updatedAt);
}
