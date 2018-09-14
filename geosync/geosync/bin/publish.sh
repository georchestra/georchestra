#!/bin/bash

usage() { 
  program=$(basename "$0") 
  echo "==> usage :"
  echo "$program [-i inputpath=.] [-o output] [-d datapath=.] [-p paramfile=./.geosync.conf] -w workspace -s datastore [-c coveragestore] [-e epsg] [-v]"
  echo "$program -i 'directory of vectors/rasters' [-d datapath=.] [-p paramfile=./.geosync.conf] -w workspace -s datastore [-e epsg] [-v]"
  echo "$program -i vector.shp [-p paramfile=./.geosync.conf] -w workspace -s datastore -g pg_datastore -b db [-e epsg] [-v]"
  echo "$program -i raster.tif|png|adf|jpg|ecw [-p paramfile=./.geosync.conf] -w workspace -c coveragestore [-e epsg] [-v]"
  echo ""
  echo "Publie les couches (rasteurs, vecteurs) dans le geoserver depuis le dossier donné ([input]) ou sinon courant et ses sous-dossiers"
  echo ""
  echo "le login, mot de passe et l'url du geoserver doivent être dans un fichier (par défaut, .geosync.conf dans le même dossier que ce script)"
} 

echoerror() {
  echo "[$(date +'%Y-%m-%dT%H:%M:%S%z')]: $@" >&2  #Redirection vers la sortie d'erreurs standard  (stderr)
} 

error() {
  echoerror "$@"
  exit 1
}

# importe les vecteurs et rasteurs du dossier (path) et sous-dossiers
# écrit dans un fichier dans le datapath la date de changement la plus récente des fichiers indexés
importallfiles() {
  local path="$1"
  shift #consomme l'argument du tableau des arguments, pour pouvoir récupérer le reste dans "$@"
  local datapath="$1"
  shift #consomme l'argument du tableau des arguments, pour pouvoir récupérer le reste dans "$@"

  local lastdatemodif=0
  local newlastdatemodif

  # si datapath n'est pas un dossier existant alors on le créée
  if  [ ! -d "$datapath" ]; then
    echo "creation de datapath : $datapath"
    mkdir -p $datapath
  fi

  # fichier dédié à stocker la valeur lastdatemodif, date de changement la plus récente des fichiers indexés
  configfile="$datapath/lastdate.txt"

  # teste si le fichier  temporaire stockant la date de modif la plus récente existe
  # si tel est le cas, alors la récupère
  # attention à bien écrire cette donnée dans un volume pérenne quand on utilise des conteneurs Docker
  # sinon, le système trouve une date et republie toutes les données
  if [ -f "$configfile" ]; then 
    lastdatemodif=$(cat "$configfile")
    echo "lastdatemodif : $lastdatemodif"
  fi
  # newlastdatemodif est la valeur qui sera stockée à la place de lastdatemodif
  newlastdatemodif=$lastdatemodif

  cd "$path"

  # shopt -s globstar
  shopt -s globstar nocaseglob
  # set globstar, so that the pattern ** used in a pathname expansion context will 
  # match a files and zero or more directories and subdirectories.
  # nocaseglob : case insensitive
  #shopt -s extglob allow (.tif|.jpg) but does not work with globstar **
  # TODO: format des rasters supportés: tif, png, adf, jpg, ecw

  # si des extensions sont rajoutées, alors penser à mettre à jour lib/util.sh util::typeoflayer()
  # ATTENTION si xml est ajouté cela peut entrainer la publication de metadata peut importe s'il y a une couche associée, ou si celle-ci a bien été publiée
  #           peut convenir si on souhaite pouvoir publier des metadata seules
  #           sinon TODO il faudrait envisager de lancer la publication des metadata seulement si la publication de la couche a réussie
  #                                       et de chercher les metadata disponibles voire d'en prendre par défaut pour qu'une couche sans metadata apparaisse tout de même au catalogue geonetwork
  for filepath in **/*.{shp,tif,png,jpg,ecw,sld,xml} **/w001001.adf; do
      # alternative dangereuse :
      # for filepath in $(find "$path" -iname "*.shp"); do
      # option -iname à find pour un filtre (-name) mais insensible à la casse (.SHP,.shp...)
    
      # teste si le fichier existe bien car (dans certaines conditions encore inconnues selon qu'on le lance par le terminal ou le cron)
      # **/*.shp retourne aussi un fichier nommé "**/*.shp"
      if [ -f "$filepath" ]; then 
         # récupère la date de changement la plus récente des fichiers (de même nom) de la couche, exemple .shp.xml
         # attention cela différe de la date de modification
         # le rsync la modifie à l'heure locale lorsque le fichier est a été modifié
         datemodif=$(util::getlastchangedate "$filepath")
         echo "datemodif : $datemodif"
    
         if [[ "$datemodif" > "$lastdatemodif" ]]; then  # [[ .. ]] nécessaire pour comparer les chaines avec >

            echo "$datemodif supérieure à $lastdatemodif, je lance l'import..."
            importfile "$filepath" ""
    
            # TODO: ne modifier la date que si l'import du fichier a été un succés
            if [[ "$datemodif" > "$newlastdatemodif" ]]; then # [[ .. ]] nécessaire pour comparer les chaines avec >
               newlastdatemodif=$datemodif
               echo "je positionne une nouvelle de date : $newlastdatemodif"
               echo "$newlastdatemodif" > "$configfile"
            fi
         fi
    
      fi
  done

  shopt -u globstar nocaseglob  # unset globstar
  
  #echo "je positionne une nouvelle de date : $newlastdatemodif"
  #echo "$newlastdatemodif" > "$configfile"

}

