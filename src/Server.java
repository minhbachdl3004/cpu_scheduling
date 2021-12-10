import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.net.*;
import java.io.*;

public class Server {
    List<Job> _jobs;
    Algorithm _algorithm;
    Double quantum;
    JFileChooser fileChooser;
    Scanner scanner;

    private Socket socket = null;
    private ServerSocket server = null;
    DataOutputStream out = null;
    DataInputStream in = null;

    String drawGanttChart(GanttChart _gantt_chart) {
        String tempGantt = "";
        System.out.print("0.0");
        String result = "0.0";
        for(int i = 0; i < _gantt_chart.getJobList().size(); i++) {
            System.out.print(" -----> " + _gantt_chart.getJobList().get(i) + " <----- " + _gantt_chart.getTimeList().get(i));
            tempGantt = " -----> " + _gantt_chart.getJobList().get(i) + " <----- " + _gantt_chart.getTimeList().get(i);
            result += tempGantt;
        }
        System.out.println("\n\n");
        System.out.println("\n\n");
        return result;
    }

    public void actionPerformed() {
        _jobs.removeAll(_jobs);
        try {
            int index = 0;

//                String userDirLocation = System.getProperty("user.dir");
//                File userDir = new File(userDirLocation);
//                // default to user directory
//                fileChooser = new JFileChooser(userDir);
//
//                File f = null;
//                int result = fileChooser.showOpenDialog(gui);
//                if (result == JFileChooser.APPROVE_OPTION) {
//                    try {
//                        f = fileChooser.getSelectedFile();
//                        FileReader fr = new FileReader(f);
//                        System.out.println("hello is :" + f);
//                        output.read(fr, f);
//                        fr.close();
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                }
            File file = new File(new File("src/input.txt").getAbsolutePath());
            Scanner sc = new Scanner(file);

            while (sc.hasNextLine()) {
                _jobs.add(
                        new Job(_jobs.size() + 1, 0, 0,
                                0, Double.POSITIVE_INFINITY)
                );
                String input = sc.nextLine();
                StringTokenizer st = new StringTokenizer(input, ";");
                String arrival = st.nextToken();
                String burst = st.nextToken();
                String priority = st.nextToken();
                if (_jobs.isEmpty()) return;
                try {
                    _jobs.get(index).setArrivalTime(Double.parseDouble(arrival));
                } catch (Exception ex) {
                }
                try {
                    _jobs.get(index).setBurstTime(Double.parseDouble(burst));
                } catch (Exception ex) {
                }
                try {
                    _jobs.get(index).setPriority(Double.parseDouble(priority));
                } catch (Exception ex) {
                }
                index++;
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        if (_jobs.isEmpty()) {
            return;
        }
        for (int i = 1; i <= _jobs.size(); i++)
            _jobs.get(i - 1).setJobNumber(i);
    }

    public void itemStateChanged(String state)	{
        switch(state) {
            case "FCFS":
                // code block
                _algorithm = Algorithm.FCFS;
                break;
            case "SJF":
                // code block
                _algorithm = Algorithm.SJF;
                break;
            case "Prio":
                // code block
                _algorithm = Algorithm.Prio;
                break;
            case "PPrio":
                // code block
                _algorithm = Algorithm.PPrio;
                break;
            case "RR":
                // code block
                _algorithm = Algorithm.RR;
                break;
            default:
                // code block
                break;
        }
    }

    public Server(int port) throws IOException {
        _jobs = new ArrayList<Job>();
        try {
            server = new ServerSocket(port);
            System.out.println("Server started");
            System.out.println("Waiting for a client ...");
            socket = server.accept();
            System.out.println("Server accepted");
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            String line = "";
            String _quantum = "";

            while (!line.equals("bye")) {
                if (line.equals("bye")) return;
                else {
                    try {
                        line = in.readUTF();
                        System.out.println("Server received: " + line);
                        itemStateChanged(line);
                        actionPerformed();
                        System.out.println("==================================");
                        if (line.equals("RR")) {
                            out.writeUTF("quantum time: ");
                            out.flush();

                            _quantum = in.readUTF();
                            quantum = Double.parseDouble(_quantum);
                            CPU_Scheduling _solver_RR = new CPU_Scheduling(_jobs, _algorithm, quantum);
                            if (_solver_RR.solve()) {
                                String result = "";
                                drawGanttChart(_solver_RR.getGanttChart());
                                result = drawGanttChart(_solver_RR.getGanttChart());
                                out.writeUTF(result);
                                out.flush();
                            }
                        }
                        if (line.equals("FCFS") || line.equals("SJF") || line.equals("Prio") || line.equals("PPrio")){
                            CPU_Scheduling _solver = new CPU_Scheduling(_jobs, _algorithm);
                            if (_solver.solve()) {
                                String result = "";
                                drawGanttChart(_solver.getGanttChart());
                                result = drawGanttChart(_solver.getGanttChart());
                                out.writeUTF(result);
                                out.flush();
                            }
                        }
                        else {
                            out.writeUTF("Dữ liệu truyền vào không dúng:");
                            out.flush();
                        }
                    } catch (IOException i) {
                        System.out.println(i);
                    }
                }
            }
        }
        catch (IOException i) {
            System.out.println(i);
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(5000);
    }
}
