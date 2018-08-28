#!/bin/bash
# permet de supprimer (dépublier) les couches du geoserver

usage() { 
  echo "Usage : clean.sh [OPTION]"
  echo ""
  echo "Options"
  echo " -a     (all) supprime toutes les couches du geoserver"
  echo " -d     (diff) supprime les couches qui ne sont plus partagées (différence entre les couches du geoserver par celles du dossier partagé)"
  echo " -s     (simulation) ne supprime rien"  
  echo " -v     verbeux"  
  echo " (-h)   affiche cette aide"
  echo ""
} 

xpath() { 
local xp=$1 
local xml=$2
echo $(xmllint --xpath "${xp}" "${xml}" - <<<"$xml" 2>/dev/null)
# redirige l'erreur standard vers null pour éviter d'être averti de chaque valeur manquante (XPath set is empty)
# mais cela peut empêcher de détecter d'autres erreurs
# TODO: faire tout de même un test, une fois sur le fichier, de la validité du xml
} 

echoerror() {
  echo "[$(date +'%Y-%m-%dT%H:%M:%S%z')]: $@" >&2  #Redirection vers la sortie d'erreurs standard  (stderr)
} 

#echo if verbose=1
echo_ifverbose() {
  if [ $verbose ]; then echo "$@"; fi
} 

