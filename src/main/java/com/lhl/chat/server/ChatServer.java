package com.lhl.chat.server;

import com.alibaba.fastjson.JSONObject;
import com.lhl.chat.entity.BotMsg;
import com.lhl.chat.utils.HttpClientUtil;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * 聊天服务器类
 * @author shiyanlou
 *
 */
//@Component
@ServerEndpoint("/websocket")
public class ChatServer {

	private static final String BOT_API = "http://api.qingyunke.com/api.php?key=free&appid=0&msg=";

	private static final Set<Session> clients = new HashSet<Session>();
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");	// 日期格式化
	private Session deleteSession;
	// 记录当前在线连接数
	private static int onlineCount = 0;

	@OnOpen
	public void open(Session session) {
		// 添加初始化操作
		clients.add(session);
		deleteSession = session;
		onlineCount++;
		System.out.println("当前连接数：" + onlineCount +"  又有人来了！");
	}
	
	/**
	 * 接受客户端的消息，并把消息发送给所有连接的会话
	 * @param msg 客户端发来的消息
	 * @param session 客户端的会话
	 */
	@OnMessage
	public void getMessage(String msg, Session session) {
		try {
			// 把客户端的消息解析为JSON对象
			JSONObject jsonObject = JSONObject.parseObject(msg);
			String name = (String) jsonObject.get("nickname");
			String content = (String) jsonObject.get("content");
			System.out.println("来自客户端消息：" + session.getId() + " " + name + "  " + content);
			// 在消息中添加发送日期
			jsonObject.put("date", DATE_FORMAT.format(new Date()));
			// 把消息发送给所有连接的会话
			for (Session openSession : clients) {
				// 添加本条消息是否为当前会话本身发的标志
				jsonObject.put("isSelf", openSession.equals(session));
				// 发送JSON格式的消息
				openSession.getAsyncRemote().sendText(jsonObject.toString());
			}
			if (content.contains("@bot") || content.contains("@Bot")) {
				content = content.replaceAll("@bot", "");
				content = content.replaceAll("@Bot", "");
				content = content.replaceAll("<p>", "");
				content = content.replaceAll("<br>", "");
				content = content.replaceAll("</p>", "");
				content = content.replaceAll("<br/>", "");
				content = content.replaceAll("&nbsp;", "");
				BotMsg botMsg = Optional.ofNullable(getMsgFormBot(content)).orElse(new BotMsg());
				if (botMsg.getResult() == 0) {
					String msgContent = botMsg.getContent();
					jsonObject.put("content", msgContent.replace("{br}", "<br/>"));
					jsonObject.put("isSelf", false);
					jsonObject.put("nickname", "Bot");
					clients.forEach(item -> item.getAsyncRemote().sendText(jsonObject.toString()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@OnClose
	public void close() {
		// 添加关闭会话时的操作
		clients.remove(deleteSession);
		onlineCount--;
		System.out.println("当前连接数: " + onlineCount + "  有人走了！");
	}

	@OnError
	public void error(Throwable t) {
		// 添加处理错误的操作
		System.out.println("发生错误了！");
	}

	private BotMsg getMsgFormBot(String keyWord) {
		try {
			String url = BOT_API + keyWord;
			String result = HttpClientUtil.get(url);
			BotMsg botMsg = JSONObject.parseObject(result, BotMsg.class);
			return botMsg;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}