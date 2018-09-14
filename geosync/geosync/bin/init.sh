#!/bin/bash
# permet d'initialiser le workspace et ses datastores d'un geoserver 

usage() { 
  echo "Usage : init.sh [OPTION]"
  echo ""
  echo "Options"
  echo " -c     (create) crée le workspace et les datastores dès que le geoserver est disponible"
  echo " -t     (test) teste la disponibilité du geoserver"
  echo " -v     verbeux"  
  echo " (-h)   affiche cette aide"
  echo ""
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
  while getopts "ctvh" opt; do
    # le : signifie que l'option attend un argument
    case $opt in
      c) create=1 ;;
      t) test=1 ;;
      v) verbose=1 ;;
      h) help=1 ;;
  # si argument faux renvoie la sortie    
      \?) error "Option invalide : -$OPTARG" ;;
  # si option sans argument renvoie la sortie   
      :) error "L'option -$OPTARG requiert un argument." ;;
    esac
  done
  shift $((OPTIND-1))

  # vérification des paramètres
  
  # si rien n'est demandé, alors affiche l'aide
  if  [ ! "$test" ] && [ ! "$create" ]; then
    usage
    exit
  fi
  
  # teste la disponibilité du geoserver
  echo_ifverbose "teste la disponibilité du geoserver"

  # récupère les paramètres de connexion dans le fichier .geosync situé dans le même dossier utilisateur
  paramfile="$HOME/.geosync.conf"
  local host login passwd workspace workspace_roles datastore pg_datastore db 
  source "$paramfile"

  # attention les fichiers .geosync est interprété et fait donc confiance au code
  # pour une solution plus sûre, envisager quelque chose comme : #while read -r line; do declare $line; done < "$HOME/.pass"

  # vérification du host/login/mot de passe
  if [ ! "$login" ] || [ ! "$passwd" ] || [ ! "$host" ]; then
    error "url du georserver, login ou mot de passe non définit; le fichier spécifié avec l'option -p [paramfile] doit contenir la définition des variables suivantes sur 3 lignes : login=[login] passwd=[password] host=[geoserver's url]"
  fi

  # récupère les paramètres d'authentification dans le fichier .pgpass (attendu dans le $HOME)
  # on utilise awk mais il faudrait faire quelque chose de plus propre !!
  cmd="cat $HOME/.pgpass | grep :$db:"
  result=($(eval $cmd)) # nom_hote:port:database:nom_utilisateur:mot_de_passe 
  cmd="echo $result | awk -F':' '{print \$4}'"
  db_login=($(eval $cmd))
  cmd="echo $result | awk -F':' '{print \$5}'"
  db_passwd=($(eval $cmd))
  cmd="echo $result | awk -F':' '{print \$1}'"
  db_host=($(eval $cmd))
  echo_ifverbose "login : ${db_login}; password : ${db_passwd}; host : ${db_host}"

  # aide au diagnostique : vérifie la présence d'un host, login, password
  # si l'une des valeurs est vide, elle le sera lors de la création du datastore postgis (pg_datastore)
  # ce qui n'est peut-être pas désirée
  if [[ -z $db_host ]] || [[ -z $db_login ]] || [[ -z $db_passwd ]]; then
    msg="WARNING l'hôte, le login ou le mot de passe sont vides dans .pgpass pour la base PostGIS ($db); ceci peut être volontaire ou bien dûe à des erreurs, notamment dans le nom de la base"
    echoerror $msg
    echo $msg
  fi

  url=$host
  password=$passwd

  # boucle d'attente d'une réponse de geoserver
  statuscode=0
  until [ "$statuscode" -ge "200" ] && [ "$statuscode" -lt "300" ]; do
      echo_ifverbose "#est-ce que le geoserver répond positivement ?"
      cmd="curl --silent --output /dev/null -w %{http_code} -u '${login}:${password}' -XGET $url/geoserver/rest/workspaces"
      echo_ifverbose $cmd

      statuscode=$(eval $cmd)
	  echo_ifverbose "statuscode $statuscode"

      # si le code de la réponse http est dans l'intervalle [200,300[
      if [ "$statuscode" -ge "200" ] && [ "$statuscode" -lt "300" ]; then
        echo "OK connexion au geoserver réussie"
      else
        echoerror "geoserver not ready ? ... error http code : $statuscode"
        sleep 1
      fi  
  done

  # si la création des workspace/datastore n'a pas été demandée, alors quitte le script là
  if [ ! "$create" ]; then exit; fi
  
  
  # recherche du workspace
  echo_ifverbose "#est-ce que le workspace $workspace existe ?"
  cmd="curl --silent --output /dev/null -w %{http_code} -u '${login}:${password}' -XGET $url/geoserver/rest/workspaces/$workspace"
  echo_ifverbose $cmd

  statuscode=$(eval $cmd)
  echo_ifverbose "statuscode $statuscode"
  
  if [ "$statuscode" -eq "404" ]; then # not found
     echo_ifverbose "le workspace n'existe pas"
	 
     echo_ifverbose "création du workspace $workspace"
     cmd="curl --silent --output /dev/null -w %{http_code} -u '${login}:${password}' -XPOST -H 'Content-type: text/xml' 
               -d '<workspace><name>$workspace</name></workspace>'
          $url/geoserver/rest/workspaces"
     echo_ifverbose $cmd

     statuscode=$(eval $cmd)
	 echo_ifverbose "statuscode $statuscode"
     if [ "$statuscode" -ge "200" ] && [ "$statuscode" -lt "300" ]; then
        echo "OK création du workspace $workspace réussie"
     else
        echoerror "ERROR création du workspace $workspace ... error http code : $statuscode"
     fi
  elif [ "$statuscode" -ge "200" ] && [ "$statuscode" -lt "300" ]; then
	  echo "YES le workspace $workspace existe déjà"
  fi

  roles=$workspace_roles # récupéré de .geosync.conf, exemple : workspace_roles="ROLE_AUTHENTICATED,GROUP_ADMIN,ADMIN"
  
  if [[ $roles ]]; then # roles (accédant au worskpace) définits
    auths="${workspace}.*.r"

    echo_ifverbose "tentative de création des régles d'accès"
    cmd="curl --silent --output /dev/null -w %{http_code} -u '${login}:${password}' -XPOST -H 'Content-type: text/xml' \
              -d '<?xml version=\"1.0\" encoding=\"UTF-8\"?> \
                  <rules> \
                    <rule resource=\"$auths\">$roles</rule> \
                 </rules>' \
         $url/geoserver/rest/security/acl/layers.xml"
    echo_ifverbose $cmd

    statuscode=$(eval $cmd)
    echo_ifverbose "statuscode $statuscode"
    
    if [ "$statuscode" -ge "200" ] && [ "$statuscode" -lt "300" ]; then
       echo "OK création des régles d'accès réussie"
    else
      echoerror "ERROR lors de la création des régles d'accès ... error http code : $statuscode"
    fi 
  else  # aucun role accédant au worskpace de définit
    echo "WARNING aucun rôle définit dans .geosync.conf pouvant accéder au worskpace $workspace"
  fi
  
