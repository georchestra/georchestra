#!/bin/bash

# takes a filepath and returns a pretty name
# examples
# $(util::cleanName "./tic/tac toe.shp") -> tac_toe.shp
# $(util::cleanName "./tic/tac toe.shp" -p) -> tic_tac_toe.shp
util::cleanName() { 
  local path="$1"
  local option="$2"
  local result
  local base
  local extension
  local length_base
  local match repl

  # par défaut, ne prend que le nom du fichier
  # avec l'option -p, prend tout le chemin
  if [ "$option" == "-p" ]; then  # par défaut
    match=" "
    repl="_"
    path=${path//$match/$repl}
    match="-"
    repl="_"
    path=${path//$match/$repl}
    result=$(dirname $path)
    result=${result##*/}            # supprime les caractères devant le dernier / 
    local name_result
    name_result=$(basename "$path")
    local space 
    space="__"
    result="$result$space$name_result"
  else
    result=$(basename "$path")
    match=" "
    repl="_"
    result=${result//$match/$repl}
    match="-"
    repl="_"
    result=${result//$match/$repl}
  fi

  # replaces all uppercases by lowercases
  result=${result,,}

  # si le nom est trop long, on tronque la partie sans extension
  # voir https://math-linux.com/linux-2/bash/article/comment-extraire-le-nom-et-l-extension-d-un-fichier-en-bash
  # le fichier est du type base.extension
  base=${result%%.*}
  extension=${result#*.}
  length_base=${#base}
  if [ "$length_base" -ge "57" ]; then
    base=${base: -57}
  fi 

  # le resultat est la concaténation de base et extension
  result="${base}.${extension}"
  echo "$result"
}

# récupére la date de changement la plus récente des fichiers (de même nom) de la couche, exemple .shp.xml
# $(util::getlastchangedate "./tic/tac.shp") -> 2015-05-04 16:14:10.063284127 +0200
# attention cela diffère de la date de modification
# le rsync la modifie à l'heure locale lorsque le fichier est a été modifié
# tandis que la date de modification dépend de l'heure de l'OS (machine) sur lequel le fichier a été modifié
util::getlastchangedate() { 
  local filepath="$1"
  local datemodif

  # récupère la date de changement du fichier
  # attention cela diffère de la date de modification
  # le rsync la modifie à l'heure locale lorsque le fichier est a été modifié
  datemodif=$(stat --printf=%z "$filepath")
  # %z date du dernier changement au format lisible
  # $ stat --printf=%z ./owncloudsync/clementData/clement/GPS/Point_ge.shp
  # 2015-05-04 16:14:10.063284127 +0200
  # %Z date  du  dernier changement en secondes depuis le temps zéro de l'ordinateur
  # $ stat --printf=%Z ./owncloudsync/clementData/clement/GPS/Point_ge.shp
  # 1430748850
  
  # récupére la date la plus récente des changements des fichiers de la couche (de même nom que le shape) exemple .shp.xml
  # pour resynchroniser la couche si par exemple les métadata ont été modifiées
  # ou encore que des dépendances de la couche (ex:.shx) ont été partagées au synchroniseur seulement après la synchronisation de la couche (qui a due échouer)
  local p filenamesansext datefile
  p=$(dirname "$filepath")
  filename=$(basename "$filepath")
  filenamesansext=${filename%%.*} # layer.shp.xml -> layer
  #pour chaque fichier portant le même nom que la couche dans le même dossier...
  for file in "${p}/${filenamesansext}."*; do
  # NB: éviter find dans une boucle car non fiable, voir https://github.com/koalaman/shellcheck/wiki/SC2044
    datefile=$(stat --printf=%z "$file")
    if [ "$datefile" \> "$datemodif" ]; then
      datemodif=$datefile
    fi
  done

  echo "$datemodif"
}

# renvoie la première chaine non nulle (sinon "")
# utile pour définir une valeur par défaut parmi plusieurs
# $(util::takefirstdefinedvalue $mynotemptyvar "default1" "default2") -> $mynotemptyvar
# $(util::takefirstdefinedvalue $myemptyvar "default1" "default2") -> default1
# $(util::takefirstdefinedvalue $myemptyvar $empty "default2") -> default2
util::takefirstdefinedvalue() {
  local result=""
  # parcours les arguments donnés...
  for val in "$@"; do
      # si une chaine est non vide (not empty) alors la renvoie
      # donc renvoie la première valeur non vide
      if [ ! -z val ]; then
        result=$val
        break
      fi
  done
  echo "$result"
}

# renvoie le type de layer dont il s'agit : raster (tif,png,adf,jpg,ecw) ou vector (shp)
# $(typeoflayer "./tic/tac.shp") -> vector
# $(typeoflayer "./tic/tac.tif") -> raster
util::typeoflayer() {
  local filepath="$1"
 
  local result="unknown"
  local filename extension
  filename=$(basename "$filepath")
  extension="${filename##*.}" # layer.shp.xml -> xml  || layer.tif -> tif  # ! sans "."
  readonly extension
  
  shopt -s nocasematch
  case $extension in
    shp) result="vector" ;;
    tif|png|adf|jpg|ecw) result="raster" ;;
    sld) result="style" ;;
    xml) result="metadata" ;;
  esac
  shopt -u nocasematch

  echo "$result"
}

