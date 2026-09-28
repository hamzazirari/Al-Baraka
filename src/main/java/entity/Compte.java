package entity;

public sealed abstract class Compte permits CompteCourant, CompteEpargne {

    protected int id;
    protected String numero;
    protected double solde;
    protected int idClient;

    public Compte(int id, String numero, double solde, int idClient) {
        this.id = id;
        this.numero = numero;
        this.solde = solde;
        this.idClient = idClient;
    }

    // Getters
    public int getId() { return id; }
    public String getNumero() { return numero; }
    public double getSolde() { return solde; }
    public int getIdClient() { return idClient; }

    // Setters utiles (le solde change souvent)
    public void setSolde(double solde) { this.solde = solde; }
    public void setId(int id) { this.id = id; }

    @Override
    public String toString() {
        return "Compte{id=%d, numero='%s', solde=%.2f, idClient=%d}"
                .formatted(id, numero, solde, idClient);
    }
}