# import un fichier (couche) si le fichier ou ses dépendances (du même nom) ont une date de changement supérieure à la date donnée (0 pour toujours)
# convertit, publie les data, publie les metadata (TODO: paramètre les droits, publie le style)
# importfile ~/owncloud/Point.shp "2015-05-13 10:00:06.000000000 +0200"
# beware : modify newlastdatemodif
importfile() {

  local filepath="$1"
  shift #consomme l'argument du tableau des arguments, pour pouvoir récupérer le reste dans "$@"
  local outputlayername="$1"
  shift #consomme l'argument du tableau des arguments, pour pouvoir récupérer le reste dans "$@"
  echo "outputlayername : $outputlayername"

  local layertype="unknown"
  layertype=$(util::typeoflayer "$filepath")

  local layername
  #global newlastdatemodif

  vector() {
  
    #takes a filepath and returns a pretty name
    #examples
    # $(util::cleanName "./tic/tac toe.shp") -> tac_toe.shp
    # $(util::cleanName "./tic/tac toe.shp" -p) -> tic_tac_toe.shp
    if [ ! "$outputlayername" ]; then
      echo "filepath : $filepath"
      outputlayername=$(util::cleanName "$filepath" -p)
    fi

    # convertit et publie la couche pour postgis_data et shpowncloud
    cmd="vector::publish -i '$filepath' -o '$outputlayername' -l '$login' -p '$passwd' 
                         -u '$host' -w '$workspace' -s '$datastore' -g '$pg_datastore' -t '$dbhost' -b '$db' -d '$dbuser' -e '$epsg' $verbosestr"
    echo $cmd
    eval $cmd

    # publie un résumé de metadata même si le .xml n'existe pas (dans ce cas publie les données par défaut)
    cmd="metadata_geoserver::publish -i '$filepath' -o '$outputlayername' -l '$login' -p '$passwd' -u '$host' -w '$workspace' -s '$datastore' -g '$pg_datastore' $verbosestr"
    echo $cmd
    eval $cmd

  }

  raster() {

    # takes a filepath and returns a pretty name
    # examples
    # $(util::cleanName "./tic/tac toe.shp") -> tac_toe.shp
    # $(util::cleanName "./tic/tac toe.shp" -p) -> tic_tac_toe.shp
    if [ ! "$outputlayername" ]; then
      echo "filepath : $filepath"
      outputlayername=$(util::cleanName "$filepath" -p)
    fi

    cmd="raster::publish -i '$filepath' -o '$outputlayername' -l '$login' -p '$passwd' 
                         -u '$host' -w '$workspace' -c '$coveragestore' -e '$epsg' -t '$dbhost' -b '$db' -d '$dbuser' $verbosestr"
    echo $cmd
    eval $cmd

  }

  style() {

    if [ ! "$outputlayername" ]; then
      echo "filepath : $filepath"
      outputlayername=$(util::cleanName "$filepath" -p)
    fi 

    cmd="style::publish -i '$filepath' -o '$outputlayername' -l '$login' -p '$passwd' 
                        -u '$host' -w '$workspace' -s '$datastore' -g '$pg_datastore' $verbosestr"
    echo $cmd
    eval $cmd
  }

  # TODO prendre en compte les fichiers peut importe leur casse -> requiert de trouver les fichiers au lieu de tester leur existence
 
  metadata() {

    if [ ! "$outputlayername" ]; then
      echo "filepath : $filepath"
      outputlayername=$(util::cleanName "$filepath" -p)
    fi

    base_file=$(echo $filepath | cut -f1 -d.) # metadata.shp.xml => metadata / metadata.xml => metadata
    ext_file=$(echo $filepath | cut -f2 -d.)  # metadata.shp.xml => shp / metadata.xml => xml
    ext_file2=$(echo $filepath | cut -f3 -d.) # raster.tif.aux.xml => aux 
    test_xml_file="${path}/${base_file}.xml"

    echo "ext_file $ext_file"
    echo "ext_file2 $ext_file2"
    echo "test_xml_file $test_xml_file"

    if [ $ext_file == "shp" ] && [ -e $test_xml_file ]; then
      echo "fichier .shp.xml ignoré car un fichier .xml existe"
    elif [ $ext_file == "tif" ] && [ $ext_file2 == "aux" ]; then
      echo "fichier .aux.xml ignoré car il s'agit d'un fichier de projection/metadonnee"
    else
      # Attention : l'utilisateur (login) doit avoir le rôle GN_EDITOR (ou GN_ADMIN) (anciennement SV_EDITOR / SV_ADMIN) voir administration ldap
      cmd="python $BASEDIR/lib/metadata_2_gn.py -i '$filepath' -o '$outputlayername' -l '$login' -p '$passwd'
                -u '$host' -w '$workspace' -s '$datastore' --db_hostname '$dbhost' $verbosestr"
      echo $cmd
      eval $cmd
    fi  

  }

  # ici, en rajoutant metadata et style à la liste, la logique qui s'appliquait aux couches (vecteur ou rasteur) a été prolongée aux metadata et aux styles
  # s'il est en effet possible d'avoir des fiches de metadata dans le geonetwork sans data associées ou un style sans couche qui l'utilise
  # on peut s'interroger de savoir si c'est ce qu'on souhaite
  # sinon il faudrait à partir d'une couche vecteur ou rasteur, rechercher les styles ou metadata à publier, au lieu de les publier en tant que tels dès qu'on en trouve
  case $layertype in
  'vector') vector ;;
  'raster') raster ;;
  'style') style ;;
  'metadata') metadata ;;
  *) echoerror "file not supported : $filepath" ;;
  esac

  # TODO: retourner si succès ou non
  
}


