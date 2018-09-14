Construire l'image

    docker build -t "mshe/geosync" .

Lancer le conteneur, en lui donnant un nom

    docker run -d --name mshe_geosync_1 -e TZ=Europe/Paris mshe/geosync

Lancer le conteneur en lui permettant de charger le module fuse de l'hôte

    docker run -d --name mshe_geosync_1 --privileged --cap-add=SYS_ADMIN -v /dev/fuse:/dev/fuse -e TZ=Europe/Paris mshe/geosync

La même chose, mais avec un partage de données
    docker run -d --name mshe_geosync_1 --privileged --cap-add=SYS_ADMIN -v /dev/fuse:/dev/fuse -v /home/geosync:/home/geo_user -e TZ=Europe/Paris mshe/geosync

    docker run -d --name mshe_geosync_1 --privileged --cap-add=SYS_ADMIN -v /dev/fuse:/dev/fuse -v /home/geosync:/home/geo_user -v /home/geosync/bin:/geosync -e TZ=Europe/Paris mshe/geosync

Lancer le conteneur comme l'aurait fait docker-compose

    docker run -d --name georchestra_geosync_1 --privileged --cap-add=SYS_ADMIN -v /dev/fuse:/dev/fuse -v /home/georchestra/georchestra/docker/ssl/DigiCertCA.crt:/usr/local/share/ca-certificates/DigiCertCA.crt:ro -e TZ=Europe/Paris -e SERVER_URL=https://georchestra-docker.umrthema.univ-fcomte.fr -e OCL_URL=https://owncloud-mshe.univ-fcomte.fr -e OPEN_USER_DAVPASS=Lesoleilauzenith -e RSCT_USER_DAVPASS=Lesoleilauzenith --add-host georchestra-docker.umrthema.univ-fcomte.fr:172.20.74.124 mshe/geosync

Se connecter sur le conteneur

    docker exec -it mshe_geosync_1 bash

Arrêter le conteneur

    docker rm -f mshe_geosync_1


