# Front evaluation/recompense

Front autonome pour tester et presenter les microservices:

- evaluation-service: `http://localhost:8085`
- recompense-service: `http://localhost:8094`
- front: `http://localhost:4200`

## Lancer

```bash
npm run dev
```

Le serveur utilise uniquement Node.js, donc aucune installation de dependances n'est necessaire.

## Ecrans

- Tableau: dashboard recompense, top freelancers, progression mensuelle.
- Evaluations: creation d'une evaluation client et test direct du moteur d'attribution automatique.
- Badges: creation, modification et suppression de badges automatiques.
- Recompenses: CRUD du catalogue admin.
- Freelancers: profils recompense calcules par le backend.
- Historique: mouvements de badges/niveaux et certificats PDF.
- Mon espace: vue freelancer par email.
- Reglages: URLs backend et mode interface.

La securite d'acces du microservice recompense est ouverte pour faciliter les tests locaux.
