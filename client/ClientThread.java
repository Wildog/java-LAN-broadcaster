import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.InetAddress;

import java.util.Date;
import java.text.SimpleDateFormat;

public class ClientThread extends Thread {
    ClientUI ui;
    Socket client;
    BufferedReader reader;
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private boolean startupFailure;
    private boolean running = true;

    public ClientThread(ClientUI ui) {
        this.ui = ui;
        try {
            client = new Socket(InetAddress.getByName(ui.serverName), 1228);
            reader = new BufferedReader(new InputStreamReader(
                        client.getInputStream()));
            startupFailure = false;
        } catch (IOException e) {
            println("连接服务器 " + ui.serverName + " 失败");
            println("等待稍后重连...");
            // println(e.toString()); 
            // e.printStackTrace(); 
            startupFailure = true;
        }
        this.start();
    }

    public void run() {
        String msg = "";
        while (startupFailure && running) {
            try {
                client = new Socket(InetAddress.getByName(ui.serverName), 1228);
                reader = new BufferedReader(new InputStreamReader(
                        client.getInputStream()));
                startupFailure = false;
                break;
            } catch (IOException e) {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e1) {
                }
            }
        }
        if (running) {
            println("-----------------------------");
            println("连接服务器 " + ui.serverName + " 成功");
            println("开始接收服务器消息...");
            println("-----------------------------");
            println("历史消息:");
        }
        while (running) {
            try {
                msg = reader.readLine();
                if (msg != null) {
                    String trimmedMsg = msg.trim();
                    if (trimmedMsg.equals("refreshClientsRequestedCheckConnectivity")) {
                        continue;
                    }
                    if (msg.trim() != "") {
                        println(">>" + df.format(new Date()) + ": " + msg);
                        ui.scroll();
                        ui.alert(msg);
                    }
                } else {
                    client.close(); 
                    // System.out.println(client.isClosed());     
                    // println("服务器断开连接");
                    // println("一秒后尝试重新连接..."); 
                    try {
                        Thread.sleep(1000L);
                        client = new Socket(InetAddress.getByName(ui.serverName), 1228);
                        reader = new BufferedReader(new InputStreamReader(
                            client.getInputStream()));
                    } catch (InterruptedException e2) {
                    }
                }
            } catch (IOException e) {
            }
        }
    }

    public void println(String s) {
        if (s != null) {
            this.ui.taShow.setText(this.ui.taShow.getText() + s + "\n");
        }
    }

    public void clientClose() {
        if (client != null) {
            /* System.out.println("Client closing");  
            System.out.println(client);   */
            try {
                client.close();  
                /*reader.close(); */
            } catch (IOException e) {
                e.printStackTrace();
            } 
        }
    }

    public void stopRunning() {
        running = false;
    }
}
