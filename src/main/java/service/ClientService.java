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

    public Optional<Client> rechercherParId(int id) {
        Client client = clientDAO.trouverParId(id);
        return Optional.ofNullable(client);
    }

    public List<Client> rechercherParNom(String nom) {
        return clientDAO.listerTout().stream()
                .filter(c -> c.nom().toLowerCase().contains(nom.toLowerCase()))
                .toList();
    }

    public double getSoldeTotal(int idClient) {
        return compteDAO.trouverParClient(idClient).stream()
                .mapToDouble(Compte::getSolde)
                .sum();
    }

    public long getNombreComptes(int idClient) {
        return compteDAO.trouverParClient(idClient).size();
    }
}