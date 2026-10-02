package ui;

import entity.*;
import service.*;
import util.*;


import java.util.List;
import java.util.Scanner;
import java.util.Optional;

public class MenuPrincipal {

    private final Scanner scanner = new Scanner(System.in);
    private final ClientService clientService = new ClientService();
    private final CompteService compteService = new CompteService();
    private final TransactionService transactionService = new TransactionService();
    private final RapportService rapportService = new RapportService();

    public void demarrer() {
        boolean continuer = true;

        while (continuer) {
            afficherMenu();
            int choix = lireEntier("Votre choix : ");

            switch (choix) {
                case 1 -> creerClientEtCompte();
                case 2 -> enregistrerTransaction();
                case 3 -> consulterHistorique();
                case 4 -> lancerAnalyses();
                case 5 -> voirAlertes();
                case 6 -> supprimerClient();
                case 0 -> continuer = false;
                default -> System.out.println("Choix invalide, réessayez.");
            }
        }

        System.out.println("Au revoir !");
    }

    private void afficherMenu() {
        System.out.println("""
                
                ===== MENU PRINCIPAL - AL BARAKA =====
                1. Créer un client et un compte
                2. Enregistrer une transaction
                3. Consulter l'historique d'un compte
                4. Lancer une analyse
                5. Voir les alertes
                6. Supprimer un client
                0. Quitter
                =======================================
                """);
    }

    // ---------- 1. Créer client + compte ----------
    private void creerClientEtCompte() {
        System.out.print("Nom du client : ");
        String nom = scanner.nextLine();

        System.out.print("Email du client : ");
        String email = scanner.nextLine();

        if (ValidationUtil.estTexteVide(nom) || !ValidationUtil.estEmailValide(email)) {
            System.out.println("Nom ou email invalide.");
            return;
        }

        Client client = new Client(0, nom, email);
        clientService.ajouterClient(client);

        // Récupérer l'id du client fraîchement créé
        List<Client> clients = clientService.listerTousLesClients();
        int idClient = clients.get(clients.size() - 1).id();

        System.out.print("Numéro de compte : ");
        String numero = scanner.nextLine();

        double solde = lireDouble("Solde initial : ");

        System.out.print("Type de compte (1=Courant, 2=Epargne) : ");
        int type = lireEntier("");

        if (type == 1) {
            double decouvert = lireDouble("Découvert autorisé : ");
            compteService.creerCompteCourant(numero, solde, idClient, decouvert);
        } else if (type == 2) {
            double taux = lireDouble("Taux d'intérêt : ");
            compteService.creerCompteEpargne(numero, solde, idClient, taux);
        } else {
            System.out.println("Type de compte invalide.");
        }
    }

    // ---------- 2. Enregistrer une transaction ----------
    private void enregistrerTransaction() {
        int idCompte = lireEntier("ID du compte : ");

        // On vérifie d'abord que le compte existe
        Optional<Compte> compteTrouve = compteService.listerTousLesComptes().stream()
                .filter(c -> c.getId() == idCompte)
                .findFirst();

        if (compteTrouve.isEmpty()) {
            System.out.println("Compte introuvable.");
            return;
        }
        Compte compte = compteTrouve.get();

        double montant = lireDouble("Montant : ");
        if (!ValidationUtil.estMontantValide(montant)) {
            System.out.println("Montant invalide.");
            return;
        }

        System.out.print("Type (VERSEMENT, RETRAIT, VIREMENT) : ");
        String typeStr = scanner.nextLine().toUpperCase();

        TypeTransaction type;
        try {
            type = TypeTransaction.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            System.out.println("Type invalide.");
            return;
        }

        System.out.print("Lieu : ");
        String lieu = scanner.nextLine();

        // Enregistrer la transaction
        Transaction transaction = new Transaction(0, DateUtil.maintenant(), montant, type, lieu, idCompte);
        transactionService.ajouterTransaction(transaction);

        // Mettre à jour le solde
        double nouveauSolde = switch (type) {
            case VERSEMENT -> compte.getSolde() + montant;
            case RETRAIT, VIREMENT -> compte.getSolde() - montant;
        };
        compteService.mettreAJourSolde(idCompte, nouveauSolde);
    }

