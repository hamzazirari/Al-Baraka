package service;

import dao.*;
import entity.*;


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RapportService {

    private final ClientDAO clientDAO = new ClientDAO();
    private final CompteDAO compteDAO = new CompteDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    private static final double SEUIL_MONTANT_SUSPECT = 10000.0;
    private static final String PAYS_HABITUEL = "Maroc";
    private static final int NB_MAX_OPERATIONS_PAR_MINUTE = 3;

    public List<Client> top5ClientsParSolde() {
        return clientDAO.listerTout().stream()
                .sorted(Comparator.comparingDouble(this::getSoldeTotalClient).reversed())
                .limit(5)
                .toList();
    }

    private double getSoldeTotalClient(Client client) {
        return compteDAO.trouverParClient(client.id()).stream()
                .mapToDouble(Compte::getSolde)
                .sum();
    }

    public Map<TypeTransaction, Long> rapportNombreParType() {
        return transactionDAO.listerTout().stream()
                .collect(Collectors.groupingBy(Transaction::type, Collectors.counting()));
    }

    public double volumeTotalTransactions() {
        return transactionDAO.listerTout().stream()
                .mapToDouble(Transaction::montant)
                .sum();
    }

    // ---------- Détection des transactions suspectes ----------

    // 1  montant trop eleve
    public List<Transaction> detecterMontantEleve() {
        return transactionDAO.listerTout().stream()
                .filter(t -> t.montant() > SEUIL_MONTANT_SUSPECT)
                .toList();
    }

    //  2 lieu different du pays habituel
    public List<Transaction> detecterLieuInhabituel() {
        return transactionDAO.listerTout().stream()
                .filter(t -> t.lieu() != null && !t.lieu().equalsIgnoreCase(PAYS_HABITUEL))
                .toList();
    }

    // 3  trop operations en moins d'une minute sur le meme compte
    public List<Transaction> detecterFrequenceExcessive() {

        Map<Integer, List<Transaction>> parCompte = transactionDAO.listerTout().stream()
                .collect(Collectors.groupingBy(Transaction::idCompte));

        return parCompte.values().stream()
                .flatMap(liste -> trouverRafales(liste).stream())
                .toList();
    }

    // Une transaction est "en rafale" si plus de 3 transactions
    // (elle incluse) ont lieu à moins de 60 secondes d'elle
    private List<Transaction> trouverRafales(List<Transaction> transactionsDuCompte) {
        return transactionsDuCompte.stream()
                .filter(t -> transactionsDuCompte.stream()
                        .filter(autre -> Math.abs(ChronoUnit.SECONDS.between(t.date(), autre.date())) <= 60)
                        .count() > NB_MAX_OPERATIONS_PAR_MINUTE)
                .toList();
    }

    // Regroupe les 3 critères (sans doublons grâce à distinct())
    public List<Transaction> detecterTransactionsSuspectes() {
        List<Transaction> suspectes = new ArrayList<>();
        suspectes.addAll(detecterMontantEleve());
        suspectes.addAll(detecterLieuInhabituel());
        suspectes.addAll(detecterFrequenceExcessive());

        return suspectes.stream()
                .distinct()
                .sorted(Comparator.comparing(Transaction::date))
                .toList();
    }

    // Comptes inactifs : aucune transaction depuis X jours (ou aucune transaction du tout)
    public List<Compte> comptesInactifsDepuis(int nombreJours) {
        LocalDateTime limite = LocalDateTime.now().minusDays(nombreJours);

        return compteDAO.listerTout().stream()
                .filter(compte -> transactionDAO.trouverParCompte(compte.getId()).stream()
                        .allMatch(t -> t.date().isBefore(limite)))
                .toList();
    }
}