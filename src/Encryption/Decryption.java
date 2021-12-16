package Encryption;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Decryption {

    public static String decryptDataByRSA(String data, Key priKey) {
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

    public static String decryptDataByAES(String data, Key secKey) {
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

}
