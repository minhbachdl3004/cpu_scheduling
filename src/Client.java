import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.*;
import java.io.*;
import java.io.File;
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

    CheckboxGroup _cbg_algo;
    Checkbox _cb_FCFS;
    Checkbox _cb_SJF;
    Checkbox _cb_PRIO;
    Checkbox _cb_PPRIO;
    Checkbox _cb_RR;

    JButton _btn_exit;

    Algorithm _algorithm;

    Socket socket = null;
    BufferedWriter out = null;
    BufferedReader in = null;

    String SECRET_KEY = "stackjava.com.if";

    String PUBLIC_KEY;

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

        _btn_exit = new JButton("EXIT");
        _btn_exit.setBounds(20, 280, 200, 60);
        _btn_exit.addActionListener(this);

        _cbg_algo = new CheckboxGroup();

        _cb_FCFS = new Checkbox("First Come First Serve (FCFS)", false, _cbg_algo);
        _cb_FCFS.setBounds(345, 130, 200, 20);
        _cb_FCFS.addItemListener(this);

        _cb_SJF = new Checkbox("Shortest Job First (SJF)", false, _cbg_algo);
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

            // Mã hoá dữ liệu bằng RSA
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.ENCRYPT_MODE, pubKey);
            String msg = SECRET_KEY;
            byte encryptOut[] = c.doFinal(msg.getBytes());
            String strEncrypt = Base64.getEncoder().encodeToString(encryptOut);
            System.out.println("Chuỗi sau khi mã hoá: " + "\n" + strEncrypt);

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
                //Giải mã dữ liệu bằng AES
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
        catch (IOException | JSONException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException i) {
            System.out.println(i);
        }
        catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        catch (BadPaddingException e) {
            e.printStackTrace();
        }
        catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }


    public static void main (String args[]) throws UnknownHostException, IOException {
        Client client = new Client("127.0.0.1", 5000);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String encryptedMessage = "";
        if (e.getSource() == _btn_open) {
            try {
                String userDirLocation = System.getProperty("user.dir");
                File userDir = new File(userDirLocation);
                // default to user directory
                JFileChooser fileChooser = new JFileChooser(userDir);
                File f = null;
                String tmp = "";
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    f = fileChooser.getSelectedFile().getAbsoluteFile();
                    String fileName = f.getName();
                    tmp = f.toString();
                    System.out.println("file chooser: " + fileName);
                    nameFile.setText(fileName);
                    out.write(fileName);
                    out.newLine();
                    out.flush();
                }
                File file = new File(f.getAbsolutePath());
                Scanner sc = new Scanner(file);
                String msgout = "";
                while (sc.hasNextLine()) {
                    System.out.println(sc.nextLine());
                    out.write(sc.nextLine());
                    out.newLine();
                    out.flush();
                }
            }
            catch (Exception i) {
                i.printStackTrace();
            }
        }
        if (e.getSource() == _btn_submit) {
            try {
                _panel_gantt.removeAll();
                _panel_gantt.repaint();
                _panel_gantt.validate();
                _scrollpane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                String algorithm = "";
                if (this._algorithm == null) {
                    JOptionPane.showMessageDialog(_panel_gantt, "Chọn thuật toán lập lịch cần tính!"
                            , "Alert", JOptionPane.WARNING_MESSAGE);
                }
                else {
                    algorithm = _algorithm.toString();
                    System.out.println(algorithm);
                    msg_text.setText(this._algorithm.toString());
                    String algorithmTmp = algorithm;

                    //Mã hóa lấn 1 bởi secretKey AES
                    SecretKeySpec skeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
                    encryptedMessage = Encryption.encryptDataByAES(algorithmTmp, skeySpec);

                    //Mã hóa lần 2 bởi publicKey RSA
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
                                if (temp.matches("-?\\d+(\\.\\d+)?")) {
                                    if (Double.parseDouble(temp) > 0) {
                                        //Mã hóa lấn 1 bởi secretKey AES
                                        skeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
                                        encryptedMessage = Encryption.encryptDataByAES(temp, skeySpec);

                                        //Mã hóa lần 2 bởi publicKey RSA
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
            msg_text.setText("");
        }
        if (e.getSource() == _btn_exit) {
            try {
                String EXIT = "EXIT";

                //Mã hóa lấn 1 bởi secretKey AES
                SecretKeySpec skeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
                encryptedMessage = Encryption.encryptDataByAES(EXIT, skeySpec);

                //Mã hóa lần 2 bởi publicKey RSA
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