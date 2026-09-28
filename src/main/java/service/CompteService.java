package service;

import dao.*;
import entity.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class CompteService {

    private final CompteDAO compteDAO = new CompteDAO();

    // Créer un compte courant
    public void creerCompteCourant(String numero, double solde, int idClient, double decouvertAutorise) {
        Compte compte = new CompteCourant(0, numero, solde, idClient, decouvertAutorise);
        compteDAO.ajouter(compte);
    }

    // Créer un compte épargne
    public void creerCompteEpargne(String numero, double solde, int idClient, double tauxInteret) {
        Compte compte = new CompteEpargne(0, numero, solde, idClient, tauxInteret);
        compteDAO.ajouter(compte);
    }

    // Mettre à jour le solde
    public void mettreAJourSolde(int idCompte, double nouveauSolde) {
        compteDAO.mettreAJourSolde(idCompte, nouveauSolde);
    }

    // Rechercher les comptes d'un client
    public List<Compte> rechercherParClient(int idClient) {
        return compteDAO.trouverParClient(idClient);
    }

    // Rechercher un compte par numéro (avec Stream + Optional)
    public Optional<Compte> rechercherParNumero(String numero) {
        return compteDAO.listerTout().stream()
                .filter(c -> c.getNumero().equalsIgnoreCase(numero))
                .findFirst();
    }

    // Trouver le compte avec le solde maximum
    public Optional<Compte> trouverSoldeMax() {
        return compteDAO.listerTout().stream()
                .max(Comparator.comparingDouble(Compte::getSolde));
    }

    // Trouver le compte avec le solde minimum
    public Optional<Compte> trouverSoldeMin() {
        return compteDAO.listerTout().stream()
                .min(Comparator.comparingDouble(Compte::getSolde));
    }

    public List<Compte> listerTousLesComptes() {
        return compteDAO.listerTout();
    }
}