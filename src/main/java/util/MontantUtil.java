package util;

public class MontantUtil {

    private MontantUtil() {
    }

    public static String formater(double montant) {
        return String.format("%.2f DH", montant);
    }
}