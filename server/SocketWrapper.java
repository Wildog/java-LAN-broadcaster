import java.net.Socket;

public class SocketWrapper {

    public Socket socket;

    public SocketWrapper(Socket socket) {
        this.socket = socket;
    }

    public boolean equals(Object that) {
        SocketWrapper thatWrapper = (SocketWrapper) that;
        if (this.socket.equals(thatWrapper.socket)) {
            return true;
        }
        return false;
    } 

    public String toString() {
        return "IP地址: " + this.socket.getInetAddress() + ", 端口号: " + this.socket.getPort(); 
    }

}