main() {
  # chemin du script pour pouvoir appeler d'autres scripts dans le même dossier
  BASEDIR=$(dirname "$0")
  #echo "BASEDIR:$BASEDIR"
  
  local OPTIND opt
  while getopts "adsvh" opt; do
    # le : signifie que l'option attend un argument
    case $opt in
      a) deleteall=1 ;;
      d) deletediff=1 ;;
      s) simulation=1 ;;
      v) verbose=1; verbosestr="-v" ;;
      h) help=1 ;;
  # si argument faux renvoie la sortie    
      \?) error "Option invalide : -$OPTARG" ;;
  # si option sans argument renvoie la sortie   
      :) error "L'option -$OPTARG requiert un argument." ;;
    esac
  done
  shift $((OPTIND-1))
 
  # vérification des paramètres

  # si aucune suppression n'est demandée, alors affiche l'aide
  if  [ ! "$deleteall" ] && [ ! "$deletediff" ]; then
    usage
    exit
  fi
  
  if  [ $simulation ]; then
      echo "INFO simulation !"
  fi
  
  if  [ $deleteall ]; then
    echo_ifverbose "INFO suppression toutes les couches du geoserver..."
  fi

  if  [ $deletediff ]; then
    echo_ifverbose "INFO suppression des couches qui ne sont plus partagées..."
  fi

  # pour générer un nom lisible et simplifier pour fichier
  # $(util::cleanName "./tic/tac toe.shp") -> tac_toe.shp #takes a filepath and returns a pretty name
  source "$BASEDIR/lib/util.sh"

  paramfile="$HOME/.geosync.conf"

  # récupère les paramètres de connexion dans le fichier .geosync situé dans le même dossier que ce script
  local host login passwd workspace datastore pg_datastore db logs share_directory
  source "$paramfile"

  # attention le fichier .geosync est interprété et fait donc confiance au code
  # pour une solution plus sûr envisager quelque chose comme : #while read -r line; do declare $line; done < "$HOME/.pass"

  # vérification du host/login/mot de passe
  if [ ! "$login" ] || [ ! "$passwd" ] || [ ! "$host" ]; then
    error "url du georserver, login ou mot de passe non définit; le fichier spécifié avec l'option -p [paramfile] doit contenir la définition des variables suivantes sur 3 lignes : login=[login] passwd=[password] host=[geoserver's url]"
  fi

  url=$host
  password=$passwd

  # créer un dossier temporaire et stocke son chemin dans une variable
  local tmpdir=~/tmp/geosync_clean

  # supprime le dossier temporaire et le recrée
  rm -R "$tmpdir"
  mkdir -p "$tmpdir"

  ###################
  # vecteurs : liste ceux publiés sur le geoserver
  ###################
  # ------------------------ Pour les vecteurs issus du datastore de type Directory $datastore
  list_path="${tmpdir}/vectors_published_directory"

  echo_ifverbose "INFO liste les vecteurs du datastore de type Directory : ${datastore}"
  cmd="curl --silent -u '${login}:${password}' -XGET ${url}/geoserver/rest/workspaces/${workspace}/datastores/${datastore}/featuretypes.xml"
  echo_ifverbose "INFO ${cmd}"
  
  xml=$(eval ${cmd})
  # extrait du contenu du xml :
  #<featureTypes> <featureType> <name>test_ids__chailluz_charbonniere</name> <atom:link xmlns:atom="http://www.w3.org/2005/Atom" rel="alternate" href="https://georchestra-mshe.univ-fcomte.fr/geoserver/rest/workspaces/geosync-ouvert/featuretypes/test_ids__chailluz_charbonniere.xml" type="application/xml"/> </featureType> <featureType>...</featureType> ... </featureTypes>
  itemsCount=$(xpath 'count(/featureTypes/featureType)' "${xml}")
  echo_ifverbose "INFO ${itemsCount} vecteur(s) publié(s)"

  > "${list_path}" # (re)créer le fichier vide
  for (( i=1; i < ${itemsCount} + 1; i++ )); do 
    name=$(xpath '/featureTypes/featureType['${i}']/name/text()' "${xml}")
    echo "${name}" >> "${list_path}"
  done

  # ------------------------ Pour les vecteurs issus du datastore de type PostGIS $pg_datastore

  list_path="${tmpdir}/vectors_published_postgis"

  echo_ifverbose "INFO liste les vecteurs du datastore de type PostGIS : ${pg_datastore}"
  cmd="curl --silent -u '${login}:${password}' -XGET ${url}/geoserver/rest/workspaces/${workspace}/datastores/$pg_datastore/featuretypes.xml"
  echo_ifverbose "INFO ${cmd}"
  
  xml=$(eval ${cmd})


  itemsCount=$(xpath 'count(/featureTypes/featureType)' "${xml}")
  echo_ifverbose "INFO ${itemsCount} vecteur(s) publié(s)"

  > "${list_path}" # (re)créer le fichier vide
  for (( i=1; i < ${itemsCount} + 1; i++ )); do 
    name=$(xpath '/featureTypes/featureType['${i}']/name/text()' "${xml}") 
    echo "${name}" >> "${list_path}"
  done

  ###################
  # rasteurs : liste ceux publiés sur le geoserver
  ###################
  list_path="${tmpdir}/rasters_published"

  echo_ifverbose "INFO liste les rasteurs (coveragestores)"
  cmd="curl --silent -u '${login}:${password}' -XGET ${url}/geoserver/rest/workspaces/${workspace}/coveragestores.xml" 
  echo_ifverbose "INFO ${cmd}"

  xml=$(eval ${cmd})
  # extrait du contenu du xml :
  #<coverageStores><coverageStore><name>mos</name><atom:link xmlns:atom="http://www.w3.org/2005/Atom" rel="alternate" href="https://georchestra-mshe.univ-fcomte.fr/geoserver/rest/workspaces/geosync-ouvert/coveragestores/mos.xml" type="application/xml"/></coverageStore><coverageStore>...</coverageStore> ... </coverageStores>
  itemsCount=$(xpath 'count(/coverageStores/coverageStore)' "${xml}")
  echo_ifverbose "INFO ${itemsCount} rasteur(s) publié(s)"

  > "${list_path}" # (re)créer le fichier vide
  for (( i=1; i < ${itemsCount} + 1; i++ )); do 
    name=$(xpath '/coverageStores/coverageStore['${i}']/name/text()' "${xml}") 
    echo "${name}" >> "${list_path}"
  done

  ###################
  # styles : liste ceux publiés sur le geoserver
  ###################
  list_path="${tmpdir}/styles_published"
  
  echo_ifverbose "INFO liste les styles"
  cmd="curl --silent -u '${login}:${password}' -XGET ${url}/geoserver/rest/workspaces/${workspace}/styles.xml"
  echo_ifverbose "INFO ${cmd}"

  xml=$(eval ${cmd})
  # extrait du contenu du xml :
  #<styles> <style> <name>generic</name> <atom:link xmlns:atom="http://www.w3.org/2005/Atom" rel="alternate" href="https://georchestra-mshe.univ-fcomte.fr/geoserver/rest/styles/generic.xml" type="application/xml"/> </style> <style>...</style> ... </styles>
  itemsCount=$(xpath 'count(/styles/style)' "${xml}")
  echo_ifverbose "INFO ${itemsCount} style(s) publié(s)"

  > "${list_path}" # (re)créer le fichier vide
  for (( i=1; i < ${itemsCount} + 1; i++ )); do 
    name=$(xpath '/styles/style['${i}']/name/text()' "${xml}")
    echo "${name}" >> "${list_path}"
  done

  # TODO il faudrait aussi supprimer les fiches de metadata (qui peuvent être orphelines, sans couche associée)
  # http://geonetwork-opensource.org/manuals/2.10.4/eng/developer/xml_services/metadata_xml_insert_update_delete.html#delete-metadata-xml-metadata-delete

  ######################

  # vérifie que le chemin de l'arborescence à publier a bien été défini dans la conf
  if [[ ! "${share_directory}" ]]; then
    echo "WARNING aucun chemin d'arborescence à publier ('share_directory') défini dans .geosync.conf"
    echoerror "WARNING aucun chemin d'arborescence à publier ('share_directory') défini dans .geosync.conf"

    share_directory="$HOME/owncloudsync" # le chemin par défaut est conservé temporairement pour rétro-compatibilité # TODO ne pas prendre de valeur pas défaut et faire une vraie erreur
    echo "WARNING chemin d'arborescence par défaut : ${share_directory}"
  fi



  # si on souhaite supprimer la différence entre les couches publiées et celles partagées
  # alors calcule la différence des listes et la stocke dans la liste des couches à supprimer
  if [ "$deletediff" ]; then
      
      cd "$share_directory"
      
      shopt -s globstar nocaseglob
      # set globstar, so that the pattern ** used in a pathname expansion context will 
      # match a files and zero or more directories and subdirectories.
      # nocaseglob : case insensitive
      #shopt -s extglob allow (.tif|.jpg) but does not work with globstar **
      
      ###################
      # pour les vecteurs
      ###################
      list_path="${tmpdir}/vectors_shared"

      echo_ifverbose "INFO liste les vecteurs partagés"
      echo_ifverbose "INFO for filepath in **/*.shp; do...done"
      for filepath in **/*.shp; do
        outputlayername=$(util::cleanName "$filepath" -p)
        outputlayernamesansext=${outputlayername%%.*} #sans extension : toe.shp.xml -> toe
        #echo "{$outputlayernamesansext}"
        echo "$outputlayernamesansext" >> "${list_path}"
      done

      cmd="wc -l < '${list_path}'"
      echo_ifverbose "INFO ${cmd}"
      itemsCount=$(eval ${cmd})
      echo_ifverbose "INFO ${itemsCount} vecteur(s) partagé(s)"
      
      # prend uniquement les noms présents dans la première liste (arraydiff <- liste1 - liste2)
      comm -23 <(sort "$tmpdir/vectors_published_directory") <(sort "$tmpdir/vectors_shared") > "$tmpdir/vectors_tobedeleted_directory"
      # -2 suppress lines unique to FILE2
      # -3 suppress lines that appear in both files

      cmd="wc -l < '$tmpdir/vectors_tobedeleted_directory'"
      echo_ifverbose "INFO ${cmd}"
      itemsCount=$(eval ${cmd})
      echo_ifverbose "INFO ${itemsCount} vecteur(s) (Directory) à supprimer"  

      # ------------------------ Pour les vecteurs issus du datastore de type PostGIS $pg_datastore

      # prend uniquement les noms présents dans la première liste (arraydiff <- liste1 - liste2)
      comm -23 <(sort "$tmpdir/vectors_published_postgis") <(sort "$tmpdir/vectors_shared") > "$tmpdir/vectors_tobedeleted_postgis"

      ###################
      # pour les rasters
      ###################
      list_path="${tmpdir}/rasters_shared"

      echo_ifverbose "INFO liste les rasteurs partagés"
      echo_ifverbose "INFO for filepath in **/*.{tif,png,jpg,ecw} **/w001001.adf; do...done"
      for filepath in **/*.{tif,png,jpg,ecw} **/w001001.adf; do
        outputlayername=$(util::cleanName "$filepath" -p)
        outputlayernamesansext=${outputlayername%%.*} #sans extension : toe.shp.xml -> toe
        #echo "{$outputlayernamesansext}"
        echo "$outputlayernamesansext" >> "${list_path}"
      done

      cmd="wc -l < '${list_path}'"
      echo_ifverbose "INFO ${cmd}"
      itemsCount=$(eval ${cmd})
      echo_ifverbose "INFO ${itemsCount} rasteur(s) partagé(s)"

      # prend uniquement les noms présents dans la première liste (arraydiff <- liste1 - liste2)
      comm -23 <(sort "$tmpdir/rasters_published") <(sort "$tmpdir/rasters_shared") > "$tmpdir/rasters_tobedeleted"
      
      ####################
      # pour les styles
      ###################
      list_path="${tmpdir}/styles_shared"

      echo_ifverbose "INFO liste les styles partagés"
      echo_ifverbose "INFO for filepath in **/*.sld ; do...done"
      for filepath in **/*.sld ; do
        outputlayername=$(util::cleanName "$filepath" -p)
        outputlayernamesansext=${outputlayername%%.*} #sans extension : toe.shp.xml -> toe
        echo "$outputlayernamesansext" >> "${list_path}"
      done

      cmd="wc -l < '${list_path}'"
      echo_ifverbose "INFO ${cmd}"
      itemsCount=$(eval ${cmd})
      echo_ifverbose "INFO ${itemsCount} style(s) partagé(s)"

      # prend uniquement les noms présents dans la première liste (arraydiff <- liste1 - liste2)
      comm -23 <(sort "$tmpdir/styles_published") <(sort "$tmpdir/styles_shared") > "$tmpdir/styles_tobedeleted_potentially"

      shopt -u globstar nocaseglob
	  
  # --------------------------    
  
  # si on souhaite supprimer toutes les couches
  # alors stocke la liste des couches publiées dans la liste des couches à supprimer
  elif [ "$deleteall" ]; then
      cat "$tmpdir/vectors_published_directory" > "$tmpdir/vectors_tobedeleted_directory"
      cat "$tmpdir/vectors_published_postgis" > "$tmpdir/vectors_tobedeleted_postgis"
      cat "$tmpdir/rasters_published" > "$tmpdir/rasters_tobedeleted"
      cat "$tmpdir/styles_published" > "$tmpdir/styles_tobedeleted_potentially" 
  fi

  # Attention : retirer ici des listes les éléments que l'on souhaite tout de même conserver
  # par exemple les styles par défaut que la liste des styles publiés inclut
  list_to_keep_path="${tmpdir}/styles_to_keep"
  > "${list_to_keep_path}" # (re)créer le fichier vide
  echo "generic" >> "${list_to_keep_path}"
  echo "line" >> "${list_to_keep_path}"
  echo "polygon" >> "${list_to_keep_path}"
  echo "point" >> "${list_to_keep_path}"
  echo "raster" >> "${list_to_keep_path}"
  # prend uniquement les noms présents dans la première liste (arraydiff <- liste1 - liste2)
  comm -23 <(sort "$tmpdir/styles_tobedeleted_potentially") <(sort "${list_to_keep_path}") > "$tmpdir/styles_tobedeleted"


  cmd="wc -l < '$tmpdir/vectors_tobedeleted_postgis'"
  echo_ifverbose "INFO ${cmd}"
  itemsCount=$(eval ${cmd})
  echo_ifverbose "INFO ${itemsCount} vecteur(s) (PostGIS) à supprimer"  

  cmd="wc -l < '$tmpdir/rasters_tobedeleted'"
  echo_ifverbose "INFO ${cmd}"
  itemsCount=$(eval ${cmd})
  echo_ifverbose "INFO ${itemsCount} rasteur(s) à supprimer"  

  cmd="wc -l < '$tmpdir/styles_tobedeleted'"
  echo_ifverbose "INFO ${cmd}"
  itemsCount=$(eval ${cmd})
  echo_ifverbose "INFO ${itemsCount} style(s) à supprimer"     

  
  # parcourt la liste des styles à supprimer dans le système de fichier
  # et supprime chacun d'eux
  while read style; do
    # Suppression des dépendances du style à supprimer
    # en mettant à jour les couches qui utilisaient le style à supprimer
    # c'est à dire en remplaçant leur style par un style par défaut
    # pour toutes les couches du workspace (quelque soit le datastore : Directory, PostGIS...)
    # nécessaire car impossible de supprimer un style qui est utilisé par une couche
    # nécessaire d'effectuer l'opération avant la suppression des couches sinon erreur si couche supprimée

    # fusion des listes de vecteurs des différents datastore (Directory, PostGIS) en une, car le traitement est le même
    cat "$tmpdir/vectors_published_directory" > "$tmpdir/vectors_published_any_datastore"
    cat "$tmpdir/vectors_published_postgis" >> "$tmpdir/vectors_published_any_datastore"

    # suppression de la dépendance au style
    # TODO une piste d'évolution pour simplifier les opérations et rendre plus performant a priori
    #      serait, lors de la définition d'un style pour une couche, de ne pas le mettre en style par défaut mais en style additionnel
    #      la suppression d'un style utilisé en style additionnel pour une couche (et non par défaut) ne pose a priori pas de problème
    #      on pourrait donc d'abord supprimer les styles sans avoir besoin de supprimer leur dépendance (sachant qu'en plus, souvent la couche est au final supprimée également)
    #      donc au lieu de supprimer la dépendance au style, le style, puis la couche, on pourrait supprimer les couches puis les styles
    #   sinon, une optimisation intermédiaire (tout en continuant de supprimer les dépendances) serait de supprimer les couches d'abord
    #      puis uniquement pour celles restantes supprimer la dépendance au style, puis le style
    #     (cela demande de bien considérer toutes les couches restantes et non celles qui auraient dues l'être mais qui sont encore là pour cause d'erreur ou autre)

    while read layer; do
      if [[ "${layer}" == "${style}" ]]; then
        echo_ifverbose "INFO suppression de la dépendance au style '${style}' et remplacement par le style 'generic' pour la couche : ${layer}"
        cmd="curl --silent -w %{http_code} \
                 -u ${login}:${password} \
                 -XPUT -H \"Content-type: text/xml\" \
                 -d \"<layer><defaultStyle><name>generic</name></defaultStyle></layer>\" \
                 ${url}/geoserver/rest/layers/${workspace}:${layer}.xml"
        echo_ifverbose "INFO ${cmd}"

        if  [ ! $simulation ]; then
          result=$(eval ${cmd}) # retourne le contenu de la réponse suivi du http_code (attention : le contenu n'est pas toujours en xml quand demandé surtout en cas d'erreur; bug geoserver ?)
          statuscode=${result:(-3)} # prend les 3 derniers caractères du retour de curl, soit le http_code
          echo_ifverbose "INFO statuscode=${statuscode}"

          if [[ "${statuscode}" -ge "200" ]] && [[ "${statuscode}" -lt "300" ]]; then
            echo "OK suppression de la dépendance au style '${style}' pour '${layer}' réussie"
          else
            echoerror "ERROR suppression de la dépendance au style '${style}' pour '${layer}' échouée... error http code : ${statuscode}"
            echoerror "${cmd}"
            echo "ERROR suppression de la dépendance au style '${style}' pour '${layer}' échouée (${statuscode})"
          fi 
        fi
      fi
    done <"$tmpdir/vectors_published_any_datastore"
    # Idem pour les styles utilisés par les rasters
    while read layer; do
      if [[ "${layer}" == "${style}" ]]; then
        echo_ifverbose "INFO suppression de la dépendance au style '${style}' et remplacement par le style 'raster' pour la couche : ${layer}"
        cmd="curl --silent \
                 -u ${login}:${password} \
                 -XPUT -H \"Content-type: text/xml\" \
                 -d \"<layer><defaultStyle><name>raster</name></defaultStyle></layer>\" \
                 $url/geoserver/rest/layers/${workspace}:${layer}.xml"

        if  [ ! $simulation ]; then
          statuscode=$(eval $cmd)
          echo_ifverbose "INFO statuscode=${statuscode}"

          if [[ "${statuscode}" -ge "200" ]] && [[ "${statuscode}" -lt "300" ]]; then
            echo "OK suppression de la dépendance au style '${style}' pour '${layer}' réussie"
          else
            echoerror "ERROR suppression de la dépendance au style '${style}' pour '${layer}' échouée... error http code : ${statuscode}"
            echoerror "${cmd}"
            echo "ERROR suppression de la dépendance au style '${style}' pour '${layer}' échouée (${statuscode})"
          fi 
        fi
      fi
    done <"$tmpdir/rasters_published"

    # enfin, suppression du style lui-même
    # attention : ne vérifie pas qu'il s'agit d'un style par défaut à ce stade (generic, point...)
    #             il faut donc s'assurer avant que les styles par défaut ne se retrouvent pas dans la liste styles_tobedeleted si on veut les conserver
    echo_ifverbose "INFO suppression du style : ${style}"
    cmd="curl --silent -w %{http_code} -u '${login}:${passwd}' -XDELETE '${url}/geoserver/rest/workspaces/${workspace}/styles/${style}'" # erreur lors du curl : Accès interdit / Désolé, vous n'avez pas accès à cette page
    echo_ifverbose "INFO ${cmd}"

    if  [ ! ${simulation} ]; then
      result=$(eval ${cmd}) # retourne le contenu de la réponse suivi du http_code (attention : le contenu n'est pas toujours en xml quand demandé surtout en cas d'erreur; bug geoserver ?)
      statuscode=${result:(-3)} # prend les 3 derniers caractères du retour de curl, soit le http_code
      echo_ifverbose "INFO statuscode=${statuscode}"

      if [[ "${statuscode}" -ge "200" ]] && [[ "${statuscode}" -lt "300" ]]; then
        echo "OK suppression du style ${style} réussie"
      else
        echoerror "ERROR suppression du style ${style} échouée... error http code : ${statuscode}"
        echoerror "${cmd}"
        echo "ERROR suppression du style ${style} échouée (${statuscode})"
      fi 
    fi 

  done <"$tmpdir/styles_tobedeleted"


  # parcours la liste des vecteurs à supprimer
  # et supprime chacun d'eux
  while read vector; do
    echo_ifverbose "INFO suppression du vecteur (Directory): ${vector}"
    # supprime une couche
    
    cmd="curl --silent -w %{http_code} -u '$login:$passwd' -XDELETE '${url}/geoserver/rest/workspaces/${workspace}/datastores/${datastore}/featuretypes/${vector}?recurse=true&purge=all'"
    echo_ifverbose "INFO ${cmd}"
    # http://docs.geoserver.org/stable/en/user/rest/api/featuretypes.html#workspaces-ws-datastores-ds-featuretypes-ft-format
    # dans le cas d'un filesystem "recurse=true" dans le cas d'une bd postgis "recurse=false"

    if  [ ! ${simulation} ]; then
      result=$(eval ${cmd}) # retourne le contenu de la réponse suivi du http_code (attention : le contenu n'est pas toujours en xml quand demandé surtout en cas d'erreur; bug geoserver ?)
      statuscode=${result:(-3)} # prend les 3 derniers caractères du retour de curl, soit le http_code
      echo_ifverbose "INFO statuscode=${statuscode}"

      if [[ "${statuscode}" -ge "200" ]] && [[ "${statuscode}" -lt "300" ]]; then
        echo "OK suppression du vecteur ${vector} réussie"
      else
        echoerror "ERROR suppression du vecteur ${vector} échouée... error http code : ${statuscode}"
        echoerror "${cmd}"
        echo "ERROR suppression du vecteur ${vector} échouée (${statuscode})"
      fi 
    fi 

  done <"$tmpdir/vectors_tobedeleted_directory"
  
  # parcours la liste des vecteurs de postgis à supprimer
  # et supprime chacun d'eux
  while read vector; do
    echo_ifverbose "INFO suppression du vecteur (PostGIS) : ${vector}..."

    echo_ifverbose "INFO suppression du vecteur (PostGIS) ${vector} du geoserver"
    cmd="curl --silent -w %{http_code} -u '${login}:${passwd}' -XDELETE '${url}/geoserver/rest/workspaces/${workspace}/datastores/${pg_datastore}/featuretypes/${vector}?recurse=true&purge=all'"
    echo_ifverbose "INFO ${cmd}"
    # http://docs.geoserver.org/stable/en/user/rest/api/featuretypes.html#workspaces-ws-datastores-ds-featuretypes-ft-format
    # dans le cas d'un filesystem "recurse=true" dans le cas d'une bd postgis "recurse=false"

    if  [ ! ${simulation} ]; then
      result=$(eval ${cmd}) # retourne le contenu de la réponse suivi du http_code (attention : le contenu n'est pas toujours en xml quand demandé surtout en cas d'erreur; bug geoserver ?)
      statuscode=${result:(-3)} # prend les 3 derniers caractères du retour de curl, soit le http_code
      echo_ifverbose "INFO statuscode=${statuscode}"

      if [[ "${statuscode}" -ge "200" ]] && [[ "${statuscode}" -lt "300" ]]; then
        echo "OK suppression du vecteur (PostGIS) ${vector} réussie"
      else
        echoerror "ERROR suppression du vecteur ${vector} échouée... error http code : ${statuscode}"
        echoerror "${cmd}"
        echo "ERROR suppression du vecteur (PostGIS) ${vector} échouée (${statuscode})"
      fi 
    fi 

    cmd="psql -h ${dbhost} -d ${db} -U geosync -w -c 'DROP TABLE \"${vector}\";'"
    echo_ifverbose "INFO ${cmd}"

    if [ ! $simulation ]; then
      eval $cmd
    fi

    echo_ifverbose "INFO suppression des metadata du vecteur (PostGIS) : ${vector}"
    cmd="python $BASEDIR/lib/deleteMetadata.py -l '${login}' -p '${passwd}' -u '${url}' -w '${workspace}' -i '${vector}' ${verbosestr}"
    echo_ifverbose "INFO ${cmd}"

    if [ ! $simulation ]; then
        eval $cmd
    fi  

  done <"$tmpdir/vectors_tobedeleted_postgis"


  # parcours la liste des rasteurs à supprimer dans le système de fichiers et postgis
  # et supprime chacun d'eux
  while read raster; do
    echo_ifverbose "INFO suppression du rasteur : ${raster}..."

    echo_ifverbose "INFO suppression du rasteur ${raster} du geoserver"
    cmd="curl --silent -w %{http_code} -u '${login}:${passwd}' -XDELETE '${url}/geoserver/rest/workspaces/${workspace}/coveragestores/${raster}?recurse=true&purge=all'"
    echo_ifverbose "INFO ${cmd}"
    # http://docs.geoserver.org/stable/en/user/rest/api/coveragestores.html#workspaces-ws-coveragestores-cs-format

    if  [ ! ${simulation} ]; then
      result=$(eval ${cmd}) # retourne le contenu de la réponse suivi du http_code
      statuscode=${result:(-3)} # prend les 3 derniers caractères du retour de curl, soit le http_code
      echo_ifverbose "INFO statuscode=${statuscode}"

      if [[ "${statuscode}" -ge "200" ]] && [[ "${statuscode}" -lt "300" ]]; then
        echo "OK suppression du rasteur ${raster} réussie"
      else
        echoerror "ERROR suppression du rasteur ${raster} échouée... error http code : ${statuscode}"
        echoerror "${cmd}"
        echo "ERROR suppression du rasteur ${raster} échouée (${statuscode})"
      fi 
    fi 

    echo_ifverbose "INFO suppression du rasteur ${raster} de la base PostGIS"
    cmd="psql -h ${dbhost} -d ${db} -U geosync -w -c 'DROP TABLE \"${raster}\";'"
    echo_ifverbose "INFO ${cmd}"

    if  [ ! $simulation ]; then
      eval $cmd
    fi
 
    echo_ifverbose "INFO suppression des metadata du rasteur : ${raster}"
    cmd="python $BASEDIR/lib/deleteMetadata.py -l '${login}' -p '${passwd}' -u '${url}' -w '${workspace}' -i '${raster}' ${verbosestr}"
    echo_ifverbose "INFO ${cmd}"

    if [ ! $simulation ]; then
        eval $cmd
    fi
  
  done <"$tmpdir/rasters_tobedeleted"

  echo_ifverbose "INFO ...terminée"

} #end of main

# if this script is a directly call as a subshell (versus being sourced), then call main()
if [ "${BASH_SOURCE[0]}" == "$0" ]; then
  main "$@"
fi

