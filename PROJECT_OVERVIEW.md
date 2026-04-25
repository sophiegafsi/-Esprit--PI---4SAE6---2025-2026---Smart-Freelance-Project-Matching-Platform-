# Smart Freelance Project Matching Platform - Module Evaluation & Recompense

## Vision generale

Ce projet est une plateforme microservices pour gerer les evaluations des freelances et automatiser les recompenses associees. Le module principal couvert ici combine:

- les evaluations client/freelance;
- les reviews et l'analyse de sentiment;
- les badges attribues selon le score ou les points;
- les recompenses, certificats, notifications et historique;
- une interface Angular qui centralise le pilotage.

L'objectif fonctionnel est simple: apres une evaluation, le systeme calcule le score du freelance, synchronise le profil cote recompense, attribue les badges/recompenses eligibles, puis expose tout dans un dashboard.

## Architecture

Le projet est organise en microservices Spring Boot avec un frontend Angular.

| Module | Technologie | Port | Role |
| --- | --- | --- | --- |
| `eureka-server` | Spring Boot + Netflix Eureka | `8761` | Service discovery |
| `api-gateway` | Spring Cloud Gateway | `8088` | Point d'entree unique pour le frontend |
| `evaluation-service/evaluation-service` | Spring Boot + MySQL + Gemini | `8085` | Evaluations, reviews, sentiment |
| `recompense/recompense` | Spring Boot + MySQL + Mail + RabbitMQ | `8094` | Badges, recompenses, points, certificats |
| `frontend` | Angular 18 | `4200` | Interface utilisateur |

## Flux principal

1. Le client cree une evaluation depuis le frontend.
2. Le frontend appelle l'API Gateway sur `http://localhost:8088`.
3. La gateway redirige vers `evaluation-service`.
4. Le service evaluation enregistre l'evaluation, calcule le score et peut analyser les reviews.
5. Le service evaluation appelle `recompense-service` via `/api/rewards/process-evaluation`.
6. Le service recompense met a jour le profil freelance, les points, le niveau, les badges et l'historique.
7. Le frontend affiche dashboard, badges actifs, profils, historique, certificats et notifications.

## Routes gateway

La gateway expose deux styles de routes:

- routes directes evaluation: `/evaluations/**`, `/review/**`, `/historique/**`;
- routes directes recompense: `/api/badges/**`, `/api/recompenses/**`, `/api/rewards/**`, `/api/user-badges/**`, `/api/points/**`, `/api/notifications/**`;
- route prefixee evaluation: `/evaluation/**` avec `StripPrefix=1`;
- route prefixee recompense: `/reward/**` avec `StripPrefix=1`.

Le frontend utilise par defaut la gateway pour les deux bases:

- `evaluationBaseUrl = http://localhost:8088`
- `rewardBaseUrl = http://localhost:8088`

## Fonctionnalites backend

### Evaluation service

Endpoints principaux:

- `POST /evaluations/add`: creer une evaluation;
- `GET /evaluations/all`: lister les evaluations;
- `GET /evaluations/user/{email}`: evaluations liees a un utilisateur;
- `GET /evaluations/{id}`: recuperer une evaluation;
- `PUT /evaluations/update/{id}`: modifier une evaluation;
- `DELETE /evaluations/delete/{id}`: supprimer une evaluation;
- `GET /evaluations/average/{userName}`: moyenne d'un utilisateur;
- `POST /review/add`: creer une review;
- `GET /review/all`: lister les reviews;
- `GET /review/evaluation/{evaluationId}`: reviews d'une evaluation;
- `GET /review/sentiment-stats`: statistiques sentiment;
- `POST /review/sentiment/analyze`: analyser un texte.

Configuration notable:

- base MySQL: `evaluationdb`;
- utilisateur MySQL: `root`;
- mot de passe MySQL: `root`;
- IA: Google Gemini via `GOOGLE_GENAI_API_KEY`;
- modele configure: `gemini-1.5-flash`;
- RabbitMQ local: `localhost:5672`.

### Recompense service

Endpoints principaux:

