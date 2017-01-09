package cn.itcast.socketdemo.socket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by User on 2017/1/9.
 */

public class ImServer {
    private static boolean isServerRunning = false;
    //用户名  用户对应对象
    public static HashMap<String, BufferedWriter> clients = new HashMap<>();
    private static ServerSocket serverSocket = null;

    public static void main(String[] args) {
        try {
            //创建im连接
            serverSocket = new ServerSocket(5222);
            System.out.println("服务器已连接");
            while (isServerRunning) {
                Socket socket = serverSocket.accept();//监听器连接
                System.out.println("客服端连接到服务器" + socket.getInetAddress() +
                        "  " + socket.getLocalPort());
                new Thread(new MyRunnable(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class MyRunnable implements Runnable {
        private Socket client;
        private BufferedReader mReader;
        private BufferedWriter mWriter;

        //连接到服务器的客服端Socket对象
        public MyRunnable(Socket socket) {
            this.client = socket;
            try {
                mReader = new BufferedReader(new
                        InputStreamReader(client.getInputStream(), "utf-8"));
                mWriter = new BufferedWriter(new
                        OutputStreamWriter(client.getOutputStream(), "utf-8"));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    mReader.close();
                    mReader = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    mWriter.close();
                    mWriter = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void run() {
            try {
                while (true) {

                    String line = mReader.readLine();
                    System.out.println("--服务器收到客户器的一条信息:" + line);

                    String[] arrays = line.split(":");
                    String action = arrays[0];
                    if ("login".equals(action)) {
                        String acount1 = arrays[1];
                        String pswd = arrays[2];
                        System.out.println("--服务器收到客户器的一条信息:" + acount1);
                        clients.put(acount1, mWriter);
                    } else if ("send".equals(action)) {
                        String to = arrays[1];
                        String content = arrays[2];
                        if (clients.containsKey(to)) {
                            //说明用户在线  转发消息
                            BufferedWriter writer = clients.get(to);
                            writer.write(content + "\r\n");
                            writer.flush();
                            System.out.println("服务器转发成功" + to + "  " + content);
                        } else {
                            //用户没有登录
                            System.out.println("用户无法转发消息" + to);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
