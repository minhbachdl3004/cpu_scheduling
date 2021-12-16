import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.net.*;
import java.io.*;

import Encryption.*;

public class Server {
    Double quantum;
    JFileChooser fileChooser;
    Scanner scanner;

    private Socket socket = null;
    private ServerSocket server = null;
    BufferedWriter out = null;
    BufferedReader in = null;


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

            // Khởi tạo cặp khóa
            KeyPair kp = kpg.genKeyPair();

            // PublicKey
            PublicKey publicKey = kp.getPublic();

            // PrivateKey
            PrivateKey privateKey = kp.getPrivate();

            //Tạp private key
            PKCS8EncodedKeySpec spec_en = new PKCS8EncodedKeySpec(privateKey.getEncoded());
            KeyFactory factory_en = KeyFactory.getInstance("RSA");
            PrivateKey priKey = factory_en.generatePrivate(spec_en);

            // Tạo public key
            X509EncodedKeySpec spec_de = new X509EncodedKeySpec(publicKey.getEncoded());
            KeyFactory factory_de = KeyFactory.getInstance("RSA");
            PublicKey pubKey = factory_de.generatePublic(spec_de);

            String pubKeyEncode = Base64.getEncoder().encodeToString(pubKey.getEncoded());

            //Server đưa public Key vào JSONObject
            JSONObject json = new JSONObject();
            json.put("publicKey", pubKeyEncode);
            String publicKeyTrans = json.toString();

            //Server gửi public Key cho Client
            out.write(publicKeyTrans);
            out.newLine();
            out.flush();

            //Server nhận secret key đã dc mã hóa bởi Client
            line = in.readLine();
            System.out.println("Server received: " + line);

            System.out.println(((Object)line).getClass().getSimpleName());

            JSONObject jsonObject = new JSONObject(line);
            String secretKeyEncrypt = jsonObject.get("secretKey").toString();
            System.out.println("SecretKey: " + secretKeyEncrypt);

            // Giải mã secret Key
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.DECRYPT_MODE, priKey);
            byte decryptOut[] = c.doFinal(Base64.getDecoder().decode(secretKeyEncrypt));
            System.out.println("Dữ liệu sau khi giải mã: " + new String(decryptOut));

            String decryptData = "";

            SecretKeySpec skeySpec = new SecretKeySpec(decryptOut, "AES");

            while (!line.equals("EXIT")) {
                if (line.equals("EXIT")) return;
                else {
                    try {
                        line = in.readLine();
                        System.out.println("Server received: " + line);

                        //Giải mã lần 1 bởi RSA
                        decryptData = Decryption.decryptDataByRSA(line, priKey);

                        //Giải mã lần 2 bởi AES
                        decryptData = Decryption.decryptDataByAES(decryptData, skeySpec);

                        System.out.println("Sau khi giải mã 2 lần: " +decryptData);

                        line = decryptData;
                        App.actionPerformed();
                        App.itemStateChanged(line);
                        System.out.println("==================================");

                        if (line.equals("RR")) {
                            _quantum = in.readLine();
                            //Giải mã lần 1 bởi RSA
                            decryptData = Decryption.decryptDataByRSA(_quantum, priKey);

                            //Giải mã lần 2 bởi AES
                            decryptData = Decryption.decryptDataByAES(decryptData, skeySpec);

                            quantum = Double.parseDouble(decryptData);
                            CPU_Scheduling _solver_RR = new CPU_Scheduling(App._jobs, App._algorithm, quantum);
                            if (_solver_RR.solve()) {
                                String result = "";
                                App.drawGanttChart(_solver_RR.getGanttChart());
                                result = App.drawGanttChart(_solver_RR.getGanttChart());

                                //Mã hóa dữ liệu mới bằng AES
                                result = Encryption.encryptDataByAES(result, skeySpec);
                                System.out.println("Dữ liệu đã dc mã hóa là: " + result);
                                out.write(result);
                                out.newLine();
                                out.flush();
                            }
                        }
                        if (line.equals("FCFS") || line.equals("SJF") || line.equals("Prio") || line.equals("PPrio")){
                            CPU_Scheduling _solver = new CPU_Scheduling(App._jobs, App._algorithm);
                            if (_solver.solve()) {
                                String result = "";
                                App.drawGanttChart(_solver.getGanttChart());
                                result = App.drawGanttChart(_solver.getGanttChart());

                                //Mã hóa dữ liệu mới bằng AES
                                result = Encryption.encryptDataByAES(result, skeySpec);
                                System.out.println("Dữ liệu đã dc mã hóa là: " + result);
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
        }
        catch (IOException | JSONException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException i) {
            System.out.println(i);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(5000);
    }
}