- `GET /api/badges`: lister les badges;
- `POST /api/badges`: creer un badge;
- `PUT /api/badges/{id}`: modifier un badge;
- `DELETE /api/badges/{id}`: supprimer un badge;
- `GET /api/badges/active`: badges actifs;
- `GET /api/recompenses`: lister les recompenses;
- `POST /api/recompenses`: creer une recompense;
- `POST /api/rewards/process-evaluation`: traiter une evaluation et recalculer les recompenses;
- `GET /api/rewards/dashboard`: donnees dashboard;
- `GET /api/rewards/profiles`: profils freelances;
- `GET /api/rewards/insights`: analyses metier;
- `GET /api/rewards/history`: historique;
- `GET /api/rewards/certificates/{historyId}`: telecharger un certificat PDF;
- `POST /api/rewards/assign-pending-rewards`: attribuer les recompenses en attente;
- `POST /api/rewards/recalculate-levels`: recalculer les niveaux;
- `GET /api/user-badges/active/{userName}`: badges actifs d'un utilisateur;
- `GET /api/points/points/{email}`: points utilisateur;
- `GET /api/notifications/{email}`: notifications utilisateur.

Configuration notable:

- base MySQL: `recompense_db`;
- utilisateur MySQL: `root`;
- mot de passe MySQL: `root`;
- RabbitMQ local: `localhost:5672`;
- mail local par defaut: `localhost:1025`;
- expediteur par defaut: `no-reply@freelink.local`.

## Interface frontend

Le frontend Angular contient une interface complete orientee operations:

- Dashboard global;
- creation et consultation des evaluations;
- gestion des badges;
- gestion des recompenses;
- liste des freelances et profils;
- historique des attributions;
- espace utilisateur;
- configuration des URLs API;
- telechargement et renvoi de certificats.

Les appels HTTP passent par des helpers:

- `frontend/src/app/evaluation/evaluation.api.ts`;
- `frontend/src/app/review/review.api.ts`;
- `frontend/src/app/badge/badge.api.ts`.

## Commandes de lancement

### Lancement complet Windows

Depuis la racine du projet:

```bat
run-all.bat
```

Ce script lance dans l'ordre:

1. Eureka server sur `8761`;
2. API Gateway sur `8088`;
3. Evaluation service sur `8085`;
4. Recompense service sur `8094`;
5. Angular sur `4200`.

### Lancement manuel

Eureka:

```bat
cd eureka-server
mvnw.cmd -Dmaven.test.skip=true spring-boot:run
```

Gateway:

```bat
cd api-gateway
mvnw.cmd -Dmaven.test.skip=true spring-boot:run
```

Evaluation:

```bat
cd evaluation-service\evaluation-service
set GOOGLE_GENAI_API_KEY=local-test-key
mvnw.cmd -Dmaven.test.skip=true spring-boot:run
```

Recompense:

```bat
cd recompense\recompense
mvnw.cmd -Dmaven.test.skip=true spring-boot:run
```

Frontend:

```bat
cd frontend
npm install
npm run dev
```

## URLs utiles

- Frontend: `http://localhost:4200`
- Eureka dashboard: `http://localhost:8761`
- API Gateway: `http://localhost:8088`
- Evaluation service direct: `http://localhost:8085`
- Recompense service direct: `http://localhost:8094`

## Prerequis locaux

- Java 17;
- Maven wrapper fourni dans les services;
- Node.js compatible Angular 18;
- MySQL local;
- bases creees automatiquement si MySQL est accessible:
  - `evaluationdb`;
  - `recompense_db`;
- RabbitMQ local si les flux evenementiels sont utilises;
- serveur mail local type MailHog/MailDev sur `localhost:1025` pour tester les mails;
- variable `GOOGLE_GENAI_API_KEY` pour l'analyse IA reelle.

## Resume metier

Le projet transforme les evaluations en progression mesurable pour les freelances. Un freelance recoit des points, des badges, un niveau, des recompenses potentielles et des certificats selon ses performances. Le dashboard permet ensuite a l'equipe de suivre la qualite, l'engagement et les recompenses attribuees.
