package com.muniao.websocketdemo.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/im/{userId}")
@Component
@Slf4j
public class ImController
{

    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;
    //新：使用map对象，便于根据userId来获取对应的WebSocket
    private static ConcurrentHashMap<String, Session> websocketList = new ConcurrentHashMap<>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    //接收sid
    private String userId = "";

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId)
    {
        this.session = session;
        websocketList.put(userId, session);
        addOnlineCount();           //在线数加1
        log.info("有新窗口开始监听:" + userId + ",当前在线人数为" + getOnlineCount());
        this.userId = userId;
        try
        {
            sendMessage(this.session, JSON.toJSONString("连接成功"));
        }
        catch (IOException e)
        {
            log.error("websocket IO异常");
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose()
    {
        if (websocketList.get(this.userId) != null)
        {
            websocketList.remove(this.userId);
            subOnlineCount();           //在线数减1
            log.info("有一连接关闭！当前在线人数为" + getOnlineCount());
        }
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session)
    {
        log.info("收到来自窗口" + userId + "的信息:" + message);
        if (!StringUtils.isEmpty(message))
        {
            JSONArray list = JSONArray.parseArray(message);
            for (int i = 0; i < list.size(); i++)
            {
                try
                {
                    //解析发送的报文
                    JSONObject object = list.getJSONObject(i);
                    String toUserId = object.getString("toUserId");
                    String contentText = object.getString("contentText");
                    object.put("fromUserId", this.userId);
                    //传送给对应用户的websocket
                    if (!StringUtils.isEmpty(toUserId) && !StringUtils.isEmpty(contentText))
                    {
                        Session toSession = websocketList.get(toUserId);
                        //需要进行转换，userId
                        if (toSession != null)
                        {
                            sendMessage(toSession,message);
                            //此处可以放置相关业务代码，例如存储到数据库
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error)
    {
        log.error("发生错误");
        error.printStackTrace();
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(Session session, String message)throws IOException
    {
        session.getBasicRemote().sendText(message);
    }

    public static synchronized int getOnlineCount()
    {
        return onlineCount;
    }

    public static synchronized void addOnlineCount()
    {
        ImController.onlineCount++;
    }

    public static synchronized void subOnlineCount()
    {
        ImController.onlineCount--;
    }
}


