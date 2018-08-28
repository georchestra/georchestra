Construire l'image

    docker build -t "mshe/geoserver" .

Lancer le conteneur, en lui donnant un nom

    docker run -d -t --name mshe_geoserver_1 mshe/geoserver

Se connecter sur le conteneur

    docker exec -it mshe_geoserver_1 bash

Arrêter le conteneur

    docker rm -f mshe_geoserver_1

