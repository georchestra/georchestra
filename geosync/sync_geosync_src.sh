#!/bin/bash

# utilisation de unison pour synchroniser les dossiers /home/georchestra/georchestra/geosync/geosync et /home/geosync/geosync
#
# on utilise le fichier ~/.unison/geosync_src pour décrire origine et destination
# il suffit d'invoquer la commande unison en passant le nom du fichier : geosync_src
# le sudo est utile quand le user georchestra doit écrire dans le répertoire du user geosync

# cat ~/.unison/geosync_src 
#
# Unison preferences file
#
#root=/home/georchestra/georchestra/geosync/geosync
#root=/home/geosync/geosync
#
#ignore = Path {.git}
#ignore = Path {test}

sudo unison geosync_src