main() {
  #chemin du script pour pouvoir appeler d'autres scripts dans le même dossier
  BASEDIR=$(dirname "$0")

  #local input output epsg datapath paramfile workspace datastore coveragestore verbose help
  local OPTIND opt
  while getopts "i:o:e:d:p:w:s:c:g:t:b:l:vh" opt; do
    # le : signifie que l'option attend un argument
    case $opt in
      i) input=$OPTARG ;;
      o) output=$OPTARG ;;
      e) epsg=$OPTARG ;;
      d) datapath=$OPTARG ;;
      p) paramfile=$OPTARG ;;
      w) workspace=$OPTARG ;; # Attention : pour l'instant la config écrase les paramètres passés à ce script (pg_datastore, dbhost, db, dbuser...)
      s) datastore=$OPTARG ;;
      c) coveragestore=$OPTARG ;;
      g) pg_datastore=$OPTARG ;;
      t) dbhost=$OPTARG ;;
      b) db=$OPTARG ;;
      l) dbuser=$OPTARG ;;
      v) verbose=1; verbosestr="-v" ;;
      h) help=1 ;;
  # si argument faux renvoie la sortie    
      \?) error "Option invalide : -$OPTARG" ;;
  # si option sans argument renvoie la sortie   
      :) error "L'option -$OPTARG requiert un argument." ;;
    esac
  done
  shift $((OPTIND-1))

  if [ "$help" ]; then
    usage
    exit
  fi

  # "paramfile" nom/chemin du fichier des paramètres de connexion
  # par défaut, prend le fichier .geosync.conf dans le dossier de ce script
  if [ ! "$paramfile" ]; then
    paramfile="$HOME/.geosync.conf"
  fi

  # teste l'existence du fichier contenant les paramètres de connexion
  if [ ! -f "$paramfile" ]; then 
    error "le fichier geosync.conf n'existe pas; le spécifier avec l'option -p [paramfile]"
  fi

  # récupère "host login passwd workspace datastore pg_datastore dbhost db logs" dans le fichier .geosync.conf situé dans le dossier de l'utilisateur
  # Attention : pour l'instant la config écrase les paramètres passés à ce script (pg_datastore, dbhost, db, dbuser...)
  local host login passwd workspace datastore pg_datastore dbhost db dbuser logs
  source "$paramfile"

  # attention le fichier .geosync.conf est interprété et fait donc confiance au code
  # pour une solution plus sûre envisager quelque chose comme : #while read -r line; do declare $line; done < "$HOME/.geosync.conf"

  # par défaut index le répertoire courant
  if [ ! "$input" ]; then
    # répertoire courant par défaut
    input="."
  fi

  # par défaut cherche le fichier contenant la dernière date de changement du fichier traité dans le répertoire courant
  if [ ! "$datapath" ]; then
    # par défaut
    datapath="."
  fi

  if  [ ! -e "$input" ]; then
    error "n'existe pas : input : $input"
  fi

  # vérification des paramètres passés (soit par argument, soit par le fichier .geosync.conf)
  if [ -z $host ] || [ -z $login ] || [ -z $passwd ] || [ -z $workspace ] || [ -z $datastore ] || [ -z $pg_datastore ] || [ -z $dbhost ] || [ -z $db ] || [ -z $dbuser ] || [ -z $logs ]; then
    echoerror "au moins un paramètre maquant !"
    usage
    exit
  fi

  # pour générer un nom lisible et simplifier pour fichier
  # $(util::cleanName "./tic/tac toe.shp") -> tac_toe.shp #takes a filepath and returns a pretty name
  source "$BASEDIR/lib/util.sh"
  # pour importer les vecteurs (couches shp)
  source "$BASEDIR/lib/vector.sh"
  # pour importer les rasteurs (couches .tif .adf .png .jpeg .ocw)
  source "$BASEDIR/lib/raster.sh"
  # pour importer un résumé de metadonnées des vecteurs dans le geoserver (la partie geonetwork est faite ailleurs)
  source "$BASEDIR/lib/metadata_2_gs.sh"
  # pour importer des fichiers de styles (fichiers .sld)
  source "$BASEDIR/lib/style.sh"


  newlastdatemodif=0

  # si c'est le chemin d'un répertoire alors indexe le répertoire
  if [ -d "$input" ]; then
    importallfiles "$input" "$datapath"

     # si c'est le chemin d'un fichier (couche) alors indexe le fichier
     elif [ -f "$input" ]; then
       importfile "$input" "$output"

  fi

} #end of main

# if this script is a directly call as a subshell (versus being sourced), then call main()
if [ "${BASH_SOURCE[0]}" == "$0" ]; then
  main "$@"
fi

# source d'inpiration pour le style du code bash https://google-styleguide.googlecode.com/svn/trunk/shell.xml
# outil pour vérifier la qualité du code : http://www.shellcheck.net/

