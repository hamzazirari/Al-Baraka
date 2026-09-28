
import dao.ClientDAO;
import entity.Client;

public class Main {
    public static void main(String[] args) {
        ClientDAO clientDAO = new ClientDAO();

        // Test ajout
        Client nouveauClient = new Client(0, "Ahmed Benali", "ahmed.benali@email.com");
        clientDAO.ajouter(nouveauClient);

        // Test liste
        System.out.println("Liste des clients :");
        clientDAO.listerTout().forEach(System.out::println);
    }
}