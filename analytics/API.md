# API du module *Analytics*

## Eléments généraux

### Statistiques recuillies

Analytics recueille les statistiques relatives aux requêtes sur la plateforme concernant 3 modules de geOrchestra:

- flux OGC de geoserver (service ``ws/ogc``)
- geonetwork (service ``ws/geonetwork``)
- extractorapp (service ``ws/extractorapp``)

Seules l'accès aux statistiques relatives aux requêtes OGC sont décrites ci-dessous.
L'API est très similaire pour les 3 services.
Les services ``ws/geonetwork`` et ``ws/extractorapp`` ne renvoient des données que si le module *downloadform* est déployé sur la plateforme geOrchestra.


### Paramètres des requêtes

Toutes les méthodes utilisent les mêmes paramètres:

- **month**: mois de l'année dont on souhaite obtenir les statistiques. Utilisez la valeur ``0`` pour ne pas préciser le mois.
- **year**: années dont on souhaite obtenir les statistiques. Utilisez la valeur ``0`` pour ne pas préciser l'année.
- **start**: indice de début des résultats des statistiques demandées. L'indice minimum est ``0``.
- **limit**: nombre de résultats demandés.
- **sort**: prorpiété de tri de la liste de résultat. Ce paramètre s'écrit sous la forme d'un objet JSON du type ``[{"property":"count","direction":"DESC"}]`` ou ``property`` est la propriété de tri et ``direction`` le sens du tri ("DESC = décroissant et ASC = croissant). Une fois encodé il prend la forme suivante: ``%5B%7B%22property%22%3A%22count%22%2C%22direction%22%3A%22DESC%22%7D%5D``.

Les requêtes suivantes sont données afin de recueuillir les statistiques des 25 premiers résultats classés par la propriété ``count`` dans le sens descendant pour le mois de décembre 2015.
Pour recuillir les statistiques sans limite de date, utiliser les paramètres ``month=0&year=0``.


### Erreur de requête

En cas d'erreur de reqête, le serveur retourne une réponse du type: (``msg`` à adapter)

```
{"msg":"invalid params => ","success":false}
```


### Absence de résultats

En cas d'absence de résultats, la réponse du serveur est:

```
{"total":0,"results":[],"success":true}
```


### Lien avec LDAPAdmin

Le requêtage sur les utilisateurs et les groupes (ou organismes) rend possible un lien avec l'API de LDAPAdmin.


### Limites

Le module *Analytics* fonctionne sur la base des fichiers de logs.

Certaines limites sont connues pour la methode ``layers``:

- le champ *layer* peut être vide dans le cas de certaines requêtes OGC, notamment de type *getCapabilities*
- le traitement des résultats se fait uniquement sur le nom du layer. En effet, les fichiers de log ne tiennent pas compte du workspace Geoserver de la couche appelée. Ainsi, le même layer peut apparaître 2 fois s'il est appelé via ``/ns/ows?layer=layer`` et ``/ows?layer=ns:layer``.


## Service ``ws/ogc``

### Méthode ``groups``: statistiques par organisme

**Requête:**

```
curl -u login:password "https://localhost/analytics/ws/ogc/groups?month=12&year=2015&start=0&limit=25&sort=%5B%7B%22property%22%3A%22count%22%2C%22direction%22%3A%22DESC%22%7D%5D"
```

**Réponse:**

```json
{
    "total": 3,                 // Nombre total de résultats
    "results": [{               // Liste des résultats retournés limités par les paramètres "start" et "limit"
        "count": 50527,         // Statistics pour cet organisme (nombre de requêtes)
        "org": "Org"            // Nom de l'organisme
    }, {
        "count": 1101,
        "org": "Camptocamp"
    }, {
        "count": 254,
        "org": "geOrchestra"
    }],
    "success": true             // Succès ou erreur du traitement de la requête par le serveur
}
```


### Méthode ``users``: statistiques par utilisateur

**Requête:**

```
curl -u login:password "https://localhost/analytics/ws/ogc/users?month=12&year=2015&start=0&limit=25&sort=%5B%7B%22property%22%3A%22count%22%2C%22direction%22%3A%22DESC%22%7D%5D"
/analytics/ws/ogc/users?month=1&year=2016&start=0&limit=25&sort=%5B%7B%22property%22%3A%22count%22%2C%22direction%22%3A%22DESC%22%7D%5D
```

**Réponse:**

```json
{
    "total": 6,                         // Nombre total de résultats
    "results": [{                       // Liste des résultats retournés limités par les paramètres "start" et "limit"
        "user_name": "anonymousUser",   // Nom de l'utilisateur (login)
        "count": 50711                  // Statistics pour cet utilisateur (nombre de requêtes)
    }, {
        "user_name": "fvanderbiest",
        "count": 646
    }, {
        "user_name": "vdorut",
        "count": 315
    }, {
        "user_name": "pmauduit",
        "count": 106
    }, {
        "user_name": "geoserver_privileged_user",
        "count": 70
    }, {
        "user_name": "fjacon",
        "count": 34
    }],
    "success": true                     // Succès ou erreur du traitement de la requête par le serveur
}
```


### Méthode ``layers``: statistiques par couche de données:

**Requête:**

```
curl -u login:password "https://localhost/analytics/ws/ogc/layers?month=12&year=2015&start=0&limit=25&sort=%5B%7B%22property%22%3A%22count%22%2C%22direction%22%3A%22DESC%22%7D%5D"
/analytics/ws/ogc/layers?month=1&year=2016&start=0&limit=25&sort=%5B%7B%22property%22%3A%22count%22%2C%22direction%22%3A%22DESC%22%7D%5D
```

**Réponse:**

```json
{
    "total": 164,                       // Nombre total de résultats
    "results": [{                       // Liste des résultats retournés limités par les paramètres "start" et "limit"
        "count": 17563,                 // Statistics pour cette couche (nombre de requêtes)
        "request": "getmap",            // Type de requête
        "layer": "dem:altitude",        // Nom de la couche de données. Peut-être vide pour les opérations de type *getCapabilities*.
        "service": "WMS"                // Type de service
    }, {
        "count": 6525,
        "request": "getmap",
        "layer": "unearthedoutdoors%3atruemarble",
        "service": "WMS"
    },
    ...
    {
        "count": 197,
        "request": "getmap",
        "layer": "pmauduit_test%3atoilettes_publiques",
        "service": "WMS"
    }, {
        "count": 191,
        "request": "describelayer",
        "layer": "geor:sdi",
        "service": "WMS"
    }],
    "success": true                     // Succès ou erreur du traitement de la requête par le serveur
}
```


## Service ``ws/geonetwork``

### Méthode ``users``: statistiques par utilisateur

**Requête:**

```
curl -u login:password "https://localhost/analytics/ws/geonetwork/users?month=12&year=2015&start=0&limit=25&sort=%5B%7B%22property%22%3A%22count%22%2C%22direction%22%3A%22DESC%22%7D%5D"
```


### Méthode ``groups``: statistiques par organisme

**Requête:**

```
curl -u login:password "https://localhost/analytics/ws/geonetwork/groups?month=12&year=2015&start=0&limit=25&sort=%5B%7B%22property%22%3A%22count%22%2C%22direction%22%3A%22DESC%22%7D%5D"
```


### Méthode ``files``: statistiques par fichier

**Requête:**

```
curl -u login:password "https://localhost/analytics/ws/geonetwork/files?month=12&year=2015&start=0&limit=25&sort=%5B%7B%22property%22%3A%22count%22%2C%22direction%22%3A%22DESC%22%7D%5D"
```


## Service ``ws/extractor``

### Méthode ``users``: statistiques par utilisateur

**Requête:**

```
curl -u login:password "https://localhost/analytics/ws/extractor/users?month=12&year=2015&start=0&limit=25&sort=%5B%7B%22property%22%3A%22count%22%2C%22direction%22%3A%22DESC%22%7D%5D"
```


### Méthode ``groups``: statistiques par organisme

**Requête:**

```
curl -u login:password "https://localhost/analytics/ws/extractor/groups?month=12&year=2015&start=0&limit=25&sort=%5B%7B%22property%22%3A%22count%22%2C%22direction%22%3A%22DESC%22%7D%5D"
```


### Méthode ``layers``: statistiques par couche de données

**Requête:**

```
curl -u login:password "https://localhost/analytics/ws/extractor/layers?month=12&year=2015&start=0&limit=25&sort=%5B%7B%22property%22%3A%22count%22%2C%22direction%22%3A%22DESC%22%7D%5D"
```
