Construire l'image

    docker build -t "geosync/database" .

Lancer le conteneur, en lui donnant un nom

    docker run -d --name geosync_database_1 geosync/database

Se connecter sur le conteneur

    docker exec -it geosync_database_1 bash

Arrêter le conteneur

    docker rm -f geosync_database_1

