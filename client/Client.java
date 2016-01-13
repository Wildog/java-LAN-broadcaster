public class Client extends Thread {
    private ClientUI ui;

    public Client(ClientUI ui) {
        this.ui = ui;
    }

    public void run() {
        while (true) {
            ui.server = new ClientThread(ui);
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
            }
        }
    }
}
