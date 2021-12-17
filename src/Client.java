import Algorithm.Algorithm;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.*;
import java.io.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Scanner;
import Encryption.*;

public class Client extends JFrame
        implements ActionListener, ItemListener {

    JPanel _panel_gantt;
    JScrollPane _scrollpane;
    Font _font_job;

    JLabel _lbl_algorithm;

    JTextField msg_text;
    JTextField nameFile;

    JButton _btn_open;
    JButton _btn_submit;
    JButton _btn_send;
    JButton _btn_exit;

    CheckboxGroup _cbg_algo;
    Checkbox _cb_FCFS;
    Checkbox _cb_SJF;
    Checkbox _cb_PRIO;
    Checkbox _cb_PPRIO;
    Checkbox _cb_RR;


    Algorithm _algorithm;

    Socket socket = null;
    BufferedWriter out = null;
    BufferedReader in = null;

    String SECRET_KEY = "stackjava.com.if";

    String PUBLIC_KEY;
    public String filename;
    public String fileNeedCreate;

    void initUI()	{

        _panel_gantt = new JPanel();

        _scrollpane = new JScrollPane(_panel_gantt);
        _scrollpane.setBounds(20, 20, 540, 100);
        _font_job = new Font("Times New Roman", Font.PLAIN, 16);

        _lbl_algorithm = new JLabel("Algorithm: ");
        _lbl_algorithm.setBounds(20, 130, 100, 30);

        msg_text = new JTextField("");
        msg_text.setBounds(110, 135, 100, 30);
        msg_text.setFont(new Font("Times New Roman", Font.PLAIN, 20));
        msg_text.setEditable(false);

        nameFile = new JTextField("");
        nameFile.setBounds(130, 200, 100, 40);
        nameFile.setFont(new Font("Times New Roman", Font.BOLD, 20));
        nameFile.setEditable(false);

        _btn_submit = new JButton("Submit");
        _btn_submit.setBounds(345, 280, 100, 30);
        _btn_submit.addActionListener(this);

        _btn_open = new JButton("Open File");
        _btn_open.setBounds(20, 200, 100, 40);
        _btn_open.addActionListener(this);

        _btn_send = new JButton("Send File");
        _btn_send.setBounds(240, 200, 100, 40);
        _btn_send.addActionListener(this);

        _btn_exit = new JButton("EXIT");
        _btn_exit.setBounds(20, 280, 200, 60);
        _btn_exit.addActionListener(this);

        _cbg_algo = new CheckboxGroup();

        _cb_FCFS = new Checkbox("First Come First Serve (FCFS)", false, _cbg_algo);
        _cb_FCFS.setBounds(345, 130, 200, 20);
        _cb_FCFS.addItemListener(this);

        _cb_SJF = new Checkbox("Shortest Algorithm.Job First (SJF)", false, _cbg_algo);
        _cb_SJF.setBounds(345, 160, 200, 20);
        _cb_SJF.addItemListener(this);

        _cb_PRIO = new Checkbox("Priority (Prio)", false, _cbg_algo);
        _cb_PRIO.setBounds(345, 190, 200, 20);
        _cb_PRIO.addItemListener(this);

        _cb_PPRIO = new Checkbox("Preemptive Priority (P-Prio)", false, _cbg_algo);
        _cb_PPRIO.setBounds(345, 220, 200, 20);
        _cb_PPRIO.addItemListener(this);

        _cb_RR = new Checkbox("Round Robin (RR)", false, _cbg_algo);
        _cb_RR.setBounds(345, 250, 200, 20);
        _cb_RR.addItemListener(this);


        this.setLayout(null);

        this.add(_scrollpane);


        this.add(_lbl_algorithm);

        this.add(msg_text);
        this.add(nameFile);

        this.add(_btn_open);
        this.add(_btn_submit);
        this.add(_btn_send);
        this.add(_btn_exit);

        this.add(_cb_FCFS);
        this.add(_cb_SJF);
        this.add(_cb_PRIO);
        this.add(_cb_PPRIO);
        this.add(_cb_RR);


        this.setSize(600, 500);
        this.setTitle("CPU Scheduling Algorithms");
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    PublicKey publicKey (String publicKey) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(publicKey);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedBytes);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PublicKey pubKey = factory.generatePublic(spec);
            return pubKey;
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Client (String address, int port) throws UnknownHostException {
        try {
            initUI();
            socket = new Socket(address, port);
            System.out.println("Connected");
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String line = "";
            String message = "";

            SecretKeySpec skeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");

            System.out.println("CLient send: " + message);
            line = in.readLine();
            System.out.println("PublicKey: " + line);

            JSONObject jsonObject = new JSONObject(line);

            String publicKey = jsonObject.get("publicKey").toString();

            PUBLIC_KEY = publicKey;

            byte[] decodedBytes = Base64.getDecoder().decode(publicKey);

            X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedBytes);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PublicKey pubKey = factory.generatePublic(spec);

            //Encrypt secretKey by RSA
            String strEncrypt = Encryption.encryptDataByRSA(SECRET_KEY, pubKey);

            String encryptSecretKey = strEncrypt;
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("secretKey", encryptSecretKey);
            encryptSecretKey = jsonObject1.toString();

            out.write(encryptSecretKey);
            out.newLine();
            out.flush();

            String result = "";

            String tmpResult = "";
            while (!line.equals("EXIT")) {
                JTextArea gantt = new JTextArea(5, 20);
                _scrollpane = new JScrollPane(gantt);
                gantt.setEditable(false);
                gantt.setFont(_font_job);
                gantt.setBackground(getBackground());
                line = in.readLine();
                //Decrypt data by AES
                result = Decryption.decryptDataByAES(line, skeySpec);
                tmpResult = result;
                tmpResult = tmpResult.replace(" <-- ", " --> ");
                if (result.equals("EXIT")) return;
                if (!result.equals("RR")) {
                    gantt.setText(result);
                    _panel_gantt.add(gantt);
                    _panel_gantt.validate();

                    String[] ganttChart = result.split(";");
                    for (String a : ganttChart) {
                        System.out.println(a);
                    }
                }
                if (result.equals("RR")) {
                    result = in.readLine();
                    gantt.setText(result);
                    _panel_gantt.add(gantt);
                    _panel_gantt.validate();
                }
                System.out.println(tmpResult);
            }
            in.close();
            out.close();
            socket.close();
        }
        catch (IOException | JSONException | NoSuchAlgorithmException | InvalidKeySpecException i) {
            System.out.println(i);
        }
    }


    public static void main (String args[]) throws UnknownHostException, IOException {
        Client client = new Client("127.0.0.1", 5000);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();

        JFileChooser openFileChooser = new JFileChooser();
        openFileChooser.setCurrentDirectory(new File(s));
        openFileChooser.setFileFilter(new FileNameExtensionFilter("Chỉ chọn file đuôi .txt", "txt"));

        String encryptedMessage = "";
        if (e.getSource() == _btn_open) {
            int returnValue = openFileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                // set the label to the path of the selected file
                filename = openFileChooser.getSelectedFile().getAbsolutePath();
                nameFile.setText(filename);
                fileNeedCreate = openFileChooser.getSelectedFile().getName();
            }
            // if the user cancelled the operation
            else {
                JOptionPane.showMessageDialog(_panel_gantt, "Hãy chọn file cần thực hiện tìm đường đi ngắn nhất", "Alert",
                        JOptionPane.WARNING_MESSAGE);
                nameFile.setText("hãy chọn file cần thực hiện");
            }
        }
        if (e.getSource() == _btn_send) {
            BufferedReader readfile = null;
            if (fileNeedCreate == null && filename == null) {
                JOptionPane.showMessageDialog(_panel_gantt, "Hãy chọn file cần thực hiện tìm đường đi ngắn nhất", "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                nameFile.setText(" Hãy chọn file cần thực hiện!!");
            } else {
                try {
                    boolean flag = true;
                    readfile = new BufferedReader(new FileReader(filename));
                    String line = "";
                    String tmp = "";

                    System.out.println("name: " + fileNeedCreate);
                    //Encrypt first time by secretKey AES
                    SecretKeySpec skeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
                    encryptedMessage = Encryption.encryptDataByAES(fileNeedCreate, skeySpec);

                    //Encrypt second time by publicKey RSA
                    PublicKey pubKey = publicKey(PUBLIC_KEY);
                    encryptedMessage = Encryption.encryptDataByRSA(encryptedMessage, pubKey);

                    out.write(encryptedMessage);
                    out.newLine();
                    out.flush();

                    String map = "\\d+(\\.\\d+)?+;{1}+\\d+(\\.\\d+)?+;{1}+\\d+(\\.\\d+)?+";

                    while ((line = readfile.readLine()) != null) {
                        if (line.matches(map)) {
                            tmp = tmp + line + ":";
                        }
                        else {
                            JOptionPane.showMessageDialog(_panel_gantt, "Dữ liệu trong " + fileNeedCreate + " sai yêu cầu \n"
                                            + "hãy kiểm tra tại dòng (" + line + ") và sửa theo đúng format \n", "Thông báo",
                                    JOptionPane.WARNING_MESSAGE);
                            nameFile.setText("Hãy nhập dữ liệu đúng");
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        System.out.println(tmp);
                        //Encrypt first time by secretKey AES
                        skeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
                        encryptedMessage = Encryption.encryptDataByAES(tmp, skeySpec);

                        //Encrypt second time by publicKey RSA
                        pubKey = publicKey(PUBLIC_KEY);
                        encryptedMessage = Encryption.encryptDataByRSA(encryptedMessage, pubKey);

                        System.out.println("file sau khi mã hóa 2 lần :" + encryptedMessage);
                        out.write(encryptedMessage);
                        out.newLine();
                        out.flush();
                    }
                } catch (FileNotFoundException fileNotFoundException) {
                    fileNotFoundException.printStackTrace();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
        if (e.getSource() == _btn_submit) {
            try {
                _panel_gantt.removeAll();
                _panel_gantt.repaint();
                _panel_gantt.validate();
                _scrollpane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                String algorithm = "";
                if (fileNeedCreate == null && filename == null) {
                    JOptionPane.showMessageDialog(_panel_gantt, "Hãy chọn file cần thực hiện dể giải bài toán lập lịch CPU", "Thông báo",
                            JOptionPane.WARNING_MESSAGE);
                    nameFile.setText(" Hãy chọn file cần thực hiện!!");
                }
                if (this._algorithm == null) {
                    JOptionPane.showMessageDialog(_panel_gantt, "Chọn thuật toán lập lịch cần tính!"
                            , "Alert", JOptionPane.WARNING_MESSAGE);
                }
                else {
                    algorithm = _algorithm.toString();
                    System.out.println(algorithm);
                    msg_text.setText(this._algorithm.toString());
                    String algorithmTmp = algorithm;

                    //Encrypt first time by secretKey AES
                    SecretKeySpec skeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
                    encryptedMessage = Encryption.encryptDataByAES(algorithmTmp, skeySpec);

                    //Encrypt second time by publicKey RSA
                    PublicKey pubKey = publicKey(PUBLIC_KEY);
                    encryptedMessage = Encryption.encryptDataByRSA(encryptedMessage, pubKey);

                    out.write(encryptedMessage);
                    out.newLine();
                    out.flush();

                    if (algorithm.equals("RR")) {
                        while (true) {
                            try {
                                String temp = JOptionPane.showInputDialog("Quantum time: ");
                                if (temp == null) return;
                                if (temp.matches("\\d+(\\.\\d+)?")) {
                                    //Encrypt first time by secretKey AES
                                    skeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
                                    encryptedMessage = Encryption.encryptDataByAES(temp, skeySpec);

                                    //Encrypt second time by publicKey RSA
                                    pubKey = publicKey(PUBLIC_KEY);
                                    encryptedMessage = Encryption.encryptDataByRSA(encryptedMessage, pubKey);
                                    out.write(encryptedMessage);
                                    out.newLine();
                                    out.flush();
                                    break;
                                } else {
                                    JOptionPane.showMessageDialog(_panel_gantt, "Dữ liệu truyền vào không đúng. Nhập lại"
                                            , "Alert", JOptionPane.WARNING_MESSAGE);
                                }
                            } catch (NumberFormatException nfe) {
                                JOptionPane.showMessageDialog(_panel_gantt, "Dữ liệu truyền vào không đúng. Nhập lại"
                                        , "Alert", JOptionPane.WARNING_MESSAGE);
                            }
                        }
                    }
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        msg_text.setText("");
        if (e.getSource() == _btn_exit) {
            try {
                String EXIT = "EXIT";

                //Encrypt first time by secretKey AES
                SecretKeySpec skeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
                encryptedMessage = Encryption.encryptDataByAES(EXIT, skeySpec);

                //Encrypt second time by publicKey RSA
                PublicKey pubKey = publicKey(PUBLIC_KEY);
                encryptedMessage = Encryption.encryptDataByRSA(encryptedMessage, pubKey);

                out.write(encryptedMessage);
                out.newLine();
                out.flush();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if(_cb_FCFS.getState())	{
            _algorithm = Algorithm.FCFS;
            msg_text.setText(_algorithm.toString());
        }
        if(_cb_SJF.getState())	{
            _algorithm = Algorithm.SJF;
            msg_text.setText(_algorithm.toString());
        }
        if(_cb_PRIO.getState())	{
            _algorithm = Algorithm.Prio;
            msg_text.setText(_algorithm.toString());
        }
        if(_cb_PPRIO.getState())	{
            _algorithm = Algorithm.PPrio;
            msg_text.setText(_algorithm.toString());
        }
        if(_cb_RR.getState())	{
            _algorithm = Algorithm.RR;
            msg_text.setText(_algorithm.toString());
        }
    }
}