    // ---------- 3. Consulter historique ----------
    private void consulterHistorique() {
        int idCompte = lireEntier("ID du compte : ");
        List<Transaction> transactions = transactionService.listerParCompteTrieParDate(idCompte);

        if (transactions.isEmpty()) {
            System.out.println("Aucune transaction trouvée.");
            return;
        }

        transactions.forEach(t -> System.out.println(
                DateUtil.formater(t.date()) + " | " + t.type() + " | " + MontantUtil.formater(t.montant()) + " | " + t.lieu()
        ));
    }

    // ---------- 4. Lancer une analyse ----------
    private void lancerAnalyses() {
        System.out.println("""
                
                --- ANALYSES DISPONIBLES ---
                1. Top 5 clients par solde
                2. Transactions par type
                3. Transactions par mois
                4. Comptes inactifs
                5. Transactions suspectes
                """);

        int choix = lireEntier("Votre choix : ");

        switch (choix) {
            case 1 -> rapportService.top5ClientsParSolde()
                    .forEach(c -> System.out.println(c.nom() + " - " + c.email()));

            case 2 -> rapportService.rapportNombreParType()
                    .forEach((type, nombre) -> System.out.println(type + " : " + nombre));

            case 3 -> transactionService.regrouperParMois()
                    .forEach((mois, liste) -> System.out.println(mois + " : " + liste.size() + " transactions"));

            case 4 -> {
                int jours = lireEntier("Depuis combien de jours ? : ");
                rapportService.comptesInactifsDepuis(jours)
                        .forEach(c -> System.out.println(c.getNumero() + " - solde : " + MontantUtil.formater(c.getSolde())));
            }

            case 5 -> rapportService.detecterTransactionsSuspectes()
                    .forEach(t -> System.out.println(
                            DateUtil.formater(t.date()) + " | " + MontantUtil.formater(t.montant()) + " | " + t.lieu()
                    ));

            default -> System.out.println("Choix invalide.");
        }
    }

    // ---------- 5. Alertes ----------
    private void voirAlertes() {
        double seuilSoldeBas = 500.0;

        System.out.println("--- Comptes avec solde bas (< " + seuilSoldeBas + ") ---");
        compteService.listerTousLesComptes().stream()
                .filter(c -> c.getSolde() < seuilSoldeBas)
                .forEach(c -> System.out.println(c.getNumero() + " - " + MontantUtil.formater(c.getSolde())));

        System.out.println("\n--- Comptes inactifs depuis plus de 30 jours ---");
        rapportService.comptesInactifsDepuis(30)
                .forEach(c -> System.out.println(c.getNumero()));
    }

    // ---------- 6. Supprimer un client ----------
    private void supprimerClient() {
        int idClient = lireEntier("ID du client à supprimer : ");

        if (clientService.rechercherParId(idClient).isEmpty()) {
            System.out.println("Client introuvable.");
            return;
        }

        System.out.print("Attention : tous les comptes et transactions de ce client seront aussi supprimés (cascade). Confirmer ? (oui/non) : ");
        String confirmation = scanner.nextLine();

        if (confirmation.equalsIgnoreCase("oui")) {
            clientService.supprimerClient(idClient);
            System.out.println("Client supprimé.");
        } else {
            System.out.println("Suppression annulée.");
        }
    }

    // ---------- Méthodes utilitaires de lecture ----------
    private int lireEntier(String message) {
        System.out.print(message);
        while (!scanner.hasNextInt()) {
            System.out.println("Veuillez entrer un nombre valide.");
            scanner.next();
        }
        int valeur = scanner.nextInt();
        scanner.nextLine(); // vider le buffer
        return valeur;
    }

    private double lireDouble(String message) {
        System.out.print(message);
        while (!scanner.hasNextDouble()) {
            System.out.println("Veuillez entrer un nombre valide.");
            scanner.next();
        }
        double valeur = scanner.nextDouble();
        scanner.nextLine(); // vider le buffer
        return valeur;
    }
}