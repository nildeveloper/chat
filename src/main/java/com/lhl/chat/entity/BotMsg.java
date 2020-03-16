package com.lhl.chat.entity;

/**
 * @author liuhaolu01
 * @date: 2020/3/13 14:16
 * @description:
 */
public class BotMsg {

    private int result;

    private String content;

    public BotMsg(){}

    public BotMsg(int result, String content) {
        this.result = result;
        this.content = content;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
