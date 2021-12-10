import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.*;
import java.io.*;


public class Client extends JFrame
        implements ActionListener {

    JPanel _panel_gantt;
    JScrollPane _scrollpane;
    Font _font_job;

    JLabel _lbl_algorithm;
    JTextField msg_text;

    CheckboxGroup _cbg_algo;

    JButton _btn_run;
    JButton _btn_open;
    JButton _btn_submit;

    Socket socket = null;
    DataInputStream in;
    DataOutputStream out;

    void initUI()	{

        _panel_gantt = new JPanel();

        _scrollpane = new JScrollPane(_panel_gantt);
        _scrollpane.setBounds(20, 20, 540, 100);
        _font_job = new Font("Times New Roman", Font.PLAIN, 16);

        _lbl_algorithm = new JLabel("Algorithm: ");
        _lbl_algorithm.setBounds(20, 130, 100, 30);

        msg_text = new JTextField("");
        msg_text.setBounds(110, 135, 100, 20);

        _btn_submit = new JButton("Submit");
        _btn_submit.setBounds(230, 130, 100, 30);
        _btn_submit.addActionListener(this);

//		_lbl_arrival = new JLabel("Arrival Time: ");
//		_lbl_arrival.setBounds(20, 160, 100, 20);
//		_txt_arrival = new JLabel();
//		_txt_arrival.setBounds(100, 160, 50, 20);
//
//		_lbl_burst = new JLabel("Burst Time: ");
//		_lbl_burst.setBounds(20, 185, 100, 20);
//		_txt_burst = new JLabel();
//		_txt_burst.setBounds(100, 185, 50, 20);
//
//
//		_lbl_priority = new JLabel("Priority: ");
//		_lbl_priority.setBounds(190, 160, 100, 20);
//		_txt_priority = new JLabel();
//		_txt_priority.setBounds(250, 160, 50, 20);


        _btn_run = new JButton("Run");
        _btn_run.setBounds(150, 200, 60, 40);
        _btn_run.addActionListener(this);

        _btn_open = new JButton("Open File");
        _btn_open.setBounds(20, 200, 100, 40);
        _btn_open.addActionListener(this);

        this.setLayout(null);

        this.add(_scrollpane);


//		this.add(_lbl_arrival);
//		this.add(_txt_arrival);
//
//		this.add(_lbl_burst);
//		this.add(_txt_burst);
//
//		this.add(_lbl_priority);
//		this.add(_txt_priority);

        this.add(_lbl_algorithm);
        this.add(msg_text);

        this.add(_btn_run);
        this.add(_btn_open);
        this.add(_btn_submit);


        this.setSize(600, 500);
        this.setTitle("CPU Scheduling Algorithms");
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public Client (String address, int port) throws UnknownHostException, IOException {
        initUI();
        socket = new Socket(address, port);
        System.out.println("Connected");
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        String line = "";

        while (!line.equals("bye")) {
            JTextArea gantt = new JTextArea(5, 20);
            _scrollpane = new JScrollPane(gantt);
            gantt.setEditable(false);
            gantt.setFont(_font_job);
            gantt.setBackground(getBackground());
            line = in.readUTF();
            if (line.equals("bye")) return;
            if (!line.equals("RR")) {
                gantt.setText(line);
                _panel_gantt.add(gantt);
                _panel_gantt.validate();
            }
            if (line.equals("RR")) {
                line = in.readUTF();
                gantt.setText(line);
                _panel_gantt.add(gantt);
                _panel_gantt.validate();
            }
        }

        in.close();
        out.close();
        socket.close();
    }


    public static void main (String args[]) throws UnknownHostException, IOException {
        Client client = new Client("127.0.0.1", 5000);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == _btn_submit) {
            try {
                _panel_gantt.removeAll();
                _panel_gantt.repaint();
                _panel_gantt.validate();
                _scrollpane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                String msgout = "";
                msgout = msg_text.getText();
                String msgouttmp = msgout;
                out.writeUTF(msgout);
                out.flush();
                if (msgouttmp.equals("RR")) {
                    try {
                        String temp = JOptionPane.showInputDialog("Quantum time: ");
                        Double quantum = Double.parseDouble(temp);
                        out.writeUTF(String.valueOf(quantum));
                        out.flush();
                    }
                    catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(_panel_gantt, "Dữ liệu truyền vào không đúng. Nhập lại"
                                ,"Alert",JOptionPane.WARNING_MESSAGE);
                    }
                }
                if (!msgouttmp.equals("FCFS") && !msgouttmp.equals("SJF") && !msgouttmp.equals("Prio")
                        && !msgouttmp.equals("PPrio") && !msgouttmp.equals("RR") && !msgouttmp.equals("bye")) {
                    JOptionPane.showMessageDialog(_panel_gantt, "Dữ liệu truyền vào không đúng. Nhập lại","Alert",JOptionPane.WARNING_MESSAGE);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            msg_text.setText("");
        }
    }
}
