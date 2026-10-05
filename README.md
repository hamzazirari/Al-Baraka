# 🏥 Système de Télé-Expertise Médicale

Application web Java EE (Jakarta EE) permettant la coordination entre **infirmiers**, **médecins généralistes** et **médecins spécialistes** afin d'optimiser le parcours patient : de l'accueil à l'hôpital jusqu'à l'avis d'expert à distance.

---

## 📑 Table des matières

1. [Contexte](#-contexte)
2. [Fonctionnalités](#-fonctionnalités)
3. [Parcours patient](#-parcours-patient)
4. [Stack technique](#-stack-technique)
5. [Architecture](#-architecture)
6. [Modèle de données](#-modèle-de-données)
7. [Sécurité](#-sécurité)
8. [Installation et lancement](#-installation-et-lancement)
9. [Tests](#-tests)
10. [Comptes de démonstration](#-comptes-de-démonstration)
11. [Bonus](#-bonus)
12. [Auteur](#-auteur)

---

## 🎯 Contexte

Ce projet vise à faciliter la collaboration médicale à distance. Un médecin généraliste qui ne peut pas traiter seul un cas peut demander l'avis d'un spécialiste (cardiologue, pneumologue, dermatologue, neurologue, endocrinologue, etc.), réserver un créneau et suivre la réponse, tout en gardant la consultation ouverte jusqu'à réception de l'avis.

Deux modes d'échange sont prévus :

| Mode | Description |
|------|-------------|
| **Synchrone** | Échange en direct (visioconférence / téléphone), réponse immédiate |
| **Asynchrone** | Transmission du dossier via la plateforme, avis écrit sous 24-48 h |

---

## ✨ Fonctionnalités

### 🔐 Authentification
- Login / Logout simple (sessions)
- 3 rôles : **Infirmier**, **Généraliste**, **Spécialiste**

### 👩‍⚕️ Module Infirmier
- **US1 – Accueil du patient**
  - Recherche du patient (par numéro de sécurité sociale / nom)
  - *Patient existant* : affichage des informations, saisie des nouveaux signes vitaux, ajout à la file d'attente
  - *Nouveau patient* : nom, prénom, date de naissance, N° sécurité sociale, téléphone, adresse (optionnel), antécédents, allergies, traitements en cours, signes vitaux → création du dossier + ajout automatique à la file d'attente
  - Signes vitaux : tension artérielle, fréquence cardiaque, température, fréquence respiratoire, poids et taille
- **US2 – Liste des patients du jour**
  - Affichage : nom, prénom, heure d'arrivée, signes vitaux, N° sécurité sociale
  - Tri par heure d'arrivée (du plus ancien au plus récent)
  - 🔎 *Stream API* : filtre par date d'enregistrement

### 🩺 Module Médecin Généraliste
- **US1 – Créer une consultation** : sélection d'un patient, saisie du motif et des observations (examen clinique, analyse des symptômes). Coût fixe : **150 DH**
- **US3 – Demander une expertise**
  - Choix d'une spécialité
  - 🔎 *Stream API* : filtre des spécialistes disponibles par spécialité, tri par tarif
  - Consultation des créneaux disponibles (futurs uniquement) et sélection
  - Question au spécialiste + données et analyses jointes + niveau de priorité (`URGENTE` / `NORMALE` / `NON_URGENTE`)
- **US4 – Coût total**
  - Consultation (150 DH) + expertise (tarif du spécialiste) + actes techniques médicaux
  - 🔎 *Lambda* : calcul avec `map().sum()`
- **Prise en charge directe** : diagnostic, traitement, clôture de la consultation (`TERMINEE`)

### 🧑‍🔬 Module Médecin Spécialiste
- **US5 – Configurer son profil** : tarif, spécialité, durée moyenne de consultation (fixe : 30 min)
- **US6 – Voir ses créneaux** (base de 30 min) :

  | Créneau | Statut initial |
  |---------|----------------|
  | 09h00 - 09h30 | Disponible |
  | 09h30 - 10h00 | Disponible |
  | 10h00 - 10h30 | Disponible |
  | 10h30 - 11h00 | Indisponible |
  | 11h00 - 11h30 | Disponible |
  | 11h30 - 12h00 | Disponible |

  Mise à jour automatique : créneau réservé → indisponible ; créneau passé → archivé ; annulation → redevient disponible
- **US7 – Consulter les demandes d'expertise**
  - 🔎 *Stream API* : filtre par statut (`EN_ATTENTE`, `TERMINEE`) et par priorité
  - Détails du patient et de la question posée
- **US8 – Répondre à une expertise** : avis médical, recommandations, marquage « terminée »

### 🧪 Actes techniques médicaux
Radiographie · Échographie · IRM · Électrocardiogramme · Actes dermatologiques (laser) · Fond d'œil · Analyse de sang · Analyse d'urine

---

## 🔄 Parcours patient

```
Accueil (infirmier) ──► File d'attente ──► Consultation (généraliste)
                                                   │
                         ┌─────────────────────────┴─────────────────────────┐
                         ▼                                                   ▼
              Scénario A : prise en charge directe             Scénario B : télé-expertise
              Diagnostic + traitement                          « Demander avis spécialiste »
              Clôture → TERMINEE                               Statut → EN_ATTENTE_AVIS_SPECIALISTE
                                                                         │
                                                      Spécialité → liste des spécialistes → créneau
                                                                         │
                                                      Question + priorité → notification du spécialiste
                                                                         │
                                                      Réponse du spécialiste → consultation clôturée
```

**Statuts des consultations** : `EN_COURS` · `EN_ATTENTE_AVIS_SPECIALISTE` · `TERMINEE`
**Statuts des demandes d'expertise** : `EN_ATTENTE` · `TERMINEE`

---

## 🛠 Stack technique

| Couche | Technologie |
|--------|-------------|
| Langage | Java 17+ |
| Build | Maven |
| Plateforme | Jakarta EE |
| Serveur | Apache Tomcat (ou Jetty / GlassFish) |
| Présentation | Servlet, JSP, JSTL |
| Persistance | JPA / Hibernate |
| Base de données | MySQL / PostgreSQL |
| Sécurité | Sessions (stateful), BCrypt, protection CSRF |
| Tests | JUnit 5, Mockito |

---

## 🏗 Architecture

Architecture en couches (MVC) :

```
src/
├── main/
│   ├── java/com/teleexpertise/
│   │   ├── model/          # Entités JPA (User, Patient, Consultation, Expertise, Creneau…)
│   │   ├── enums/          # Role, StatutConsultation, StatutExpertise, Priorite, Specialite, ActeTechnique
│   │   ├── repository/     # Accès aux données (DAO / JPA)
│   │   ├── service/        # Logique métier (Stream API, calcul des coûts…)
│   │   ├── servlet/        # Contrôleurs (Auth, Infirmier, Généraliste, Spécialiste)
│   │   ├── filter/         # Filtres : authentification, autorisation par rôle, CSRF
│   │   └── util/           # JPAUtil, PasswordUtil (BCrypt), CsrfUtil
│   ├── resources/
│   │   └── META-INF/persistence.xml
│   └── webapp/
│       ├── WEB-INF/
│       │   ├── views/      # Pages JSP (par rôle)
│       │   └── web.xml
│       └── assets/         # CSS / JS
└── test/
    └── java/               # Tests unitaires JUnit / Mockito
```

> ⚠️ Adaptez le nom du package et l'arborescence à votre implémentation réelle.

---

## 🗄 Modèle de données

Entités principales :

- **Utilisateur** (id, nom, email, mot de passe haché, rôle)
  - **Spécialiste** (spécialité, tarif, disponibilité)
- **Patient** (identité, N° sécurité sociale, mutuelle, coordonnées, antécédents, allergies, traitements)
- **SignesVitaux** (tension, fréquence cardiaque, température, fréquence respiratoire, poids, taille, date)
- **FileAttente** (patient, heure d'arrivée)
- **Consultation** (patient, généraliste, motif, observations, diagnostic, traitement, coût, statut, date)
- **DemandeExpertise** (consultation, spécialiste, créneau, question, priorité, statut, avis, recommandations)
- **Creneau** (spécialiste, début, fin, statut)
- **ActeTechnique** (type, tarif) lié à la consultation

Relations clés : un patient a plusieurs consultations ; une consultation peut avoir une demande d'expertise ; un spécialiste a plusieurs créneaux ; un créneau n'est réservé que par une seule demande.

---

## 🔒 Sécurité

- **Authentification stateful** via `HttpSession`
- **Hachage des mots de passe** avec **BCrypt**
- **Protection CSRF** : jeton généré par session, vérifié sur chaque requête `POST`
- **Contrôle d'accès par rôle** via filtres Servlet (un infirmier ne peut pas accéder aux pages d'un spécialiste, etc.)
- Requêtes JPA paramétrées (protection contre l'injection SQL) et échappement des sorties JSTL (`<c:out>`)

---

## 🚀 Installation et lancement

### Prérequis
- JDK 17+
- Maven 3.8+
- Apache Tomcat 10+ (Jakarta EE)
- MySQL 8+ ou PostgreSQL

### Étapes

```bash
# 1. Cloner le dépôt
git clone <url-du-depot>
cd tele-expertise-medicale

# 2. Créer la base de données
mysql -u root -p -e "CREATE DATABASE tele_expertise CHARACTER SET utf8mb4;"

# 3. Configurer la connexion dans src/main/resources/META-INF/persistence.xml
#    (url, user, password)

# 4. Compiler et packager
mvn clean package

# 5. Déployer le WAR généré dans Tomcat
cp target/tele-expertise.war $TOMCAT_HOME/webapps/

# 6. Démarrer Tomcat puis ouvrir
http://localhost:8080/tele-expertise
```

---

## 🧪 Tests

```bash
mvn test
```

Les tests unitaires (JUnit 5 + Mockito) couvrent notamment :
- le calcul du coût total (consultation + expertise + actes techniques)
- le filtrage et le tri des spécialistes (Stream API)
- la gestion des statuts des créneaux (réservation, annulation, archivage)
- le service d'authentification et le hachage BCrypt

---

## 👤 Comptes de démonstration

| Rôle | Email | Mot de passe |
|------|-------|--------------|
| Infirmier | `infirmier@demo.ma` | `ChangeMe123!` |
| Généraliste | `generaliste@demo.ma` | `ChangeMe123!` |
| Spécialiste | `specialiste@demo.ma` | `ChangeMe123!` |

> ⚠️ Comptes d'exemple à remplacer par ceux de votre jeu de données initial. Ne jamais utiliser ces identifiants en production.

---

## ⭐ Bonus

Gestion du staff (au choix) :
- **Option 1** : insertion des utilisateurs via scripts SQL (`data.sql`)
- **Option 2** : rôle **Administrateur** avec interface de gestion du staff (CRUD infirmiers, généralistes, spécialistes)

---

## ✍️ Auteur

Projet réalisé dans le cadre du Sprint 2 – Brief 1.

**ZIRARI HAMZA**
