package dao;

import config.DatabaseConnection;
import entity.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    // CREATE - Ajouter une transaction
    public void ajouter(Transaction transaction) {
        String sql = "INSERT INTO transaction (date_transaction, montant, type, lieu, id_compte) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(transaction.date()));
            stmt.setDouble(2, transaction.montant());
            stmt.setString(3, transaction.type().name());
            stmt.setString(4, transaction.lieu());
            stmt.setInt(5, transaction.idCompte());
            stmt.executeUpdate();

            System.out.println("Transaction ajoutée avec succès !");

        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout de la transaction : " + e.getMessage());
        }
    }

    // READ - Trouver une transaction par id
    public Transaction trouverParId(int id) {
        String sql = "SELECT * FROM transaction WHERE id = ?";
        Transaction transaction = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                transaction = construireTransaction(rs);
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la recherche de la transaction : " + e.getMessage());
        }

        return transaction;
    }

    // READ - Lister toutes les transactions (recherche globale)
    public List<Transaction> listerTout() {
        String sql = "SELECT * FROM transaction";
        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                transactions.add(construireTransaction(rs));
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des transactions : " + e.getMessage());
        }

        return transactions;
    }

    // READ - Rechercher les transactions d'un compte
    public List<Transaction> trouverParCompte(int idCompte) {
        String sql = "SELECT * FROM transaction WHERE id_compte = ?";
        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCompte);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                transactions.add(construireTransaction(rs));
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des transactions du compte : " + e.getMessage());
        }

        return transactions;
    }

    // UPDATE - Modifier une transaction
    public void modifier(Transaction transaction) {
        String sql = "UPDATE transaction SET date_transaction = ?, montant = ?, type = ?, lieu = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(transaction.date()));
            stmt.setDouble(2, transaction.montant());
            stmt.setString(3, transaction.type().name());
            stmt.setString(4, transaction.lieu());
            stmt.setInt(5, transaction.id());
            stmt.executeUpdate();

            System.out.println("Transaction modifiée avec succès !");

        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification de la transaction : " + e.getMessage());
        }
    }

    // DELETE - Supprimer une transaction
    public void supprimer(int id) {
        String sql = "DELETE FROM transaction WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

            System.out.println("Transaction supprimée avec succès !");

        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression de la transaction : " + e.getMessage());
        }
    }

    // Méthode utilitaire privée : transforme une ligne SQL en objet Transaction
    private Transaction construireTransaction(ResultSet rs) throws SQLException {
        return new Transaction(
                rs.getInt("id"),
                rs.getTimestamp("date_transaction").toLocalDateTime(),
                rs.getDouble("montant"),
                TypeTransaction.valueOf(rs.getString("type")),
                rs.getString("lieu"),
                rs.getInt("id_compte")
        );
    }
}