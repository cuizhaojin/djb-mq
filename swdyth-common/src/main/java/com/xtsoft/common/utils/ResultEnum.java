package com.xtsoft.common.utils;

/**
 * @author cheleilei
 * @date 2022/01/21 14:25
 */
public enum ResultEnum {
    //这里是可以自己定义的，方便与前端交互即可
    SUCCESS("success", "成功"),
    ERROR("failed", "失败"),
    IMPORT_EXCEL_TOKEN("0200", "模板上传,校验通过"),
    IMPORT_EXCEL_TOKEN_ERROR("0400", "模板上传,token校验未通过"),
    IMPORT_EXCEL_ING("0500", "文件上传中,请勿重复提交"),
    CHECKEXCEL_ERROR("0300", "模板上传,校验失败");
    private final String status;
    private final String msg;

    ResultEnum(String status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public String getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }
}