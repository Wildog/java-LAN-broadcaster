import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.Image;
import java.awt.AWTException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Frame;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.PopupMenu;
import java.awt.MenuItem;
import java.net.Socket;
import java.net.InetAddress;
import java.util.List;
import java.util.ArrayList; 
import java.util.Map;

import java.util.Date;
import java.text.SimpleDateFormat;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.DefaultListModel;
import javax.swing.ListSelectionModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/*这个类是服务器端的UI*/
public class ServerUI extends JFrame {
    public static void main(String[] args) {
        ServerUI serverUI = new ServerUI();
    }

    private static final long serialVersionUID = -1677342784183836763L;

    public JButton btSend;//发送信息按钮
    public JButton btSendSelected;//发送所选信息按钮
    public JButton btRefresh;//刷新在线客户端列表
    public JTextField tfSend;//需要发送的文本信息
    public JTextArea taShow;//信息展示
    public Server server;//用来监听客户端连接
    static Map<InetAddress, Socket> clients;//保存连接到服务器的客户端
    static List<SocketWrapper> selectedClients;//保存选择要发送的客户端 
    public JList<SocketWrapper> clientList;
    public DefaultListModel<SocketWrapper> clientListModel;
    public ListSelectionModel clientListSelectionModel;
    public SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public ServerUI() {
        super("通知群发系统");

        Toolkit tk = Toolkit.getDefaultToolkit();
        Image img = tk.getImage("signal.gif");
        SystemTray systemTray = SystemTray.getSystemTray();
        TrayIcon trayIcon = null;

        PopupMenu popupMenu = new PopupMenu();
        MenuItem exit = new MenuItem("退出");
        popupMenu.add(exit);

        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                server.closeServer();
                System.exit(0);
            }
        });

        try {
            trayIcon = new TrayIcon(img, "通知群发系统", popupMenu);
            systemTray.add(trayIcon);
            this.setIconImage(img);
            trayIcon.setImageAutoSize(true);
        } catch (AWTException e2) {
            e2.printStackTrace();
        }

        btSend = new JButton("全部发送");
        btSendSelected = new JButton("所选发送");
        btRefresh = new JButton("刷新列表");
        tfSend = new JTextField(27);
        taShow = new JTextArea();

        btSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = tfSend.getText();
                tfSend.setText("");
                server.sendMsg(text);
                server.println(df.format(new Date()) + ": " + text);
            }
        });
        btSendSelected.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                List<SocketWrapper> protoList = clientList.getSelectedValuesList();
                if (protoList.isEmpty()) {
                    return;
                }
                selectedClients = (ArrayList<SocketWrapper>) protoList;
                String text = tfSend.getText();
                tfSend.setText("");
                for (SocketWrapper wrappedSocket : selectedClients) {
                    server.sendMsgSingle(text, wrappedSocket.socket);
                }
                server.println(df.format(new Date()) + ": " + text);
            }
        });
        btRefresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                server.refreshClients();
            }
        });
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                /* Object[] options = {"最小化到托盘", "确认关闭"};
                int a = JOptionPane.showOptionDialog(null, "确定关闭服务器吗？", "温馨提示",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                if (a == 1) {
                    server.closeServer();
                    System.exit(0);
                } */
            }
            public void windowIconified(WindowEvent e) {
                dispose();
            }
        });

        trayIcon.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    setExtendedState(Frame.NORMAL);
                    setVisible(true);
                }
            }
        });

        JPanel top = new JPanel(new FlowLayout());
        top.add(tfSend);
        top.add(btSend);
        top.add(btSendSelected);
        top.add(btRefresh);
        this.add(top, BorderLayout.SOUTH);

        JPanel area = new JPanel();
        area.setLayout(new BoxLayout(area, BoxLayout.PAGE_AXIS)); 
        final JScrollPane sp = new JScrollPane();
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        sp.setViewportView(this.taShow);
        sp.setPreferredSize(new Dimension(650, 250));
        this.taShow.setEditable(false);
        this.taShow.setLineWrap(true);

        clientListModel = new DefaultListModel<SocketWrapper>();
        clientList = new JList<SocketWrapper>(clientListModel);
        clientList.setVisibleRowCount(10);
        clientList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        clientListSelectionModel = clientList.getSelectionModel();

        JScrollPane clientScrollPane = new JScrollPane(clientList);

        /* JLabel lbMsg = new JLabel("消息列表:");
        JLabel lbList = new JLabel("在线客户端列表:"); */
        
        /* area.add(lbMsg); */
        area.add(sp);
        /* area.add(lbList); */
        area.add(clientScrollPane);

        this.add(area, BorderLayout.CENTER);
        this.getRootPane().setDefaultButton(btSend);
        /* this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); */
        this.setSize(650, 600);
        this.setLocation(100, 200);
        this.setVisible(true);
        tfSend.requestFocus();

        server = new Server(ServerUI.this);
    }
}
