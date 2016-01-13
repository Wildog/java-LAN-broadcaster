import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
/*这个类是服务器端的等待客户端发送信息*/
public class ListenerClient extends Thread {
    BufferedReader reader;
    PrintWriter writer;
    ServerUI ui;
    Socket client;
    public ListenerClient(ServerUI ui, Socket client) {
        this.ui = ui;
        this.client=client;
        this.start();
    }
    //为每一个客户端创建线程等待接收信息，然后把信息广播出去
    public void run() {
        String msg = "";
        while (true) {
            try {
                reader = new BufferedReader(new InputStreamReader(
                        client.getInputStream()));
                writer = new PrintWriter(client.getOutputStream(), true);
                msg = reader.readLine();
                sendMsg(msg);
                
            } catch (IOException e) {
                println(e.toString());
                // e.printStackTrace();
                break;
            }
            if (msg != null && msg.trim() != "") {
                println(">>" + msg);
            }
        }
    }
    //把信息广播到所有用户
    public synchronized void sendMsg(String msg) {
        try {
            for (Socket client : ui.clients.values()) {
                writer = new PrintWriter(client.getOutputStream(), true);
                writer.println(msg);
            }

        } catch (Exception e) {
            println(e.toString());
        }
    }

    

    public void println(String s) {
        if (s != null) {
            this.ui.taShow.setText(this.ui.taShow.getText() + s + "\n");
            System.out.println(s + "\n");
        }
    }
}
