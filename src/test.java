import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class test {
    public static String line = "";
    public static void main(String[] args) throws IOException {
        String[] tmpArr;
        String tmp = "";
        File file = new File("D:\\HOCTAP\\LTM_Project\\cpu_scheduling\\src\\dataClient\\input.txt");
        BufferedReader readfile = new BufferedReader(new FileReader(file));
        while ((line = readfile.readLine()) != null) {
            line += "\n" + readfile.readLine();
        }
        System.out.println("Ket qua: " + line);
    }
}
