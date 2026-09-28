package dao;

import config.DatabaseConnection;
import entity.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CompteDAO {

    public void ajouter(Compte compte) {
        String sql = "INSERT INTO compte (numero, solde, id_client, type_compte, decouvert_autorise, taux_interet) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, compte.getNumero());
            stmt.setDouble(2, compte.getSolde());
            stmt.setInt(3, compte.getIdClient());

            if (compte instanceof CompteCourant courant) {
                stmt.setString(4, "COURANT");
                stmt.setDouble(5, courant.getDecouvertAutorise());
                stmt.setNull(6, java.sql.Types.DECIMAL);
            } else if (compte instanceof CompteEpargne epargne) {
                stmt.setString(4, "EPARGNE");
                stmt.setNull(5, java.sql.Types.DECIMAL);
                stmt.setDouble(6, epargne.getTauxInteret());
            }

            stmt.executeUpdate();
            System.out.println("Compte ajouté avec succès !");

        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout du compte : " + e.getMessage());
        }
    }

    public Compte trouverParId(int id) {
        String sql = "SELECT * FROM compte WHERE id = ?";
        Compte compte = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                compte = construireCompte(rs);
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la recherche du compte : " + e.getMessage());
        }

        return compte;
    }

    public List<Compte> listerTout() {
        String sql = "SELECT * FROM compte";
        List<Compte> comptes = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                comptes.add(construireCompte(rs));
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des comptes : " + e.getMessage());
        }

        return comptes;
    }

    public List<Compte> trouverParClient(int idClient) {
        String sql = "SELECT * FROM compte WHERE id_client = ?";
        List<Compte> comptes = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idClient);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                comptes.add(construireCompte(rs));
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des comptes du client : " + e.getMessage());
        }

        return comptes;
    }

    public void mettreAJourSolde(int id, double nouveauSolde) {
        String sql = "UPDATE compte SET solde = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, nouveauSolde);
            stmt.setInt(2, id);
            stmt.executeUpdate();

            System.out.println("Solde mis à jour avec succès !");

        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour du solde : " + e.getMessage());
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM compte WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

            System.out.println("Compte supprimé avec succès !");

        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression du compte : " + e.getMessage());
        }
    }

    private Compte construireCompte(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String numero = rs.getString("numero");
        double solde = rs.getDouble("solde");
        int idClient = rs.getInt("id_client");
        String type = rs.getString("type_compte");

        if ("COURANT".equals(type)) {
            double decouvert = rs.getDouble("decouvert_autorise");
            return new CompteCourant(id, numero, solde, idClient, decouvert);
        } else {
            double taux = rs.getDouble("taux_interet");
            return new CompteEpargne(id, numero, solde, idClient, taux);
        }
    }
}