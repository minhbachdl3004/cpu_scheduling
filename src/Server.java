import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.net.*;
import java.io.*;
import Algorithm.*;

import Encryption.*;

public class Server {
    Double quantum;
    JFileChooser fileChooser;
    Scanner scanner;

    private Socket socket = null;
    private ServerSocket server = null;
    BufferedWriter out = null;
    BufferedReader in = null;
    String check_txt = "\\w+\\.txt";
    String filePath;
    private BufferedWriter writer;
    String []temp;

    public Server(int port) throws IOException {
        try {
            server = new ServerSocket(port);
            System.out.println("Server started");
            System.out.println("Waiting for a client ...");
            socket = server.accept();
            System.out.println("Server accepted");
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = "";
            String _quantum = "";

            SecureRandom sr = new SecureRandom();
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(512, sr);

            // Key Pair Initialize
            KeyPair kp = kpg.genKeyPair();

            // PublicKey
            PublicKey publicKey = kp.getPublic();

            // PrivateKey
            PrivateKey privateKey = kp.getPrivate();

            //Generator private key
            PKCS8EncodedKeySpec spec_en = new PKCS8EncodedKeySpec(privateKey.getEncoded());
            KeyFactory factory_en = KeyFactory.getInstance("RSA");
            PrivateKey priKey = factory_en.generatePrivate(spec_en);

            //Generator public key
            X509EncodedKeySpec spec_de = new X509EncodedKeySpec(publicKey.getEncoded());
            KeyFactory factory_de = KeyFactory.getInstance("RSA");
            PublicKey pubKey = factory_de.generatePublic(spec_de);

            String pubKeyEncode = Base64.getEncoder().encodeToString(pubKey.getEncoded());

            //Server put public Key into JSONObject
            JSONObject json = new JSONObject();
            json.put("publicKey", pubKeyEncode);
            String publicKeyTrans = json.toString();

            //Server send public Key to Client
            out.write(publicKeyTrans);
            out.newLine();
            out.flush();

            //Server receive encrypted secret Key by Client
            line = in.readLine();
            System.out.println("Server received: " + line);

            System.out.println(((Object)line).getClass().getSimpleName());

            JSONObject jsonObject = new JSONObject(line);
            String secretKeyEncrypt = jsonObject.get("secretKey").toString();
            System.out.println("SecretKey: " + secretKeyEncrypt);

            //Server decrpyt secret Key by private Key from Client
            String decryptOut = Decryption.decryptDataByRSA(secretKeyEncrypt, priKey);

            SecretKeySpec skeySpec = new SecretKeySpec(decryptOut.getBytes(), "AES");

            String decryptData = "";

            while (!line.equals("EXIT")) {
                try {
                    boolean flag = true;
                    line = in.readLine();
                    //Decrypt first time by RSA
                    decryptData = Decryption.decryptDataByRSA(line, priKey);

                    //Decrypt second time by AES
                    decryptData = Decryption.decryptDataByAES(decryptData, skeySpec);
                    line = decryptData;
                    System.out.println(line);
                    if (line.equals("EXIT")) return;
                    if (line.matches(check_txt)) {
                        System.out.println(line);
                        File file = new File("./src/dataServer/S_" + line);
                        file.createNewFile();
                        filePath = file.getAbsolutePath();
                        writer = new BufferedWriter(new FileWriter(file, false));

                        line = in.readLine();
                        //Decrypt first time by RSA
                        decryptData = Decryption.decryptDataByRSA(line, priKey);

                        //Decrypt second time by AES
                        decryptData = Decryption.decryptDataByAES(decryptData, skeySpec);
                        line = decryptData;
                        while (line != null && flag) {
                            System.out.println("data nhận:" + line);
                            temp = line.split(":");
                            for (int i = 0; i < temp.length; i++) {
                                writer.write(temp[i]);
                                System.out.println(temp[i]);
                                writer.newLine();
                                writer.flush();
                            }
                            flag = false;
                        }
                    }
                    else {
                        try {
                            System.out.println("Server received: " + line);

                            System.out.println(filePath);
                            App.actionPerformed(filePath);
                            App.itemStateChanged(line);
                            System.out.println("==================================");

                            if (line.equals("RR")) {
                                _quantum = in.readLine();
                                //Decrypt first time by RSA
                                decryptData = Decryption.decryptDataByRSA(_quantum, priKey);

                                //Decrypt second time by AES
                                decryptData = Decryption.decryptDataByAES(decryptData, skeySpec);

                                quantum = Double.parseDouble(decryptData);
                                CPU_Scheduling _solver_RR = new CPU_Scheduling(App._jobs, App._algorithm, quantum);
                                if (_solver_RR.solve()) {
                                    String result = "";
                                    App.drawGanttChart(_solver_RR.getGanttChart());
                                    result = App.drawGanttChart(_solver_RR.getGanttChart());

                                    //Encrypt data by AES
                                    result = Encryption.encryptDataByAES(result, skeySpec);
                                    System.out.println("Dữ liệu đã dc mã hóa là: " + result);
                                    out.write(result);
                                    out.newLine();
                                    out.flush();
                                }
                            }
                            if (line.equals("FCFS") || line.equals("SJF") || line.equals("Prio") || line.equals("PPrio")) {
                                CPU_Scheduling _solver = new CPU_Scheduling(App._jobs, App._algorithm);
                                if (_solver.solve()) {
                                    String result = "";
                                    App.drawGanttChart(_solver.getGanttChart());
                                    result = App.drawGanttChart(_solver.getGanttChart());

                                    //Encrypt data by AES
                                    result = Encryption.encryptDataByAES(result, skeySpec);
                                    System.out.println("Dữ liệu đã dc mã hóa là: " + result);
                                    out.write(result);
                                    out.newLine();
                                    out.flush();
                                }
                            }
                        } catch (IOException i) {
                            System.out.println(i);
                        }
                    }
                }
                catch (Exception i) {
                    System.out.println(i);
                }
            }
        }
        catch (IOException | JSONException | NoSuchAlgorithmException | InvalidKeySpecException i) {
            System.out.println(i);
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(5000);
    }
}