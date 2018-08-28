#!/bin/bash
#
# Ajoute un résumé des metadata d'un .shp à une couche dans un geoserver
# à partir des metadata dans les fichiers vecteur.shp.xml et vecteur.xml

usage() { 
  echo "==> usage : "
  echo "source /lib/metadata_2_gs.sh"
  echo "metadata_geoserver::publish -i path/shapefile.shp [-o output=shapefile] -l login -p password -u url -w workspace -s datastore -g pg_datastore [-v]"
  } 
  
metadata_geoserver::publish() {

  echoerror() {
    echo "[$(date +'%Y-%m-%dT%H:%M:%S%z')]: $@" >&2  #Redirection vers la sortie d'erreurs standard  (stderr)
  } 

  usage() {
    echoerror "metadata_geoserver::publish -i path/shapefile.shp [-o output=shapefile] -l login -p password -u url -w workspace -s datastore -g pg_datastore [-v]"
  }

  #echo if verbose=1
  echo_ifverbose() {
    if [ $verbose ]; then echo "$@"; fi
  }

  local DIR
  # chemin du script pour pouvoir appeler d'autres scripts dans le même dossier
  DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
  #readonly DIR

  # pour générer un nom lisible et simplifier
  # $(util::cleanName "./tic/tac toe.shp") -> tac_toe.shp #takes a filepath and returns a pretty name
  source "$DIR/util.sh"

  local input output login password url workspace datastore verbose
  local OPTIND opt
  while getopts "i:o:l:p:u:w:s:g:v" opt; do
    # le : signifie que l'option attend un argument
    case $opt in
      i) input=$OPTARG ;;
      o) output=$OPTARG ;;
      l) login=$OPTARG ;;
      p) password=$OPTARG ;;
      u) url=$OPTARG ;;
      w) workspace=$OPTARG ;;
      s) datastore=$OPTARG ;;
      g) pg_datastore=$OPTARG ;;
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

  # par défault output prend la valeur du nom du fichier source sans son extension
  # tic/tac/toe.shp.xml -> toe
  if  [ ! "$output" ]; then
    #filenameext=$(basename "$input")
    #filename=${filenameext%%.*}
    #output=$filename
    output=$(util::cleanName "$input" -p)
  fi

  # teste si le fichier (xml) existe
  # si le fichier n'existe pas, alors continue quand même pour publier les données par défaut 
  if [ ! -f "$input" ] ; then 
    echoerror "le fichier n'existe pas : $input"
    # continue quand même pour publier les données par défaut  #return 1 # erreur 
  fi

  # retourne la valeur de l'expression xpath évalue sur le fichier $input
  # ex: $(xpath "/metadata/dataIdInfo/idCitation/date/pubDate/text()") -> 2015-01-29T00:00:00
  xpath() { 
    local xp=$1 
    local filexml=$2 
    echo $(xmllint --xpath "$xp" "$filexml" 2>/dev/null )
    # redirige l'erreur standard vers null pour éviter d'être averti de chaque valeur manquante (XPath set is empty)
    # mais cela peut empêcher de détecter d'autres erreurs
    # TODO: faire tout de même un test, une fois sur le fichier, de la validité du xml
  } 

  get_xml_value() {
    filexml=$2
    pathx='xpath string('$1')'
    setns1='setns gmd=http://www.isotc211.org/2005/gmd'
    setns2='setns gco=http://www.isotc211.org/2005/gco'
    xmllint --xinclude --shell $filexml <<CMD
$setns1
$setns2
$pathx
CMD
  }

  local filenameext=$(basename "$input") # /path/t.o/file/vecteur.foo.shp -> vecteur.foo.shp
  local filename=${filenameext%%.*}  # vecteur.foo.shp -> vecteur  
  local filepath=$(dirname "$input") # relative/path/to/file/vecteur.shp -> relative/path/to/file

  local files_found=()

  ## récupére le chemin du fichier vecteur.xml (insensible à la casse)
  local xml_path="";
  files_found=()
  while IFS=  read -r -d $'\0'; do
      files_found+=("$REPLY")
  done < <(find "${filepath}" -maxdepth 1 -iname "${filename}.xml" -print0)
  #cpg_found=($(find "${filepath}" -maxdepth 1 -iname "${filename}.cpg")) ## Attention : ne marche pas avec des espaces dans le chemin/nom
  if [ ${#files_found[@]} -gt 0 ]; then  # ne pas utiliser [ -n $cpg_found ] comme c'est un array
    xml_path="${files_found[0]}"
  fi
  echo_ifverbose "INFO xml_path=${xml_path}"


  ## récupére le chemin du fichier vecteur.shp.xml (insensible à la casse)
  local shpxml_path="";
  files_found=()
  while IFS=  read -r -d $'\0'; do
      files_found+=("$REPLY")
  done < <(find "${filepath}" -maxdepth 1 -iname "${filename}.shp.xml" -print0)
  #cpg_found=($(find "${filepath}" -maxdepth 1 -iname "${filename}.cpg")) ## Attention : ne marche pas avec des espaces dans le chemin/nom
  if [ ${#files_found[@]} -gt 0 ]; then  # ne pas utiliser [ -n $cpg_found ] comme c'est un array
    shpxml_path="${files_found[0]}"
  fi
  echo_ifverbose "INFO shpxml_path=${shpxml_path}"

  # récupére (avec xpath) les métadonnées au format INSPIRE d'ArcGIS depuis le .shp.xml
  # si le titre, le résumé, l'auteur ou la date de publication ne sont pas trouvés avec leurs balises au format INSPIRE 
  # ils sont recherchés avec leurs balises au format ISO 19139 (Esri ArcGIS)
  # ils sont alors recherchés avec leurs balises ISO19139 de QSphere (QGIS)
  # s'ils ne sont pas du tout renseignés dans le xml alors ils sont remplacés par une variante
  # local metadata
  local title
  local abstract
  local origin
  local pubdate

  # if [[ -f "$shpxml_path" ]]; then
  #   metadata=$(xpath "//text()" "$shpxml_path")
  # fi

  if [[ -f "$shpxml_path" ]] && [[ ! "$title" ]]; then
    title=$(xpath "/metadata/dataIdInfo/idCitation/resTitle/text()" "$shpxml_path") 
  fi
  if [[ -f "$shpxml_path" ]] && [[ ! "$title" ]]; then
    title=$(xpath "/metadata/idinfo/citation/citeinfo/title/text()" "$shpxml_path") 
  fi
  if [[ -f "$xml_path" ]] && [[ ! "$title" ]]; then
    xpathquery='/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString'
    title=$(get_xml_value "$xpathquery" "$xml_path")
    title=`echo $title | grep -o -P '(?<=: ).*(?= / )'`
  fi
  if [ ! "$title" ]; then          
    title=$(basename "$output" .shp)
  fi

  if [[ -f "$shpxml_path" ]] && [[ ! "$abstract" ]]; then
    abstract=$(xpath "/metadata/dataIdInfo/idAbs/text()" "$shpxml_path")
  fi
  if [[ -f "$shpxml_path" ]] && [[ ! "$abstract" ]]; then
    abstract=$(xpath "/metadata/idinfo/descript/abstract/text()" "$shpxml_path")
  fi
  if [[ -f "$xml_path" ]] && [[ ! "$abstract" ]]; then
    xpathquery='/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString'
    abstract=$(get_xml_value "$xpathquery" "$xml_path")       
    abstract=`echo $abstract | grep -o -P '(?<=: ).*(?= / )'`
  fi
  if [ ! "$abstract" ]; then
    abstract="A compléter!"
  fi

  if [[ -f "$shpxml_path" ]] && [[ ! "$origin" ]]; then
    origin=$(xpath "/metadata/dataIdInfo/rpIndname/text()" "$shpxml_path")
  fi
  if [[ -f "$shpxml_path" ]] && [[ ! "$origin" ]]; then
    origin=$(xpath "/metadata/idinfo/citation/citeinfo/origin/text()" "$shpxml_path")
  fi
  if [[ -f "$xml_path" ]] && [[ ! "$origin" ]]; then
    xpathquery='/gmd:MD_Metadata/gmd:contact/gmd:CI_ResponsibleParty/gmd:organisationName/gco:CharacterString'
    origin=$(get_xml_value "$xpathquery" "$xml_path")  
    origin=`echo $origin | grep -o -P '(?<=: ).*(?= / )'`
  fi
  if [ ! "$origin" ]; then
    origin="A compléter!"
  fi

  if [[ -f "$shpxml_path" ]] && [[ ! "$pubdate" ]]; then
    pubdate=$(xpath "/metadata/dataIdInfo/idCitation/date/pubDate/text()" "$shpxml_path")
  fi
  if [[ -f "$shpxml_path" ]] && [[ ! "$pubdate" ]]; then
    pubdate=$(xpath "/metadata/idinfo/citation/citeinfo/pubdate/text()" "$shpxml_path")
  fi
  if [[ -f "$xml_path" ]] && [[ ! "$pubdate" ]]; then
    xpathquery='/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:Date'
    pubdate=$(get_xml_value "$xpathquery" "$xml_path")  
    pubdate=`echo $pubdate | grep -o -P '(?<=: ).*(?= / )'`
  fi
  if [ ! "$pubdate" ]; then
    pubdate="A compléter!"
  fi

  ## --------- Vecteurs issus du datastore PostGIS  --------------

  # attention : spécifier le shp concerné en fin d'url
  # http://docs.geoserver.org/2.6.x/en/user/rest/api/datastores.html#workspaces-ws-datastores-ds-file-url-external-extension

  local layer=${output}

  echo_ifverbose "INFO publication d'un résumé des metadata de ${layer} dans le geoserver dans le datastore (PostGIS) ${pg_datastore}"
  cmd="curl --silent -w %{http_code} \
            -u '${login}:${password}' \
            -H 'Content-type: text/xml' \
            -d '<featureType><title>${title}</title>
<abstract>${abstract}
Auteur : ${origin}
Date de production de la donnée : ${pubdate}
Chemin : ${input}</abstract>
<enabled>true</enabled>
<description></description></featureType>' \
            -XPUT '${url}/geoserver/rest/workspaces/${workspace}/datastores/${pg_datastore}/featuretypes/${layer}'"
  echo_ifverbose "INFO ${cmd}"
  # <enabled>true</enabled><advertised>true</advertised> est nécessaire pour éviter que la couche ne soit pas dépubliée (car par défaut "enabled" est mis à false lors d'un update
  # pour rajouter des mots clés
  # ...</description><keywords><string>my_keyword1</string><string>my_keyword2</string></keywords></featureType>

  result=$(eval ${cmd}) # retourne le contenu de la réponse suivi du http_code (attention : le contenu n'est pas toujours en xml quand demandé surtout en cas d'erreur; bug geoserver ?)
  statuscode=${result:(-3)} # prend les 3 derniers caractères du retour de curl, soit le http_code
  echo_ifverbose "INFO statuscode=${statuscode}"
  content=${result:0:-3} # prend tout sauf les 3 derniers caractères (du http_code)

  if [[ "${statuscode}" -ge "200" ]] && [[ "${statuscode}" -lt "300" ]]; then
      echo "OK publication des metadata de ${layer} dans le geoserver réussie"
  else
    echoerror "ERROR publication des metadata de ${layer} dans le geoserver échouée... error http code : ${statuscode}"
    echoerror "      message : ${content}"
    echoerror "${cmd}"
    echo "ERROR publication des metadata de ${layer} dans le geoserver échouée (${statuscode})"
  fi


} #end of importmetadata()

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

