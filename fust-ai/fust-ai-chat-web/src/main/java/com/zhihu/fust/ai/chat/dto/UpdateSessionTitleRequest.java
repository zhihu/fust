package com.zhihu.fust.ai.chat.dto;

/**
 * 更新会话标题请求DTO
 */
public class UpdateSessionTitleRequest {
    /**
     * 新的会话标题
     */
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}