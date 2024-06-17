package com.kylin.kylinojcodesandbox.model;

import lombok.Data;

/**
 * 进程执行信息
 */
@Data
public class ExecuteMessage {
    private Integer exitValue;//如果用int默认值为0 和正常退出一样了
    private String message;
    private String errorMessage;
    private Long time;//执行用时
}
