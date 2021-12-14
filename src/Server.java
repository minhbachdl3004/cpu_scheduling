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

    String decryptDataByRSA(String data, Key priKey) {
        String result = "";
        try {
            //Giải mã bằng RSA
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.DECRYPT_MODE, priKey);
            byte decryptOutData[] = c.doFinal(Base64.getDecoder().decode(data));
            System.out.println("Dữ liệu sau khi giải mã bằng RSA: " + new String(decryptOutData));
            result = new String(decryptOutData);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException i) {
            i.printStackTrace();
        }
        catch (IllegalBlockSizeException i) {
            i.printStackTrace();
        }
        catch (BadPaddingException i) {
            i.printStackTrace();
        }
        catch (InvalidKeyException i) {
            i.printStackTrace();
        }
        return result;
    }

    String decryptDataByAES(String data, Key secKey) {
        String result = "";
        try {
            //Giải mã bằng AES
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secKey);
            byte[] byteDecrypted = cipher.doFinal(Base64.getDecoder().decode(data));
            String decrypted = new String(byteDecrypted);
            System.out.println("Dữ liệu sau khi giải mã bằng AES: " + decrypted);
            result = decrypted;
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException i) {
            i.printStackTrace();
        }
        catch (IllegalBlockSizeException i) {
            i.printStackTrace();
        }
        catch (BadPaddingException i) {
            i.printStackTrace();
        }
        catch (InvalidKeyException i) {
            i.printStackTrace();
        }
        return result;
    }

    String encryptDataByAES (String data, Key secKey) {
        String result = "";
        try {
            //Mã hóa dữ liệu bằng AES
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, secKey);
            byte[] byteEncryptedNew = cipher.doFinal(data.getBytes());
            String encryptedDataNew = Base64.getEncoder().encodeToString(byteEncryptedNew);
            System.out.println("Dữ liệu sau khi được mã hóa: " + encryptedDataNew);
            result = encryptedDataNew;
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException i) {
            i.printStackTrace();
        }
        catch (IllegalBlockSizeException i) {
            i.printStackTrace();
        }
        catch (BadPaddingException i) {
            i.printStackTrace();
        }
        catch (InvalidKeyException i) {
            i.printStackTrace();
        }
        return result;
    }

    String drawGanttChart(GanttChart _gantt_chart) {
        String tempGantt = "";
        System.out.print("0.0");
        String result = "0.0 --> ";
        String tmpGantt = "";
        for(int i = 0; i < _gantt_chart.getJobList().size(); i++) {
            System.out.print(" -----> " + _gantt_chart.getJobList().get(i) + " <----- " + _gantt_chart.getTimeList().get(i));
            tempGantt = " -----> " + _gantt_chart.getJobList().get(i) + " <----- " + _gantt_chart.getTimeList().get(i);
            tmpGantt = _gantt_chart.getJobList().get(i) + " <-- " + _gantt_chart.getTimeList().get(i) + ";" + _gantt_chart.getTimeList().get(i) + " --> ";
            result += tmpGantt;
        }
        System.out.println("\n\n");
        System.out.println(result);
        System.out.println("\n\n");
        return result;
    }

    public void actionPerformed() {
        _jobs.removeAll(_jobs);
        try {
            int index = 0;

            File file = new File("D:\\hoctap_minhbach\\cpu-scheduling-algorithms\\src\\input.txt");
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
                }
                catch (Exception ex) {
                }
                try {
                        _jobs.get(index).setBurstTime(Double.parseDouble(burst));

                }
                catch (Exception ex) {
                }
                try {
                        _jobs.get(index).setPriority(Double.parseDouble(priority));
                }
                catch (Exception ex) {
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

//            line = in.readLine();
//            String path = line;
//            System.out.println(line);
            while (!line.equals("bye")) {
                if (line.equals("bye")) return;
                else {
                    try {
                        line = in.readLine();
                        System.out.println("Server received: " + line);

                        //Giải mã lần 1 bởi RSA
                        decryptData = decryptDataByRSA(line, priKey);

                        //Giải mã lần 2 bởi AES
                        decryptData = decryptDataByAES(decryptData, skeySpec);

                        System.out.println("Sau khi giải mã 2 lần: " +decryptData);

                        line = decryptData;
                        actionPerformed();
                        itemStateChanged(line);
                        System.out.println("==================================");

                        if (line.equals("RR")) {
                            _quantum = in.readLine();
                            //Giải mã lần 1 bởi RSA
                            decryptData = decryptDataByRSA(_quantum, priKey);

                            //Giải mã lần 2 bởi AES
                            decryptData = decryptDataByAES(decryptData, skeySpec);

                            quantum = Double.parseDouble(decryptData);
                            CPU_Scheduling _solver_RR = new CPU_Scheduling(_jobs, _algorithm, quantum);
                            if (_solver_RR.solve()) {
                                String result = "";
                                drawGanttChart(_solver_RR.getGanttChart());
                                result = drawGanttChart(_solver_RR.getGanttChart());

                                //Mã hóa dữ liệu mới bằng AES
                                result = encryptDataByAES(result, skeySpec);
                                System.out.println("Dữ liệu đã dc mã hóa là: " + result);
                                out.write(result);
                                out.newLine();
                                out.flush();
                            }
                        }
                        if (line.equals("FCFS") || line.equals("SJF") || line.equals("Prio") || line.equals("PPrio")){
                            CPU_Scheduling _solver = new CPU_Scheduling(_jobs, _algorithm);
                            if (_solver.solve()) {
                                String result = "";
                                drawGanttChart(_solver.getGanttChart());
                                result = drawGanttChart(_solver.getGanttChart());

                                //Mã hóa dữ liệu mới bằng AES
                                result = encryptDataByAES(result, skeySpec);
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