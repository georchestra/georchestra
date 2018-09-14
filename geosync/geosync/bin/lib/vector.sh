#!/bin/bash/
#
# Importe une couche .shp dans un geoserver

usage() { 
  echo "==> usage : "
  echo "source /lib/vector.sh"
  echo "vector::publish -i input [-o output=input] [-e epsg=2154] -l login -p password -u url -w workspace -s datastore -g pg_datastore -t db_host -b db_name -d db_user [-v]"
  echo ""
  echo "1. convertit (une copie du) shapefile (-i input) dans le système de coordonnées désiré (-e epsg)"
  echo "2. publie le shapefile converti sous le nom (-o output=input)"
  echo "   dans l'entrepôt (-s datastore) de l'espace de travail (-w workspace)"
  echo "   dans le geoserver accéssible à l'adresse donnée (-u url)"
} 

vector::publish() {

  echoerror() {
    echo "[$(date +'%Y-%m-%dT%H:%M:%S%z')]: $@" >&2  #Redirection vers la sortie d'erreurs standard  (stderr)
  } 

  usage() {
    echoerror "vector::publish -i input [-o output=input] [-e epsg=2154] -l login -p password -u url -w workspace -s datastore -g pg_datastore -b db -d dbuser [-v]"
  }

  #echo if verbose=1
  echo_ifverbose() {
    if [ $verbose ]; then echo "$@"; fi
  }

  xpath() {
  local xp=$1
  local xml=$2
  echo $(xmllint --xpath "${xp}" "${xml}" - <<<"$xml" 2>/dev/null)
  # redirige l'erreur standard vers null pour éviter d'être averti de chaque valeur manquante (XPath set is empty)
  # mais cela peut empêcher de détecter d'autres erreurs
  # TODO: faire tout de même un test, une fois sur le fichier, de la validité du xml
  }

  local DIR
  # chemin du script (sourcé ou non) pour pouvoir appeler d'autres scripts dans le même dossier
  DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
  # echo "BASH_SOURCE:${BASH_SOURCE[0]}"
  # echo "DIR:$DIR"
  # http://stackoverflow.com/questions/59895/can-a-bash-script-tell-what-directory-its-stored-in
  #readonly DIR

  # pour générer un nom lisible et simplifier
  # $(util::cleanName "./tic/tac toe.shp") -> tac_toe.shp #takes a filepath and returns a pretty name
  source "$DIR/util.sh"

  local input output epsg login password url workspace datastore verbose
  local OPTIND opt
  while getopts "i:o:e:l:p:u:w:s:g:t:b:d:vh" opt; do
    # le : signifie que l'option attend un argument
    case $opt in
      i) input=$OPTARG ;;
      o) output=$OPTARG ;;
      e) epsg=$OPTARG ;;
      l) login=$OPTARG ;;
      p) password=$OPTARG ;;
      u) url=$OPTARG ;;
      w) workspace=$OPTARG ;;
      s) datastore=$OPTARG ;;
      g) pg_datastore=$OPTARG ;;
      t) dbhost=$OPTARG ;;
      b) db=$OPTARG ;;
      d) dbuser=$OPTARG ;;
      v) verbose=1 ;;
      *) usage ;;
    esac
  done
  shift $((OPTIND-1))

  # vérification des paramètres
  if  [ ! "$input" ]; then
    echoerror "input missing"
    usage
    return 1 # erreur
  fi
  if [ ! "$login" ]; then
    echoerror "login missing"
    usage
    return 1 # erreur
  fi
  if [ ! "$password" ]; then
    echoerror "password missing"
    usage
    return 1 # erreur
  fi
  if [ ! "$url" ]; then
    echoerror "url missing"
    usage
    return 1 # erreur
  fi
  if [ ! "$workspace" ]; then
    echoerror "workspace missing"
    usage
    return 1 # erreur
  fi
  if [ ! "$datastore" ]; then
    echoerror "datastore missing"
    usage
    return 1 # erreur
  fi

  # valeurs des paramètres par défaut

  if [ ! "$output" ]; then
    # filename correspondant à l'$input par défaut "prettyfied"
    # $(util::cleanName "./tic/tac toe.shp") -> tac_toe.shp
    output=$(util::cleanName "$input" -p)
  fi
  echo_ifverbose "INFO $output"

  if [ ! "$epsg" ]; then
    # Lambert 93 par défaut
    epsg="2154"
  fi

  # teste si le fichier shapefile en $input existe
  # si le fichier n'existe pas, alors quitter
  if [ ! -f "$input" ]; then 
    echoerror "le fichier n'existe pas : $input"
    return 1 # erreur
  fi

  local statuscode=0

  # crée un dossier temporaire et stocke son chemin dans une variable
  local tmpdir=~/tmp/geosync_vector

  # supprime le dossier temporaire et le recrée
  rm -R "$tmpdir"
  mkdir -p "$tmpdir"
  #tmpdir=$(mktemp --directory /tmp/geoscript_vector_XXXXXXX) # !!! does NOT work as file://$tmpdir becomes file:/tmp instead of file:///tmp

  # détecte l'encoding, indépendamment de la casse
  # si présence d'un fichier .cpg
  #  alors prendre le contenu du fichier .cpg
  # sinon, si présence d'un .dbf
  #  détecter l'encoding du fichier .dbf
  local filenameext=$(basename "$input") # /path/t.o/file/vecteur.foo.shp -> vecteur.foo.shp
  local filename=${filenameext%%.*}  # vecteur.foo.shp -> vecteur  
  local filepath=$(dirname "$input") # relative/path/to/file/vecteur.shp -> relative/path/to/file
   
  echo "filename $filename"
  echo "filepath $filepath"
  
  # vérifie la présence des 2 fichiers .shx et .prj qui sont nécessaires en plus du .shp
  shx_found=($(find "${filepath}" -maxdepth 1 -iname "${filename}.shx"))
  if [ ${#shx_found[@]} -eq 0 ]; then  # ne pas utiliser [ -n $cpg_found ] comme c'est un array
    echo "ERROR le fichier ${filename}.shx n'a pas été trouvé. Interruption de la publication de ${input}"
    echoerror "le fichier .shx n'a pas été trouvé : ${filename}.shx"
    return 1 # erreur
  fi
  prj_found=($(find "${filepath}" -maxdepth 1 -iname "${filename}.prj"))
  if [ ${#prj_found[@]} -eq 0 ]; then  # ne pas utiliser [ -n $cpg_found ] comme c'est un array
    echo "ERROR le fichier ${filename}.prj n'a pas été trouvé. Interruption de la publication de ${input}"
    echoerror "le fichier .prj n'a pas été trouvé : ${filename}.prj"
    return 1 # erreur
  fi

  encoding="UTF-8"
  conv2utf8=""
  
  # retrouve le(s) fichier(s) cpg correspondant(s), indépendament de la casse, voir http://stackoverflow.com/questions/23356779/how-can-i-store-find-command-result-as-arrays-in-bash
  cpg_found=()
  while IFS=  read -r -d $'\0'; do
      cpg_found+=("$REPLY")
  done < <(find "${filepath}" -maxdepth 1 -iname "${filename}.cpg" -print0)
  #cpg_found=($(find "${filepath}" -maxdepth 1 -iname "${filename}.cpg")) ## Attention : ne marche pas avec des espaces dans le chemin/nom
  if [ ${#cpg_found[@]} -gt 0 ]; then  # ne pas utiliser [ -n $cpg_found ] comme c'est un array
    echo_ifverbose "INFO cpg existe : ${cpg_found[0]}"
  	encoding=$(cat "${cpg_found[0]}") # contenu du fichier .cpg, par exemple "UTF-8"
  else
	  echo_ifverbose "INFO cpg n'existe PAS"
    # retrouve le(s) fichier(s) dbf correspondant(s), indépendament de la casse, voir http://stackoverflow.com/questions/23356779/how-can-i-store-find-command-result-as-arrays-in-bash
    dbf_found=()
    while IFS=  read -r -d $'\0'; do
        dbf_found+=("$REPLY")
    done < <(find "${filepath}" -maxdepth 1 -iname "${filename}.dbf" -print0)
    #dbf_found=($(find "${filepath}" -maxdepth 1 -iname "${filename}.dbf")) ## Attention : ne marche pas avec des espaces dans le chemin/nom
    if [ ${#dbf_found[@]} -gt 0 ]; then # ne pas utiliser [ -n $dbf_found ] comme c'est un array
      echo_ifverbose "INFO dbf existe : ${dbf_found[0]}"
      #exemple de sortie de dbview foo.dbf | file -i -
      #/dev/stdin: text/plain; charset=iso-8859-1
      cmd="dbview \"${dbf_found[0]}\" | file -i - | cut -d= -f2"  # charset du fichier .dbf, par exemple "ISO-8859-1" ou encore "UTF-8"
      echo_ifverbose "INFO ${cmd}"
      encoding=$(eval ${cmd})

      # conversion en utf-8 en considérant que unknown serait du latin1
      if [[ $encoding == unknown* ]]; then
        conv2utf8="| iconv -f latin1 -t utf-8"
      fi
      # us-ascii est convertit en utf-8
      if [[ $encoding == us-ascii ]]; then
        conv2utf8="| iconv -f us-ascii -t utf-8"
      fi
      # conversion systématique iso-8859-1 en utf-8
      if [[ $encoding == iso-8859-1 ]]; then
        conv2utf8="| iconv -f iso-8859-1 -t utf-8"
      fi
    else
	  echo_ifverbose "INFO dbf n'existe PAS"
	fi
  fi
   
  echo_ifverbose "INFO encoding $encoding"
  echo_ifverbose "INFO conversion : $conv2utf8"
  
  # ----------------------------- PUBLICATION POSTGIS -------------
  # publication en utilisant directement ogr2ogr, avec le driver pgdump
  # nécessaire car le nom d'une table postgres ne peut avoir de .
  layer=$(echo $output | cut -d. -f1) # TODO envisager de supprimer les points et non de prendre avant un point pour diminuer le risque de colision avec une autre table
  cmd="ogr2ogr -f PGDump --config PG_USE_COPY YES -lco SRID='${epsg}' -lco create_schema=off -lco GEOMETRY_NAME=geom -lco DROP_TABLE=IF_EXISTS -nln '${layer}' -nlt PROMOTE_TO_MULTI /vsistdout/ '${input}' ${conv2utf8} | perl -p -e 's/VARCHAR\(\d+\)/VARCHAR/' - | psql -h '${dbhost}' -d '${db}' -U '${dbuser}' -w -f -"
  echo_ifverbose "INFO ${cmd}"
  result=$(eval ${cmd})

  # récupére la couche si elle existe
  echo_ifverbose "INFO vérifie l'existence du vecteur ${layer}"
  cmd="curl --silent -w %{http_code} \
            -u '${login}:${password}' \
            -XGET '${url}/geoserver/rest/workspaces/${workspace}/datastores/${pg_datastore}/featuretypes/${layer}.xml'"
  echo_ifverbose "INFO ${cmd}"

  result=$(eval ${cmd}) # retourne le contenu de la réponse suivi du http_code (attention : le contenu n'est pas toujours en xml quand demandé surtout en cas d'erreur; bug geoserver ?)
  statuscode=${result:(-3)} # prend les 3 derniers caractères du retour de curl, soit le http_code
  echo_ifverbose "INFO statuscode=${statuscode}"
  content=${result:0:-3} # prend tout sauf les 3 derniers caractères (du http_code)

  if [[ "${statuscode}" -ge "200" ]] && [[ "${statuscode}" -lt "300" ]]; then
    echo "YES le vecteur ${layer} existe déjà"
    return 0 # TODO envisager de mettre à jour si besoin, voire de supprimer et publier de nouveau
  elif [[ "${statuscode}" == "404" ]]; then
    echo_ifverbose "INFO le vecteur ${layer} n'existe pas encore"
    # continue pour publier la couche
  else
    echoerror "ERROR vérification de l'existence du vecteur ${layer} échouée... error http code : ${statuscode}"
    echoerror "      message : ${content}"
    echoerror "${cmd}"
    echo "ERROR vérification de l'existence du vecteur ${layer} échouée (${statuscode})"
    return 1 #erreur
  fi

  # publication des données sur geoserver
  # doc : http://docs.geoserver.org/stable/en/user/rest/api/featuretypes.html
  echo_ifverbose "INFO publication du vecteur ${layer} dans le datastore (PostGIS) ${pg_datastore}"
  cmd="curl --silent -w %{http_code} \
            -u '${login}:${password}' \
            -XPOST -H 'Content-type: text/xml' \
            -d '<featureType><name>${layer}</name></featureType>' \
            ${url}/geoserver/rest/workspaces/${workspace}/datastores/${pg_datastore}/featuretypes 2>&1"
  echo_ifverbose "INFO ${cmd}"

  result=$(eval ${cmd}) # retourne le contenu de la réponse suivi du http_code (attention : le contenu n'est pas toujours en xml quand demandé surtout en cas d'erreur; bug geoserver ?)
  statuscode=${result:(-3)} # prend les 3 derniers caractères du retour de curl, soit le http_code
  echo_ifverbose "INFO statuscode=${statuscode}"
  content=${result:0:-3} # prend tout sauf les 3 derniers caractères (du http_code)

  if [[ "${statuscode}" -ge "200" ]] && [[ "${statuscode}" -lt "300" ]]; then
    echo "OK publication du vecteur ${layer} réussie"
  else
    echoerror "ERROR publication du vecteur ${layer} échouée... error http code : ${statuscode}"
    echoerror "      message : ${content}"
    echoerror "${cmd}"
    echo "ERROR publication du vecteur ${layer} échouée (${statuscode})"
  fi

  # l'assignation d'un style est faite ailleurs

}

main() {
  usage
  exit
} #end of main

# if this script is a directly call as a subshell (versus being sourced), then call main()
if [ "${BASH_SOURCE[0]}" == "$0" ]; then
  main "$@"
fi

# source d'inpiration pour le style du code bash https://google-styleguide.googlecode.com/svn/trunk/shell.xml
# outil pour vérifier la qualité du code : http://www.shellcheck.net/

