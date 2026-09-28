package service;

import dao.*;
import entity.*;

import java.util.List;
import java.util.Optional;

public class ClientService {

    private final ClientDAO clientDAO = new ClientDAO();
    private final CompteDAO compteDAO = new CompteDAO();

    public void ajouterClient(Client client) {
        clientDAO.ajouter(client);
    }

    public void modifierClient(Client client) {
        clientDAO.modifier(client);
    }

    public void supprimerClient(int id) {
        clientDAO.supprimer(id);
    }

    public List<Client> listerTousLesClients() {
        return clientDAO.listerTout();
    }

    // Recherche par id -> Optional car le client peut ne pas exister
    public Optional<Client> rechercherParId(int id) {
        Client client = clientDAO.trouverParId(id);
        return Optional.ofNullable(client);
    }

    // Recherche par nom (contient, insensible à la casse) avec Stream
    public List<Client> rechercherParNom(String nom) {
        return clientDAO.listerTout().stream()
                .filter(c -> c.nom().toLowerCase().contains(nom.toLowerCase()))
                .toList();
    }

    // Solde total d'un client = somme des soldes de tous ses comptes
    public double getSoldeTotal(int idClient) {
        return compteDAO.trouverParClient(idClient).stream()
                .mapToDouble(Compte::getSolde)
                .sum();
    }

    // Nombre de comptes d'un client
    public long getNombreComptes(int idClient) {
        return compteDAO.trouverParClient(idClient).size();
    }
}