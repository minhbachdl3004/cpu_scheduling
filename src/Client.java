import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.io.*;
import java.io.File;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


public class Client extends JFrame
        implements ActionListener {

    JPanel _panel_gantt;
    JScrollPane _scrollpane;
    Font _font_job;

    JLabel _lbl_algorithm;
    JLabel _lbl_fileName;
    JTextField msg_text;
    JTextField nameFile;

    JButton _btn_open;
    JButton _btn_submit;

    Socket socket = null;
    BufferedWriter out = null;
    BufferedReader in = null;

    String SECRET_KEY = "stackjava.com.if";

    String PUBLIC_KEY;

    String encryptDataByAES (String data, Key seckey) {
        String result = "";
        try {
            //Mã hóa dữ liệu bằng AES
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, seckey);
            byte[] byteEncrypted = cipher.doFinal(data.getBytes());
            String encryptedData = Base64.getEncoder().encodeToString(byteEncrypted);
            System.out.println("Dữ liệu sau khi được mã hóa: " + encryptedData);
            result = encryptedData;
        }
        catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch (BadPaddingException e) {
            e.printStackTrace();
        }
        catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return result;
    }

    String encryptDataByRSA (String data, Key pubKey) {
        String result = "";
        try {
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.ENCRYPT_MODE, pubKey);
            byte encryptOutData[] = c.doFinal(data.getBytes());
            String encryptedData = Base64.getEncoder().encodeToString(encryptOutData);
            System.out.println("Chuỗi sau khi mã hoá lần 2: " + "\n" + encryptedData);
            result = encryptedData;
        }
        catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch (BadPaddingException e) {
            e.printStackTrace();
        }
        catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return result;
    }

    String decryptDataByAES (String data, Key secKey) {
        String result = "";
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secKey);
            byte[] byteDecrypted = cipher.doFinal(Base64.getDecoder().decode(data));
            String decryptedData = new String(byteDecrypted);
            System.out.println("Dữ liệu sau khi giải mã là: " + decryptedData);
            result = decryptedData;
        }
        catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        catch (BadPaddingException e) {
            e.printStackTrace();
        }
        catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return result;
    }

    void initUI()	{

        _panel_gantt = new JPanel();

        _scrollpane = new JScrollPane(_panel_gantt);
        _scrollpane.setBounds(20, 20, 540, 100);
        _font_job = new Font("Times New Roman", Font.PLAIN, 16);

        _lbl_algorithm = new JLabel("Algorithm: ");
        _lbl_algorithm.setBounds(20, 130, 100, 30);

        msg_text = new JTextField("");
        msg_text.setBounds(110, 135, 100, 20);

        nameFile = new JTextField("");
        nameFile.setBounds(130, 200, 100, 40);
        nameFile.setFont(new Font("Times New Roman", Font.BOLD, 20));
        nameFile.setEditable(false);

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
        this.add(nameFile);

        this.add(_btn_open);
        this.add(_btn_submit);


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

            while (!line.equals("bye")) {
                JTextArea gantt = new JTextArea(5, 20);
                _scrollpane = new JScrollPane(gantt);
                gantt.setEditable(false);
                gantt.setFont(_font_job);
                gantt.setBackground(getBackground());
                line = in.readLine();
                //Giải mã dữ liệu bằng AES
                result = decryptDataByAES(line, skeySpec);

                if (result.equals("bye")) return;
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
        String resultMessage = "";
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
                }
                String msgout = "";
                msgout = tmp;
                out.write(msgout);
                out.newLine();
                out.flush();
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
                String msgout = "";
                msgout = msg_text.getText();
                String msgouttmp = msgout;

                //Mã hóa lấn 1 bởi secretKey AES
                SecretKeySpec skeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
                encryptedMessage = encryptDataByAES(msgouttmp, skeySpec);

                //Mã hóa lần 2 bởi publicKey RSA
                PublicKey pubKey = publicKey(PUBLIC_KEY);
                encryptedMessage = encryptDataByRSA(encryptedMessage, pubKey);

                out.write(encryptedMessage);
                out.newLine();
                out.flush();

                if (msgout.equals("RR")) {
                    while (true) {
                        try {
                            String temp = JOptionPane.showInputDialog("Quantum time: ");
                            if (temp.matches("-?\\d+(\\.\\d+)?")) {
                                if (Double.parseDouble(temp) > 0) {
                                    //Mã hóa lấn 1 bởi secretKey AES
                                    skeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
                                    encryptedMessage = encryptDataByAES(temp, skeySpec);

                                    //Mã hóa lần 2 bởi publicKey RSA
                                    pubKey = publicKey(PUBLIC_KEY);
                                    encryptedMessage = encryptDataByRSA(encryptedMessage, pubKey);
                                    out.write(encryptedMessage);
                                    out.newLine();
                                    out.flush();
                                    break;
                                }
                                else {
                                    JOptionPane.showMessageDialog(_panel_gantt, "Dữ liệu truyền vào không đúng. Nhập lại"
                                            , "Alert", JOptionPane.WARNING_MESSAGE);
                                }
                            }
                            else {
                                JOptionPane.showMessageDialog(_panel_gantt, "Dữ liệu truyền vào không đúng. Nhập lại"
                                        , "Alert", JOptionPane.WARNING_MESSAGE);
                            }
                        } catch (NumberFormatException nfe) {
                            JOptionPane.showMessageDialog(_panel_gantt, "Dữ liệu truyền vào không đúng. Nhập lại"
                                    , "Alert", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
                if (!msgout.equals("FCFS") && !msgout.equals("SJF") && !msgout.equals("Prio")
                        && !msgout.equals("PPrio") && !msgout.equals("RR") && !msgout.equals("bye")) {
                    JOptionPane.showMessageDialog(_panel_gantt, "Dữ liệu truyền vào không đúng. Nhập lại","Alert",JOptionPane.WARNING_MESSAGE);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            msg_text.setText("");
        }
    }
}