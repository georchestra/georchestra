Publier des styles (fichier.sld) sur GeOrchestra et les associer aux couches prévues
--

#### Préparation du style :

Les styles du SLD cookbook de geoserver (http://docs.geoserver.org/stable/en/user/styling/sld/cookbook/)  peuvent être utilisés. Attention cependant à bien : cliquer sur "View and download the full SLD" puis à l'enregistrer en faisant clic-droit.

En utilisant cURL et GeOrchestra 15.06, les styles contenant le namespace "se:" sont mal publiés. Ils devraient cependant être pris en charge avec geoserver 2.8. Il faudra alors changer la 2ème requête cURL correspondant à l'envoi du fichier.sld :

	curl $var_v -w %{http_code} \
	                   -u ${login}:${password} \
	                   -XPUT -H 'Content-type: application/vnd.ogc.sld+xml' \
	                   -d @/home/georchestra-ouvert/owncloudsync/$input \
	            $url/geoserver/rest/styles/$output 2>&1
XPUT -H 'Content-type: application/vnd.ogc.**sld**+xml' => XPUT -H 'Content-type: application/vnd.ogc.**se**+xml'

#### Publication du style :

La publication d'un style s'effectue en deux étapes :

*** Création du style vide, sans attributs***
Le style apparait bien dans l'interface web de Geoserver et le fichier associé style2.xml est bien créé dans le répertoire des styles de Geoserver.

		curl -v ${login}:${password} -XPOST -H "Content-type: text/xml" -d "<style><name>style2</name><filename>style2.sld</filename></style>" $url/geoserver/rest/styles

*** Chargement des attributs du styles à partir d'un fichier local***
Les attributs du style sont chargés dans l'interface web de Geoserver et le fichier style2.sld est créé dans le répertoire des styles de Geoserver.

		curl -v -u ${login}:${password} -XPUT -H "Content-type: application/vnd.ogc.sld+xml"  -d @/home/smollard/ownCloud/style2.sld $url/geoserver/rest/styles/style2


#### Obtention des caractéristiques d'un style :

	curl -v -u ${login}:${password} -XGET $url/geoserver/rest/styles/nom_style.sld

#### Assignation d'un style à une couche :

	curl -v -u ${login}:${password} -XPUT -H "Content-type: text/xml" -d "<layer><defaultStyle><name>style2</name></defaultStyle></layer>"  $url/geoserver/rest/layers/geosync:baies_anthony_concate__haies_anthony_concate

#### Obtention de la liste des styles :

	curl --silent -u ${login}:${password} -XGET $url/geoserver/rest/styles

#### Intégration de la publication de styles dans Geosync

***Pour qu'un style soit associé à une couche :***

Nom du style : nom_couche.sld
Nom de la couche : nom_couche.shp

***Publication du style***

La publication d'un nouveau style se fait via l'appel du script style.sh. 

***Association style / couche***

L'association peut s'effectuer de deux façons :

- A la publication d'une nouvelle couche. Dans ce cas, l'association s'effectue par le script vector.sh en récupérant la liste des styles publiés et en regardant dans celle-ci si un style ne peut pas être associé à la nouvelle couche.

- A la publication d'un nouveau style. Dans ce cas, l'association s'effectue par le script style.sh en récupérant la liste des couches publiées et en regardant dans celle-ci si des couches peuvent êtres associées au nouveau style.



