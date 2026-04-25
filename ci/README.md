# Pipelines CI Sprint 3

Ce dossier contient les trois pipelines demandes pour la phase DevOps.

## Jobs Jenkins a creer

| Job Jenkins | Jenkinsfile |
| --- | --- |
| `ci-evaluation-service` | `ci/Jenkinsfile-evaluation` |
| `ci-recompense-service` | `ci/Jenkinsfile-recompense` |
| `ci-frontend` | `ci/Jenkinsfile-frontend` |
| `cd-global-smart-freelance` | `ci/Jenkinsfile-cd-global` |

## Configuration Jenkins requise

- JDK configure avec le nom `JDK-17`.
- NodeJS configure avec le nom `NodeJS-18`.
- Serveur SonarQube configure dans Jenkins avec le nom `SonarQube`.
- Plugin Jenkins SonarQube installe.
- Scanner Sonar disponible pour le frontend via `npx sonar-scanner`.

## Ce que chaque pipeline fait

1. Checkout du code.
2. Execution des tests unitaires.
3. Generation de la couverture.
4. Build du livrable.
5. Analyse SonarQube.
6. Verification du Quality Gate.

## Projets SonarQube

| Projet | Cle Sonar |
| --- | --- |
| Evaluation service | `evaluation-service` |
| Recompense service | `recompense-service` |
| Frontend Angular | `freelink-frontend` |

## Captures a preparer pour le coaching

Pour chaque projet SonarQube:

1. lancer une premiere analyse avant refactoring;
2. prendre une capture de l'etat initial;
3. corriger les code smells, duplications ou bugs;
4. relancer le pipeline;
5. prendre une capture apres amelioration;
6. montrer la couverture importee depuis Jacoco ou LCOV.
