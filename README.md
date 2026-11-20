# Atlantic Palace Pressing API 👔🫧

API REST moderne, robuste et performante pour la gestion intégrale de pressings & blanchisseries écologiques (conforme au Cahier des Charges Fonctionnel Merise v3.0).

---

## 🚀 Stack Technique
- **Framework** : Spring Boot 3.4.3
- **Langage** : Java 21 (LTS)
- **Base de Données** : MySQL 8.x / MariaDB (Aiven Cloud & XAMPP local)
- **Sécurité** : Spring Security 6 + JWT Stateless (Auth & RBAC)
- **Documentation & Métriques** : Actuator `/actuator/health`
- **Déploiement Cloud** : Docker multi-stage optimisé pour Render Free Tier (512 Mo RAM)

---

## ⚙️ Variables d'Environnement

Le backend bascule automatiquement en mode Cloud si ces variables sont configurées, sinon il utilise les valeurs locales XAMPP par défaut :

| Variable | Description | Exemple (Aiven / Cloud) | Local (Défaut) |
|---|---|---|---|
| `SPRING_DATASOURCE_URL` | URL JDBC MySQL | `jdbc:mysql://mysql-xxx.aivencloud.com:21345/pressing_db?sslMode=REQUIRED` | `jdbc:mysql://localhost:3306/pressing_db` |
| `SPRING_DATASOURCE_USERNAME` | Utilisateur MySQL | `avnadmin` | `root` |
| `SPRING_DATASOURCE_PASSWORD` | Mot de passe MySQL | `votre_secret_aiven` | *(vide)* |
| `CORS_ALLOWED_ORIGINS` | Origines autorisées (Frontend) | `https://votre-frontend.vercel.app` | `http://localhost:3000` |
| `JWT_SECRET` | Clé secrète HMAC (min 256 bits) | `atlantic-palace-ultra-secure-key-2026...` | `atlantic-palace-secret-key-super-secure-token-2026` |
| `PORT` | Port dynamique de l'application | Auto-défini par Render | `8080` |

---

## 🐳 Déploiement Docker (Render)

Le projet contient un `Dockerfile` multi-stage prêt à l'emploi :
- **Build Stage** : Maven 3.9 + Eclipse Temurin 21 Alpine
- **Run Stage** : Eclipse Temurin 21 JRE Alpine ultra-léger
- **Optimisation RAM** : `-XX:+UseSerialGC -Xss512k -XX:MaxRAMPercentage=75`

---

## 🛠️ Démarrage Local

```bash
# Compilation et packaging
./mvnw clean package -DskipTests

# Lancement
java -jar target/atlantic-palace-pressing-api-3.0.0-SNAPSHOT.jar
```
