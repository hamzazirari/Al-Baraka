package util;

public class ValidationUtil {

    private ValidationUtil() {
    }

    public static boolean estEmailValide(String email) {
        return email != null && email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    public static boolean estMontantValide(double montant) {
        return montant > 0;
    }

    public static boolean estTexteVide(String texte) {
        return texte == null || texte.trim().isEmpty();
    }
}