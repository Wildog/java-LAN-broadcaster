import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Toolkit;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.Image;
import java.awt.AWTException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Frame;
import java.awt.PopupMenu;
import java.awt.MenuItem;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class ClientUI extends JFrame {

    private static final long serialVersionUID = -1671342784187834763L;

    public JButton btStart;
    public JTextArea taShow;
    public ClientThread server;
    public String serverName;
    public File file = new File("ServerIP.cfg");

    public static void main(String[] args) {
        ClientUI client = new ClientUI();
    }
    public ClientUI() {
        super("通知接收");

        Toolkit tk = Toolkit.getDefaultToolkit();
        Image img = tk.getImage("signal.gif");
        SystemTray systemTray = SystemTray.getSystemTray();
        TrayIcon trayIcon = null;

        PopupMenu popupMenu = new PopupMenu();
        MenuItem setServer = new MenuItem("设置服务器地址");
        MenuItem exit = new MenuItem("退出");
        
        popupMenu.add(setServer);
        popupMenu.add(exit);

        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                /* System.out.println("Starting exiting"); */
                /* server.clientClose(); */
                server.stopRunning();
                System.exit(0);
            }
        });

        setServer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getServerName("请输入服务器IP地址:");
                server.println("-----------------------------");
                server.println("服务器IP地址重设为: " + serverName);
                server.println("即将重新连接服务器...");
                server.stopRunning();
                server = new ClientThread(getUI());
            }
        });

        try {
            trayIcon = new TrayIcon(img, "通知接收", popupMenu);
            systemTray.add(trayIcon);
            this.setIconImage(img);
            trayIcon.setImageAutoSize(true);
        } catch (AWTException e2) {
            e2.printStackTrace();
        }

        taShow = new JTextArea();

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                /* Object[] options = {"最小化到托盘", "确认关闭"};
                int a = JOptionPane.showOptionDialog(null, "确定关闭客户端吗？", "温馨提示",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                if (a == 1) {
                    System.out.println("Starting exiting");
                    server.clientClose();
                    System.exit(0);
                } */
            }
            public void windowIconified(WindowEvent e) {
                collapse();
            }
        });

        trayIcon.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    extend();
                }
            }
        });

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.PAGE_AXIS));
        this.add(top, BorderLayout.SOUTH);
        final JScrollPane sp = new JScrollPane();
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        sp.setViewportView(this.taShow);
        this.taShow.setEditable(false);
        this.taShow.setLineWrap(true);
        this.add(sp);
        this.setSize(400, 300);
        this.setLocation(600, 200);
        collapse();

        serverName = readIpFromFile();
        if (serverName == null || serverName.equals("")) {
            getServerName("首次运行，请输入服务器IP地址:");
        }

        server = new ClientThread(this);
    }

    public void extend() {
        scroll();
        setExtendedState(Frame.NORMAL);
        setVisible(true);
    }

    public void collapse() {
        dispose();
    }

    public void alert(String msg) {
        this.setExtendedState(Frame.NORMAL);
        this.setAlwaysOnTop(true);
        JOptionPane.showMessageDialog(this, msg, "通知", JOptionPane.INFORMATION_MESSAGE);
        this.setAlwaysOnTop(false);
    }

    public void getServerName(String msg) {
        this.setExtendedState(Frame.NORMAL);
        this.setAlwaysOnTop(true);
        serverName = JOptionPane.showInputDialog(this, msg, "设置服务器地址", JOptionPane.WARNING_MESSAGE);
        this.setAlwaysOnTop(false);
        try {
            file.createNewFile();
            FileOutputStream out = new FileOutputStream(file);
            out.write(serverName.getBytes());
        } catch (IOException e) {
        }
    }

    public void scroll() {
        this.taShow.setCaretPosition(this.taShow.getText().length());
    } 

    public String readIpFromFile() {
        String str = "";
        try {
            FileInputStream in = new FileInputStream(file);
            int size = in.available();
            byte[] buffer = new byte[size];
            in.read(buffer);
            in.close();
            str = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return str;
    }

    public ClientUI getUI() {
        return this;
    }
}
