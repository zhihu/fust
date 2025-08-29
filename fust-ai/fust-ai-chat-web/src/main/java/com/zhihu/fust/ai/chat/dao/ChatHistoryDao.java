package com.zhihu.fust.ai.chat.dao;

import com.zhihu.fust.ai.chat.model.ChatHistoryModel;
import com.zhihu.fust.spring.mybatis.TemplateDao;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ChatHistoryDao extends TemplateDao<ChatHistoryModel> {

    /**
     * 查找所有记录
     */
    @Select("SELECT * FROM " + ChatHistoryModel.TABLE_NAME)
    @ResultMap("ChatHistoryModel")
    List<ChatHistoryModel> findAll();

    /**
     * 查找所有会话ID
     */
    @Select("SELECT DISTINCT conversation_id FROM " +
            ChatHistoryModel.TABLE_NAME + " ORDER BY created_at DESC")
    List<String> findAllConversationIds();

    /**
     * 根据会话ID查找所有消息，按创建时间排序
     */
    @Select("SELECT * FROM " + ChatHistoryModel.TABLE_NAME
            + " WHERE conversation_id = #{conversationId} ORDER BY ID")
    @ResultMap("ChatHistoryModel")
    List<ChatHistoryModel> findByConversationId(@Param("conversationId") String conversationId);


    /**
     * 根据会话ID查找所有消息，按创建时间排序
     */
    @Select("SELECT * FROM " + ChatHistoryModel.TABLE_NAME + " WHERE conversation_id = #{conversationId} ORDER BY ID limit #{limit}")
    @ResultMap("ChatHistoryModel")
    List<ChatHistoryModel> findByConversationIdWithLimit(@Param("conversationId") String conversationId, @Param("limit") int limit);


    /**
     * 根据会话ID删除所有消息
     */
    @Delete("DELETE FROM " + ChatHistoryModel.TABLE_NAME + " WHERE conversation_id = #{conversationId}")
    int deleteByConversationId(@Param("conversationId") String conversationId);

    /**
     * 统计会话中的消息数量
     */
    @Select("SELECT COUNT(*) FROM " + ChatHistoryModel.TABLE_NAME + " WHERE conversation_id = #{conversationId}")
    long countByConversationId(@Param("conversationId") String conversationId);
}
