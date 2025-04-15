package main.Admin;
class TeacherInfo {
    private String id;
    private String nom;
    private String prenom;
    private String specialite;
    private String email;

    public TeacherInfo(String id, String nom, String prenom, String specialite, String email) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.specialite = specialite;
        this.email = email;
    }

    // Getters
    public String getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getSpecialite() { return specialite; }
    public String getEmail() { return email; }
}