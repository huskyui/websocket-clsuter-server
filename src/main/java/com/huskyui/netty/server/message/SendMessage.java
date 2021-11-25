package com.huskyui.netty.server.message;

import lombok.Data;

/**
 * @author 王鹏
 */
@Data
public class SendMessage {
    private String type;
    private String user;
    private String toUser;
    private String message;
}
