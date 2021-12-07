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
    BufferedWriter out = null;
    BufferedReader in = null;

    String drawGanttChart(GanttChart _gantt_chart) {
        System.out.print("0.0");
        String result = "0.0";
        for(int i = 0; i < _gantt_chart.getJobList().size(); i++)	{
            System.out.print(" -----Process" + (i+1)  + "----- " + _gantt_chart.getTimeList().get(i));
            String tempGantt = " -----Process" + (i+1) + "----- " + _gantt_chart.getTimeList().get(i);
            result += tempGantt;
        }
        System.out.println("\n\n");
        System.out.println(result);
        System.out.println("\n\n");
        return result;
    }

    public void actionPerformed() {
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

    public void itemStateChanged(int state)	{
        switch(state) {
            case 1:
                // code block
                _algorithm = Algorithm.FCFS;
                break;
            case 2:
                // code block
                _algorithm = Algorithm.SJF;
                break;
            case 3:
                // code block
                _algorithm = Algorithm.Prio;
                break;
            case 4:
                // code block
                _algorithm = Algorithm.PPrio;
                break;
            case 5:
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
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = "";
            String result = "";
            String _quantum = "";

            while (!line.equals("bye")) {
                try {
                    line = in.readLine();
                    System.out.println("Server received: " + line);
                    int algorithm = Integer.parseInt(line);
                    itemStateChanged(algorithm);
                    actionPerformed();
                    System.out.println("==================================");
                    if (algorithm == 5) {
                        out.write("quantum time: ");
                        out.newLine();
                        out.flush();

                        _quantum = in.readLine();
                        quantum = Double.parseDouble(_quantum);
                        CPU_Scheduling _solver_RR = new CPU_Scheduling(_jobs, _algorithm, quantum);
                        if (_solver_RR.solve()) {
                            drawGanttChart(_solver_RR.getGanttChart());
                            result = drawGanttChart(_solver_RR.getGanttChart());
                            out.write(result);
                            out.newLine();
                            out.flush();
                        }
                    }
                    else {
                        CPU_Scheduling _solver = new CPU_Scheduling(_jobs, _algorithm);
                        if (_solver.solve()) {
                            drawGanttChart(_solver.getGanttChart());
                            result = drawGanttChart(_solver.getGanttChart());
                            out.write(result);
                            out.newLine();
                            out.flush();
                        }
                    }
                }
                catch (IOException i) {
                    System.out.println(i);
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
