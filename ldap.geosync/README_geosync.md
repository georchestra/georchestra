on s'intéresse pour l'instant au conteneur georchestra/ldap, pour lui ajouter l'authentification saslauth.

Construire l'image

    docker build -t "geosync/ldap" .

Lancer le conteneur, en lui donnant un nom

    docker run -d --name geosync_ldap_1 geosync/ldap

Se connecter sur le conteneur

    docker exec -it geosync_ldap_1 bash

Arrêter le conteneur

    docker rm -f geosync_ldap_1


