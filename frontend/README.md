# Front Angular evaluation/recompense

Application Angular pour tester et presenter les microservices:

- evaluation-service: `http://localhost:8085`
- recompense-service: `http://localhost:8094`
- front Angular: `http://localhost:4200`

## Installation

```bash
npm install
```

## Lancer

```bash
npm run dev
```

## Build / verification

```bash
npm run check
```

## Ecrans

- Tableau: dashboard recompense, top freelancers, progression mensuelle.
- Evaluations: creation d'une evaluation client/freelancer.
- Badges: creation, modification et suppression de badges automatiques.
- Recompenses: CRUD du catalogue admin.
- Freelancers: profils recompense, analyse metier et attribution des recompenses eligibles.
- Historique: mouvements de badges, niveaux et recompenses.
- Mon espace: historique evaluations, objectifs metier et badges PDF.
- Reglages: URLs backend et mode interface.

La securite d'acces du microservice recompense est ouverte pour faciliter les tests locaux.
