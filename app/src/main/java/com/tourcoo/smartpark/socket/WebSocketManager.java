package com.tourcoo.smartpark.socket;


import com.apkfuns.logutils.LogUtils;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.tourcoo.smartpark.util.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class WebSocketManager {

    private static final int DEFAULT_SOCKET_CONNECT_TIMEOUT = 5000;
    private static final int DEFAULT_SOCKET_RECONNECT_INTERVAL = 5000;
    private static final int DEFAULT_SOCKET_PING_INTERVAL = 10000;
    private static final int FRAME_QUEUE_SIZE = 5;

    private WebSocketListener mWebSocketListener;
    private WebSocketFactory mWebSocketFactory;
    private WebSocket mWebSocket;
    private List<TimerTask> timerTaskList = new ArrayList<>();
    private List<TimerTask> pingTaskList = new ArrayList<>();
    private ConnectStatus mConnectStatus = ConnectStatus.CONNECT_DISCONNECT;
    private Timer mPingTimer = new Timer();
    private TimerTask mPingTimerTask;
    private Timer mReconnectTimer = new Timer();
    private String mUri;
    private String mToken;

    private boolean isEnableDisconnect = false;

    public interface WebSocketListener {
        void onConnected(Map<String, List<String>> headers);

        void onTextMessage(String text);
    }

    public enum ConnectStatus {
        CONNECT_DISCONNECT,// 断开连接
        CONNECT_SUCCESS,//连接成功
        CONNECT_FAIL,//连接失败
        CONNECTING //正在连接
    }

    public WebSocketManager(String url, String token) {
        this(url, token, DEFAULT_SOCKET_CONNECT_TIMEOUT);
    }

    public WebSocketManager(String url, String token, int timeout) {
        mUri = url;
        mToken = StringUtil.getNotNullValue(token);
        mWebSocketFactory = new WebSocketFactory().setConnectionTimeout(timeout);
    }

    public void setWebSocketListener(WebSocketListener webSocketListener) {
        mWebSocketListener = webSocketListener;
    }

    public void connect() {
        try {
            mWebSocket = mWebSocketFactory.createSocket(mUri)
                    .setFrameQueueSize(FRAME_QUEUE_SIZE)//设置帧队列最大值为5
                    .setMissingCloseFrameAllowed(false)//设置不允许服务端关闭连接却未发送关闭帧
                    .addListener(new NVWebSocketListener())
                    .addHeader("Authorization", mToken)
                    .connectAsynchronously();  // 同步调用请使用connect()
            setConnectStatus(ConnectStatus.CONNECTING);
        } catch (IOException e) {
            e.printStackTrace();
            reconnect();
        }
    }

    public void setEnableDisconnect(boolean isEnableDisconnect) {
        this.isEnableDisconnect = isEnableDisconnect;
    }

    public void setPingInterval(long time) {
        if (mPingTimerTask == null) {
            releaseTask(pingTaskList);
            mPingTimerTask = new TimerTask() {
                @Override
                public void run() {
                    if (mWebSocket != null) {
                        mWebSocket.sendText("ping");
                    }
                }
            };
            pingTaskList.add(mPingTimerTask);
        }
        if (time <= 0) {
            time = DEFAULT_SOCKET_PING_INTERVAL;
        }
        mPingTimer.schedule(mPingTimerTask, 0, time);
    }

    public void setPingInterval() {
        setPingInterval(DEFAULT_SOCKET_PING_INTERVAL);
    }

    // 客户端像服务器发送消息
    public void sendMessage(String start, String end) {
        try {
            JSONObject json = new JSONObject();
            json.put("start", start);
            json.put("end", end);
            mWebSocket.sendText(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setConnectStatus(ConnectStatus connectStatus) {
        mConnectStatus = connectStatus;
    }

    public ConnectStatus getConnectStatus() {
        return mConnectStatus;
    }


    public void disconnect() {
        isEnableDisconnect = true;
        if (mWebSocket != null) {
            mWebSocket.disconnect();
        }
        setConnectStatus(null);
    }


    // Adapter的回调中主要做三件事：1.设置连接状态2.回调websocketlistener3.连接失败重连
    class NVWebSocketListener extends WebSocketAdapter {

        @Override
        public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
            super.onConnected(websocket, headers);
            LogUtils.d("---->OS. WebSocket onConnected");
            setConnectStatus(ConnectStatus.CONNECT_SUCCESS);
            if (mWebSocketListener != null) {
                mWebSocketListener.onConnected(headers);
            }
        }

        @Override
        public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
            super.onConnectError(websocket, exception);
            LogUtils.e("---->OS. WebSocket onConnectError：" + exception);
            setConnectStatus(ConnectStatus.CONNECT_FAIL);
            if (!isEnableDisconnect) {
                reconnect();
            }
        }

        @Override
        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
            super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
            LogUtils.e("---->OS. WebSocket onDisconnected");
            setConnectStatus(ConnectStatus.CONNECT_DISCONNECT);
            if (!isEnableDisconnect) {
                reconnect();
            }
        }

        @Override
        public void onTextMessage(WebSocket websocket, String text) throws Exception {
            super.onTextMessage(websocket, text);
            LogUtils.d("---->OS. WebSocket onTextMessage :" + text);
            if (mWebSocketListener != null) {
                mWebSocketListener.onTextMessage(text);
            }
        }
    }

    public void reconnect() {
        releaseTask(timerTaskList);
        if (mWebSocket != null && !mWebSocket.isOpen() && getConnectStatus() != ConnectStatus.CONNECTING) {
            TimerTask mReconnectTimerTask = new TimerTask() {
                @Override
                public void run() {
                    connect();
                }
            };
            timerTaskList.add(mReconnectTimerTask);
            mReconnectTimer.schedule(mReconnectTimerTask, DEFAULT_SOCKET_RECONNECT_INTERVAL);
        }
    }


    public void release() {
        disconnect();
        releaseTask(timerTaskList);
        releaseTask(pingTaskList);
        if (mPingTimer != null) {
            mPingTimer.cancel();
        }
    }

    private void releaseTask(List<TimerTask> taskList) {
        if (taskList != null) {
            TimerTask timerTask;
            for (int i = taskList.size() - 1; i >= 0; i--) {
                timerTask = taskList.get(i);
                if (timerTask != null) {
                    timerTask.cancel();
                    taskList.remove(timerTask);
                    LogUtils.d("---->定时器已被移除");
                }
            }
        }
    }
}


