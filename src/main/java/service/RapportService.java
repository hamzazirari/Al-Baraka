package service;

import dao.*;
import entity.*;


import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

    // Top 5 des clients par solde total (somme des soldes de leurs comptes)
    public List<Client> top5ClientsParSolde() {
        List<Client> clients = clientDAO.listerTout();

        return clients.stream()
                .sorted(Comparator.comparingDouble(this::getSoldeTotalClient).reversed())
                .limit(5)
                .toList();
    }

    private double getSoldeTotalClient(Client client) {
        return compteDAO.trouverParClient(client.id()).stream()
                .mapToDouble(Compte::getSolde)
                .sum();
    }

    // Rapport mensuel : nombre de transactions par type + volume total
    public Map<TypeTransaction, Long> rapportNombreParType() {
        return transactionDAO.listerTout().stream()
                .collect(Collectors.groupingBy(Transaction::type, Collectors.counting()));
    }

    public double volumeTotalTransactions() {
        return transactionDAO.listerTout().stream()
                .mapToDouble(Transaction::montant)
                .sum();
    }

    // Transactions suspectes : montant > seuil OU lieu différent du pays habituel
    public List<Transaction> detecterTransactionsSuspectes() {
        return transactionDAO.listerTout().stream()
                .filter(t -> t.montant() > SEUIL_MONTANT_SUSPECT
                        || (t.lieu() != null && !t.lieu().equalsIgnoreCase(PAYS_HABITUEL)))
                .toList();
    }

    // Trop d'opérations en moins d'une minute pour un même compte (simulation)
    public List<Transaction> detecterFrequenceExcessive(int idCompte) {
        List<Transaction> transactions = transactionDAO.trouverParCompte(idCompte).stream()
                .sorted(Comparator.comparing(Transaction::date))
                .toList();

        return transactions.stream()
                .filter(t -> transactions.stream()
                        .filter(autre -> Math.abs(ChronoUnit.SECONDS.between(t.date(), autre.date())) <= 60)
                        .count() > NB_MAX_OPERATIONS_PAR_MINUTE)
                .toList();
    }

    // Comptes inactifs depuis X jours (aucune transaction récente)
    public List<Compte> comptesInactifsDepuis(int nombreJours) {
        LocalDateTime limite = LocalDateTime.now().minusDays(nombreJours);

        return compteDAO.listerTout().stream()
                .filter(compte -> {
                    List<Transaction> transactions = transactionDAO.trouverParCompte(compte.getId());
                    return transactions.stream()
                            .allMatch(t -> t.date().isBefore(limite))
                            || transactions.isEmpty();
                })
                .toList();
    }
}