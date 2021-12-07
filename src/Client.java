import java.net.*;
import java.io.*;

public class Client {
    private Socket socket = null;
    BufferedWriter out = null;
    BufferedReader in = null;
    BufferedReader stdIn = null;

    public Client (String address, int port) throws UnknownHostException, IOException {
        socket = new Socket(address, port);
        System.out.println("Connected");
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        stdIn = new BufferedReader(new InputStreamReader(System.in));

        String line = "";

        while (!line.equals("bye")) {
            System.out.println("1. FCFS");
            System.out.println("2. SJF");
            System.out.println("3. Prio");
            System.out.println("4. PPrio");
            System.out.println("5. Round Robin");
            System.out.print("Nhập lựa chọn của bạn: ");
            line = stdIn.readLine();
            System.out.println("================================");
            System.out.println("CLient sent :" + line);
            out.write(line);
            out.newLine();
            out.flush();

            if (!line.equals("quantum time: ")) {
                line = in.readLine();
                System.out.println(line);
            }
            if (line.equals("quantum time: ")) {
                System.out.print("Nhập quantum time: ");
                line = stdIn.readLine();
                System.out.println("================================");
                System.out.println("CLient sent :" + line);
                out.write(line);
                out.newLine();
                out.flush();

                line = in.readLine();
                System.out.println(line);
            }
        }

        in.close();
        out.close();
        socket.close();
    }

    public static void main (String args[]) throws UnknownHostException, IOException {
        Client client = new Client("127.0.0.1", 5000);
    }
}