createDatastore() {
  local datastore_name=$1
  local datastore_xml=$2

  # recherche du datastore
  echo_ifverbose "#est-ce que le datastore $datastore_name existe ?"
  cmd="curl -silent --output /dev/null -w %{http_code} -u '${login}:${password}' -XGET $url/geoserver/rest/workspaces/$workspace/datastores/$datastore_name"
  echo_ifverbose $cmd
 
  statuscode=$(eval $cmd)
  echo_ifverbose "statuscode $statuscode"
  
  if [ "$statuscode" -eq "404" ]; then # not found
    echo_ifverbose "le datastore n'existe pas"
  
  echo_ifverbose "tentative de création du datastore"
    cmd="curl --silent --output /dev/null -w %{http_code} -u '${login}:${password}' -XPOST -H 'Content-type: text/xml' \
               -d '$datastore_xml' \
               $url/geoserver/rest/workspaces/$workspace/datastores"
  echo_ifverbose $cmd

  statuscode=$(eval $cmd)
  echo_ifverbose "statuscode $statuscode"
    
  if [ "$statuscode" -ge "200" ] && [ "$statuscode" -lt "300" ]; then
    echo "OK création du datastore $datastore_name réussie"
  else
    echoerror "ERROR lors de création du datastore $datastore_name... error http code : $statuscode"
  fi 
  elif [ "$statuscode" -ge "200" ] && [ "$statuscode" -lt "300" ]; then
    echo "YES le datastore $datastore_name existe déjà"
  fi
}


  createDatastore $datastore \
                   "<dataStore> 
                     <name>$datastore</name> 
                     <description>shp dans owncloud</description> 
                     <type>Directory of spatial files (shapefiles)</type> 
                     <enabled>true</enabled> 
                     <connectionParameters> 
                       <entry key=\"charset\">UTF-8</entry> 
                       <entry key=\"url\">file:data/$login/$datastore</entry> 
                       <entry key=\"enable spatial index\">true</entry> 
                       <entry key=\"cache and reuse memory maps\">true</entry> 
                     </connectionParameters> 
                   </dataStore>"

   
  createDatastore $pg_datastore \
                   "<dataStore> 
                     <name>$pg_datastore</name> 
                     <connectionParameters> 
                       <host>$db_host</host> 
                       <port>5432</port> 
                       <database>$db</database> 
                       <user>$db_login</user> 
                       <passwd>$db_passwd</passwd> 
                       <dbtype>postgis</dbtype> 
                     </connectionParameters>
                   </dataStore>"

} #end of main

# if this script is a directly call as a subshell (versus being sourced), then call main()
if [ "${BASH_SOURCE[0]}" == "$0" ]; then
  main "$@"
fi

