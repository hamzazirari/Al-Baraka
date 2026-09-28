package dao;

import config.DatabaseConnection;
import entity.Client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {

    public void ajouter(Client client) {
        String sql = "INSERT INTO client (nom, email) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, client.nom());
            stmt.setString(2, client.email());
            stmt.executeUpdate();

            System.out.println("Client ajouté avec succès !");

        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout du client : " + e.getMessage());
        }
    }

    public Client trouverParId(int id) {
        String sql = "SELECT * FROM client WHERE id = ?";
        Client client = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                client = new Client(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("email")
                );
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la recherche du client : " + e.getMessage());
        }

        return client;
    }

    public List<Client> listerTout() {
        String sql = "SELECT * FROM client";
        List<Client> clients = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Client client = new Client(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("email")
                );
                clients.add(client);
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des clients : " + e.getMessage());
        }

        return clients;
    }

    public void modifier(Client client) {
        String sql = "UPDATE client SET nom = ?, email = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, client.nom());
            stmt.setString(2, client.email());
            stmt.setInt(3, client.id());
            stmt.executeUpdate();

            System.out.println("Client modifié avec succès !");

        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification du client : " + e.getMessage());
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM client WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

            System.out.println("Client supprimé avec succès !");

        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression du client : " + e.getMessage());
        }
    }
}