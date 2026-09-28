package service;

import dao.*;
import entity.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TransactionService {

    private final TransactionDAO transactionDAO = new TransactionDAO();

    public void ajouterTransaction(Transaction transaction) {
        transactionDAO.ajouter(transaction);
    }

    // Lister les transactions d'un compte, triées par date
    public List<Transaction> listerParCompteTrieParDate(int idCompte) {
        return transactionDAO.trouverParCompte(idCompte).stream()
                .sorted(Comparator.comparing(Transaction::date))
                .toList();
    }

    // Filtrer par montant minimum
    public List<Transaction> filtrerParMontantMin(double montantMin) {
        return transactionDAO.listerTout().stream()
                .filter(t -> t.montant() >= montantMin)
                .toList();
    }

    // Filtrer par type
    public List<Transaction> filtrerParType(TypeTransaction type) {
        return transactionDAO.listerTout().stream()
                .filter(t -> t.type() == type)
                .toList();
    }

    // Filtrer par lieu
    public List<Transaction> filtrerParLieu(String lieu) {
        return transactionDAO.listerTout().stream()
                .filter(t -> t.lieu() != null && t.lieu().equalsIgnoreCase(lieu))
                .toList();
    }

    // Filtrer par période (entre deux dates)
    public List<Transaction> filtrerParPeriode(LocalDateTime debut, LocalDateTime fin) {
        return transactionDAO.listerTout().stream()
                .filter(t -> !t.date().isBefore(debut) && !t.date().isAfter(fin))
                .toList();
    }

    // Regrouper les transactions par type
    public Map<TypeTransaction, List<Transaction>> regrouperParType() {
        return transactionDAO.listerTout().stream()
                .collect(Collectors.groupingBy(Transaction::type));
    }

    // Regrouper les transactions par mois (période)
    public Map<String, List<Transaction>> regrouperParMois() {
        return transactionDAO.listerTout().stream()
                .collect(Collectors.groupingBy(t ->
                        t.date().getYear() + "-" + t.date().getMonthValue()));
    }

    // Calculer le total des transactions d'un compte
    public double calculerTotalParCompte(int idCompte) {
        return transactionDAO.trouverParCompte(idCompte).stream()
                .mapToDouble(Transaction::montant)
                .sum();
    }

    // Calculer la moyenne des transactions d'un compte
    public double calculerMoyenneParCompte(int idCompte) {
        return transactionDAO.trouverParCompte(idCompte).stream()
                .mapToDouble(Transaction::montant)
                .average()
                .orElse(0.0);
    }

    // Détecter les transactions suspectes simples (montant élevé)
    public List<Transaction> detecterMontantEleve(double seuil) {
        return transactionDAO.listerTout().stream()
                .filter(t -> t.montant() > seuil)
                .toList();
    }

    public List<Transaction> listerTout() {
        return transactionDAO.listerTout();
    }
}