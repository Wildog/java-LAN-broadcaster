import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
/*这个类是服务器端的等待客户端连接*/
public class Server extends Thread {
    ServerUI ui;
    ServerSocket ss;
    PrintWriter writer;

    public Server(ServerUI ui) {
        this.ui = ui;
        this.start();
    }

    public void run() {
        try {

            ss = new ServerSocket(1228);
            println("-----------------------------");
            println("启动服务器成功：端口 1228");
            println("开始接受客户端连接...");
            println("提示: 在线列表中按住 Ctrl 以多选");
            println("-----------------------------");
            println("历史消息:");
            ui.clients = new HashMap<InetAddress, Socket>();
            
            while (true) {
                /* println("等待客户端"); */
                Socket client = ss.accept();
                InetAddress clientAddr = client.getInetAddress();
                if (ui.clients.containsKey(clientAddr)) {
                    Socket origSocket = ui.clients.get(clientAddr);
                    ui.clientListModel.removeElement(new SocketWrapper(origSocket));
                    ui.clients.remove(clientAddr); 
                    /*clientListener still needs to be terminated*/
                }
                ui.clients.put(clientAddr, client);
                ui.clientListModel.addElement(new SocketWrapper(client));

                /* println("连接成功" + client.toString()); */
            }
        } catch (IOException e) {
            println("启动服务器失败：端口 1228");
            println(e.toString());
            e.printStackTrace();
        }

    }

    public synchronized void refreshClients() {
        try {
            int maxtries = 6;
            for (int i = 0; i < maxtries; i++) {
                for (Socket client : ui.clients.values()) {
                    writer = new PrintWriter(client.getOutputStream(), true);
                    writer.println("refreshClientsRequestedCheckConnectivity");
                    if (writer.checkError()) {
                        /* System.out.println(client);
                        System.out.println("client failed to connect, closing client"); */
                        InetAddress clientAddr = client.getInetAddress();
                        ui.clients.remove(clientAddr);
                        ui.clientListModel.removeElement(new SocketWrapper(client));
                    }
                }
            }
        } catch (Exception e) {
            println("刷新失败");
            println(e.toString());
        }
    }

    public synchronized void sendMsg(String msg) {
        try {
            for (Socket client : ui.clients.values()) {
                writer = new PrintWriter(client.getOutputStream(), true);
                writer.println(msg);
            }
        } catch (Exception e) {
            println("发送失败");
            println(e.toString());
        }
    }

    public synchronized void sendMsgSingle(String msg, Socket client) {
        try {
            writer = new PrintWriter(client.getOutputStream(), true);
            writer.println(msg);
        } catch (Exception e) {
            println("发送失败");
            println(e.toString());
        }
    }

    public void println(String s) {
        if (s != null) {
            this.ui.taShow.setText(this.ui.taShow.getText() + s + "\n");
            /* System.out.println(s + "\n"); */
        }
    }

    public void closeServer() {
        try {
            if (ss != null) {
                ss.close();
            }
            if (writer != null) {
                writer.close();
            }
            for (Socket client : ui.clients.values()) {
                client.close();
                // System.out.println(client);
                // System.out.println("client closed");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
