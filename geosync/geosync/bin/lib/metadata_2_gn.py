#!/usr/bin/python
# -*-coding:Utf-8 -*

# pré-requis : 
# apt install python-owslib python-lxml python-dev libxml2-utils libsaxonb-java

# objectif : envoyer le fichier de métadonnées xml au GeoNetwork
# on modifie le fichier xml à la volée pour lui ajouter des balises xml
#
# 1) pour faire apparaître le bouton "Visualiser" dans GeoNetwork, on ajoute les balises suivantes dans <gmd:MD_DigitalTransferOptions>
#
#  <gmd:distributionInfo>
#    <gmd:MD_Distribution>
#      <gmd:transferOptions>
#        <gmd:MD_DigitalTransferOptions>
#          <gmd:onLine>
#            <gmd:CI_OnlineResource>
#              <gmd:linkage>
#                <gmd:URL>https://georchestra-mshe.univ-fcomte.fr/geoserver/ows?SERVICE=WMS&amp;</gmd:URL>
#              </gmd:linkage>
#              <gmd:protocol>
#                <gco:CharacterString>OGC:WMS-1.3.0-http-get-map</gco:CharacterString>
#              </gmd:protocol>
#              <gmd:name>
#                <gco:CharacterString>geosync-restreint:NOM_DE_LA_COUCHE</gco:CharacterString>
#              </gmd:name>
#              <gmd:description>
#                <gco:CharacterString>NOM_DE_LA_COUCHE</gco:CharacterString>
#              </gmd:description>
#            </gmd:CI_OnlineResource>
#          </gmd:onLine>
#        </gmd:MD_DigitalTransferOptions>
#
# 2) on ajoute également un uuid au fichier, dans la balise gmd:fileIdentifier
#
#  <gmd:fileIdentifier><
#    gco:CharacterString>8349df1c-1ebd-4734-9b69-1efd60a43b33</gco:CharacterString>
#  </gmd:fileIdentifier>
#

import os
import sys
import owslib
import requests
import uuid
import shutil
from   requests.auth import HTTPBasicAuth
from   httplib       import HTTPConnection
from   lxml          import etree
from   xml.dom       import minidom

def publish_2_gn(xml_filename, url, login, password, workspace, database_hostname, verbose):

    from cleanName import cleanName

    output = cleanName(xml_filename, True)
    
    if verbose:
        print "xml_filename : ", xml_filename
        print "output       : ", output
        print "url          : ", url
        print "login        : ", login
        print "password     : ", password
        print "workspace    : ", workspace
        print "dbhost       : ", database_hostname

    # https://stackoverflow.com/questions/3828723/why-should-we-not-use-sys-setdefaultencodingutf-8-in-a-py-script/34378962
    # il serait préférable de positionner correctement la variable d'environnement PYTHONIOENCODING="UTF-8"
    reload(sys)  
    sys.setdefaultencoding('utf8')

    # vérifie l'existence du fichier d'entrée, au format xml, qui contient les métadonnées à envoyer à GeoNetwork
    if not os.path.isfile(xml_filename):
        sys.stderr.write("ERROR xml_filename file not found : " + xml_filename + "\n")
        return

    # teste l'existence d'un fichier avec l'extension uid associé au fichier xml, qui contient un uuid déjà défini par geosync
    uuid_filename = xml_filename + ".uuid"
    old_geosync_uuid = ""
    if os.path.isfile(uuid_filename):
        print "fichier .xml.uuid trouvé"
        # Ouverture du fichier .uuid en lecture
        file_uuid = open(uuid_filename, "r")
        # Lit la première ligne, sans caractère de fin de ligne
        # http://www.chicoree.fr/w/Lire_et_%C3%A9crire_un_fichier_texte_avec_Python
        old_geosync_uuid = file_uuid.readline().rstrip('\n\r')
        print "old_geosync_uuid : " + old_geosync_uuid

    # on affiche dans les commentaires le nom de la couche associée à la métadonnée
    # name_layer_gs = geosync-restreint:baies_metadata__baies_metadata
    name_layer_gs = workspace + ":" + output.split(".")[0]

    # création d'un répertoire temporaire pour y enregistrer le fichier de travail
    home = os.environ["HOME"] 
    tmpdir = home + "/tmp/geosync_metadata"
    if os.path.exists(tmpdir):
        import shutil
        shutil.rmtree(tmpdir,True) # ignore_errors
        try:
            os.mkdir(tmpdir)
        except OSError as e:
            if e.errno != errno.EEXIST:
                #sys.stderr.write("erreur lors de la création de tmpdir"+ tmpdir +"\n")
                raise  # raises the error again
    else :
        os.mkdir(tmpdir) 

    # Translate Esri metadata to ISO19139

    # vérifie la présence de ArcGIS2ISO19139.xsl
    script_path = os.path.dirname(os.path.abspath(__file__))
    xsl_path = script_path + "/ArcGIS2ISO19139.xsl"
    if not os.path.isfile(xsl_path) :
        sys.stderr.write("ERROR xsl file not found : " + xsl_path + "\n")
        return

    # import des codecs pour les fichiers ArcGIS
    # à améliorer : le nom du fichier xml_filename est renommé dans la boucle de lecture du fichier
    initial_file_name = xml_filename
    new_uuid = False
    # recherche de la base Esri
    tree = etree.parse(xml_filename)
    xpath_esri = tree.xpath('Esri')
    if xpath_esri :
        print "Métadonnée Esri"

        # recherche de la balise ArcGISProfile
        # <ArcGISProfile>ISO19139</ArcGISProfile>
        balise_ArcGISProfile = 'ArcGISProfile'
        doc = minidom.parse(xml_filename)
        test_agsprofile = doc.getElementsByTagName(balise_ArcGISProfile)

        # si le fichier contient une balise ArcGISProfile
        if test_agsprofile :
            esri_type = test_agsprofile[0].firstChild.data
            print "balise ArcGISProfile : " + esri_type

            # s'il est dans le format ISO19139
            if (esri_type == "ISO19139") : 
                print "Métadonnée Esri iso 19139"
                # <mdFileID>73D12C5D-A5F7-4217-A781-A7042E94476E</mdFileID>
                balise_mdFileID = 'mdFileID'
                test_mdfileid = doc.getElementsByTagName(balise_mdFileID)

                # le fichier a un identifiant
                if test_mdfileid :
                    mdfileid = test_mdfileid[0].firstChild.data
                    print "balise mdFileID : " + mdfileid
                else :
                    print "Métadonnée Esri iso 19139 sans identifiant"

            #else :
            # tous les fichiers ArcGISProfile doivent être convertis avec saxonb-xslt
            # utilisation de saxonb-xslt pour traduire ArcGIS metadata => iso 19139
            # <ArcGISProfile>ItemDescription</ArcGISProfile>
            print "Métadonnée Esri propriétaire"
            print "Métadonnée de type ArcGIS à convertir en ISO 19139"
            import subprocess
            saxon_xml_filename  = "-s:" + xml_filename
            print str(saxon_xml_filename) 
            saxon_xsl    = "-xsl:" + xsl_path 
            saxon_output = "-o:" + tmpdir + "/sax_" +  output 
            print str(saxon_output)
            cmd = "saxonb-xslt", "-ext:on", saxon_xml_filename, saxon_xsl, saxon_output
            if verbose:
                print "saxonb cmd :", cmd
            subprocess.call(cmd)
            xml_filename = tmpdir + "/sax_" +  output
            print "xml_filename : " + xml_filename

    # Add Geoserver link to metadata and generate UUID

    # utilisation de lxml pour récupérer le contenu de la balise gmd:title
    # exemple :
    #      <gmd:title>
    #        <gco:CharacterString>Haies de Franche-Comté en 2010</gco:CharacterString>
    #      </gmd:title>
    # question : pourrait-on avoir une balise title sans gmd et/ou sans gco ?
    # oui, ça marche aussi avec
    #      <title>
    #        <gco:CharacterString>Haies_Besancon_ouest</gco:CharacterString>
    #      </title>

    # recherche de la balise title
    # à améliorer : tous les fichiers ont-ils un title ?
    tree = etree.parse(xml_filename)
    xpath_title = tree.xpath('//gmd:title/gco:CharacterString',
                             namespaces={'gmd': 'http://www.isotc211.org/2005/gmd',
                                         'gco': 'http://www.isotc211.org/2005/gco'})
    # quand il y a plusieurs balises gmd:title, on obtient un tableau de titres
    titre = ''
    i = 0
    if len(xpath_title) :
        for title in xpath_title :
            i += 1
            titre = titre + str(title.text)
            print "titre " + str(i) + " : " + str(title.text)
    else :
       print "balise gmd:title non trouvée"
       titre = 'sans titre'

    # utilisation de minidom pour lire et modifier l'arbre xlm
    # tutoriel minidom : http://www.fil.univ-lille1.fr/~marvie/python/chapitre4.html
    # à refaire éventuellement avec lxml
    doc = minidom.parse(xml_filename)
    element = doc.documentElement

    # à quel type de fichier de métadonnées avons-nous à faire ? 
    # typiquement on a une balise principale qui peut être l'une 3 balises suivantes :
    # - <metadata...>
    # - <MD_Metadata...>
    # - <gmd:MD_Metadata...
    # objectif : insérer des balises avec le namespace gmd si le document xml original en contient
    # GeoNetwork gère bien l'import avec ou sans gmd, dès lors que le fichier est cohérent
    # GMD : Geographic MetaData extensible markup language
    type_csw = doc.firstChild.tagName
    print "type_csw : " + type_csw
    # on positionne dans une variable la présence des préfixes gmd:
    if ('gmd:' in type_csw) :
        gmd = 'gmd:'
    else :
        gmd = ''

    # recherche de la balise gmd:fileIdentifier
    # on recherche d'abord toutes les balises gco:CharacterString et on s'arrête quand le parent est une balise gmd:fileIdentifier ou fileIdentifier
    balise = 'gco:CharacterString'

    fileIdentifier = False
    for element in doc.getElementsByTagName(balise):
        # si la balise gmd:fileIdentifier existe déjà
        balise_file = gmd + "fileIdentifier"
	if balise_file in str(element.parentNode):
	    fileIdentifier = True
	    print "fileIdentifier trouvé : " + element.firstChild.nodeValue

    # si la balise gmd:fileIdentifier n'existe pas, alors on la créée, avec un nouvel uuid
    if not fileIdentifier :
        balise_file = gmd + 'fileIdentifier'
        element_file = doc.createElement(balise_file)
        balise_gco = 'gco:CharacterString'
        element_file_gco = doc.createElement(balise_gco)
        element_file.appendChild(element_file_gco)

        # si un uuid a été trouvé dans le fichier.xml.uid, on le récupère
        if old_geosync_uuid :
            print "réutilisation de l'ancien identifiant"
            geosync_uuid = old_geosync_uuid
        else :
            print "création à la volée d'un identifiant"
	    # https://stackoverflow.com/questions/534839/how-to-create-a-guid-uuid-in-python
            geosync_uuid = str(uuid.uuid4())
            new_uuid = True

        element_geosync_uuid = doc.createTextNode(geosync_uuid)
        element_file_gco.appendChild(element_geosync_uuid)
        print "insertion de la balise fileIdentifier dans l'arbre"
        for element in doc.getElementsByTagName(type_csw) :
            element.appendChild(element_file)

    # recherche de la balise gmd:URL avec lxml
    # tutoriel : http://lxml.de/tutorial.html
    # même remarque : on suppose que toutes les balises contiennent gmd:
    lien_existant = False
    xpath_url = tree.xpath('//gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL',
                           namespaces={'gmd': 'http://www.isotc211.org/2005/gmd',
                                       'gco': 'http://www.isotc211.org/2005/gco'})
    #print "xpath_url : " + str(xpath_url)
    # on obtient un tableau quand il y a plusieurs url
    if len(xpath_url) :
        for xurl in xpath_url :
            print str(xurl.text)
            # le lien vers le geoserver existe-t-il déjà ?
            # à améliorer pour éviter les redondances de liens dev/test/prod
            if url in xurl.text :
                lien_existant = True
                print "le lien vers " + url + " est déjà positionné"

    if not lien_existant :
        # si le lien vers le geoserver n'existe pas, on doit le créer.
        # mais est-ce que la balise MD_DigitalTransferOptions existe déjà ?
        # si elle n'existe pas, alors on la créée

        # recherche de la balise MD_DigitalTransferOptions avec minidom
        balise_DigitalTransferOptions = gmd + 'MD_DigitalTransferOptions'
        test_digital = doc.getElementsByTagName(balise_DigitalTransferOptions)

        # création de l'arborescence nécessaire à la création de la balise MD_DigitalTransferOptions
        if test_digital :
            print "balise MD_DigitalTransferOptions trouvée"
        else :
            print "pas de balise MD_DigitalTransferOptions"
            # donc création des 4 balises xml imbriquées :
            # gmd:distributionInfo / gmd:MD_Distribution / gmd:transferOptions / gmd:MD_DigitalTransferOptions"

            balise_dist = gmd + 'distributionInfo'
            element_dist = doc.createElement(balise_dist)
            balise_MD_dist = gmd + 'MD_Distribution'
            element_MD_dist = doc.createElement(balise_MD_dist)
            balise_transfert = gmd + 'transferOptions'
            element_transfert = doc.createElement(balise_transfert)
            balise_digital = gmd + 'MD_DigitalTransferOptions'
            element_digital = doc.createElement(balise_digital)

            print "insertion des balises dans l'arbre des 4 balises"
            for element in doc.getElementsByTagName(type_csw) : 
                element.appendChild(element_dist)
                element_dist.appendChild(element_MD_dist)
                element_MD_dist.appendChild(element_transfert)
                element_transfert.appendChild(element_digital)
 
        # la balise gmd:MD_DigitalTransferOptions existait déjà
        # on lui rajoute un lien vers notre geoserver

        # création balise online
        balise_online = gmd + 'onLine'
        element_online = doc.createElement(balise_online)

        # création balise ressource
        balise_ressource = gmd + 'CI_OnlineResource'
        element_ressource = doc.createElement(balise_ressource)	

        # création balise linkage
        balise_linkage = gmd + 'linkage'
        element_linkage = doc.createElement(balise_linkage)

        # création et remplissage balise url
        balise_url = gmd + 'URL' 
        element_url = doc.createElement(balise_url)
        url_wms = url + "/geoserver/ows?SERVICE=WMS&"
        element_url_txt = doc.createTextNode(url_wms)
        element_url.appendChild(element_url_txt)

        # création et remplissage balise protocole
        balise_protocol = gmd + 'protocol'	
        element_protocol = doc.createElement(balise_protocol)	
        balise_gco = 'gco:CharacterString'
        element_protocol_gco = doc.createElement(balise_gco)
        element_protocol.appendChild(element_protocol_gco)
        #element_protocol_txt = doc.createTextNode(u"OGC:WMS-1.3.0-http-get-capabilities")
        element_protocol_txt = doc.createTextNode(u"OGC:WMS-1.3.0-http-get-map")
        element_protocol_gco.appendChild(element_protocol_txt)        

        # création et remplissage balise name
        balise_name = gmd + u'name'
        element_name = doc.createElement(balise_name)
        balise_gco = 'gco:CharacterString'
        element_name_gco = doc.createElement(balise_gco)
        element_name.appendChild(element_name_gco)

        # création et remplissage balise name_layer_gs qui contient le nom de la couche geoserver
        # name_layer_gs est initialisée en début de procédure
        element_name_txt = doc.createTextNode(name_layer_gs)
        element_name_gco.appendChild(element_name_txt)

        # création et remplissage balise description
        balise_descr = gmd +'description'
        element_descr = doc.createElement(balise_descr)
        balise_gco = 'gco:CharacterString'
        element_descr_gco = doc.createElement(balise_gco)
        element_descr.appendChild(element_descr_gco)
        element_descr_txt = doc.createTextNode(output.split(".")[0])
        element_descr_gco.appendChild(element_descr_txt)

        # une fois créé, chaque élément est inséré dans l'arbre
        # la fonction print sert à l'affichage à la console
        for element in doc.getElementsByTagName(balise_DigitalTransferOptions):
            element.appendChild(element_online)
            #print element.toxml()
            element_online.appendChild(element_ressource)
            #print element.toxml()
            element_ressource.appendChild(element_linkage)
            #print element.toxml()
            element_linkage.appendChild(element_url)
            #print element.toxml()
            element_ressource.appendChild(element_protocol)
            #print element.toxml()
            element_ressource.appendChild(element_name)
            #print element.toxml()
            element_ressource.appendChild(element_descr)
            #print element.toxml()

    # le fichier qui sera envoyé à geonetwork est d'abord écrit dans le répertoire temporaire
    xml_filename_csw = tmpdir + "/csw_" +  output
    xml_filename_csw_fic = open(xml_filename_csw,'w') 
    txt = doc.toxml().encode('utf-8','ignore')
    xml_filename_csw_fic.write(txt)
    xml_filename_csw_fic.close()

    # on crée à la volée un fichier contenant l'identifiant s'il a été généré
    if new_uuid :
        output_uid = tmpdir + "/uid_" +  output
        print "écriture de l'identifiant dans " + output_uid
        output_uid_fic = open(output_uid,'w')
        output_uid_fic.write(geosync_uuid)
        output_uid_fic.close()

        # ce fichier est retourné à l'utilisateur via le partage OwnCloud
        rep = os.path.dirname(initial_file_name)
        fic = os.path.basename(initial_file_name)
        retour_output_uid = rep + "/" + fic + ".uuid"
        print "le fichier est retourné à l'utilisateur " + retour_output_uid
        shutil.copyfile(output_uid, retour_output_uid)

    # connexion à GeoNetwork avec la librairie owslib
    from owslib.csw import CatalogueServiceWeb
    url_csw = url + "/geonetwork/srv/fre/csw-publication"
    # Attention : l'utilisateur (login) doit avoir le rôle GN_EDITOR (ou GN_ADMIN) voir administration ldap
    ## sinon peut générer l'erreur : lxml.etree.XMLSyntaxError: Opening and ending tag mismatch
    csw = CatalogueServiceWeb(url_csw, skip_caps=True, username=login, password=password)
    
    # suppression des métadonnées relatives à la même couche geoserver
    print "suppression de " + titre + " " + name_layer_gs
    from owslib.fes import PropertyIsEqualTo, PropertyIsLike
    myquery = PropertyIsEqualTo('csw:AnyText', name_layer_gs)
    csw.getrecords2(constraints=[myquery], maxrecords=10)
    resultat = csw.results
    #print "resultat : " , resultat 
    for rec in csw.records:
        print "suppression de " + csw.records[rec].title + csw.records[rec].identifier
        csw.transaction(ttype='delete', typename=type_csw, identifier=csw.records[rec].identifier)
   
    # Transaction: insert
    #print "type_csw " + type_csw
    print "xml_filename_csw : " + xml_filename_csw

    # le fichier de métadonnées pourrait être envoyé avec la librairie owslib, si ça marchait bien.
    # csw.transaction(ttype='insert', typename=type_csw, record=open(xml_filename_csw).read())
    # mais problème : les données ne sont pas publiques qiand elles sont envoyées avec owslib
    # on utilise donc l'API de GeoNetwork
    # https://georchestra-mshe.univ-fcomte.fr/geonetwork/doc/api/

    HTTPConnection.debuglevel = 0

    # ouverture de session
    geonetwork_session = requests.Session()
    geonetwork_session.auth = HTTPBasicAuth(login, password)
    geonetwork_session.headers.update({"Accept" : "application/xml"})

    # 1er POST, pour récupérer le token xsrf
    geonetwork_url = url + '/geonetwork/srv/eng/info?type=me'
    r_post = geonetwork_session.post(geonetwork_url)

    # prise en compte du token xsrf
    # https://geonetwork-opensource.org/manuals/trunk/eng/users/customizing-application/misc.html
    token = geonetwork_session.cookies.get('XSRF-TOKEN')
    geonetwork_session.headers.update({"X-XSRF-TOKEN" : geonetwork_session.cookies.get('XSRF-TOKEN')})

    # envoi du fichier de métadonnées
    geonetwork_post_url = url + '/geonetwork/srv/api/0.1/records?uuidProcessing=OVERWRITE'
    files = {'file': (xml_filename_csw, open(xml_filename_csw,'rb'), 'application/xml', {'Expires': '0'})}
    geonetwork_session.headers.update({"Accept" : "application/json"})
    r_post = geonetwork_session.post(geonetwork_post_url, files=files)
    content = r_post.json()
    identifiant = content[u'metadataInfos'].keys()
    identifiant = identifiant[0]
    print "métadonnées envoyées : " + xml_filename_csw

    # modification des privilèges de la métadonnée qu'on vient d'insérer dans GeoNetwork
    # Attention : l'utilisateur (login) doit avoir le rôle GN_ADMIN. voir administration ldap
    data_privilege = '{ "clear": true, "privileges": [ {"operations":{"view":true,"download":false,"dynamic":false,"featured":false,"notify":false,"editing":false},"group":-1}, {"operations":{"view":true,"download":false,"dynamic":false,"featured":false,"notify":false,"editing":false},"group":0}, {"operations":{"view":true,"download":false,"dynamic":false,"featured":false,"notify":false,"editing":false},"group":1} ] }'
    geonetwork_session.headers.update({"Accept" : "*/*"})
    geonetwork_session.headers.update({"Content-Type" : "application/json"})
    geonetwork_session.headers.update({"X-XSRF-TOKEN" : token})
    geonetwork_put_url = url + '/geonetwork/srv/api/0.1/records/' + identifiant + '/sharing'
    print geonetwork_put_url
    r_put = geonetwork_session.put(geonetwork_put_url, data=data_privilege)
    print r_put.text
    print "métadonnées rendues publiques"

# test de la fonction publish_2_gn
if __name__ == "__main__":

    import argparse

    parser = argparse.ArgumentParser(add_help=True)
    #parser.add_argument('-i',           action="store",      dest="xml_filename",       required=True)
    #parser.add_argument('-i',           action="store",      dest="xml_filename",       default="metadata.xml")
    parser.add_argument('-i',           action="store",      dest="xml_filename",       default="meta_esri_natif.shp.xml")
    #parser.add_argument('-i',           action="store",      dest="xml_filename",       default="meta_esri_19139_sans_uid.shp.xml")
    #parser.add_argument('-i',           action="store",      dest="xml_filename",       default="meta_esri_19139_avec_uid.shp.xml")
    #parser.add_argument('-i',           action="store",      dest="xml_filename",       default="200_metadata_xml_QGIS_gmd.xml")
    #parser.add_argument('-i',           action="store",      dest="xml_filename",       default="haies_sans_lien_geoserver.xml")
    #parser.add_argument('-i',           action="store",      dest="xml_filename",       default="Haies_Besancon_ouest.shp.xml")
    #parser.add_argument('-i',           action="store",      dest="xml_filename",       default="haies_avec_lien_geoserver.xml")
    #parser.add_argument('-i',           action="store",      dest="xml_filename",       default="haies_avec_deux_liens_geoserver.xml")
    #parser.add_argument('-i',           action="store",      dest="xml_filename",       default="geonetwork-record.xml")
    #parser.add_argument('-l',           action="store",      dest="login",              required=True)
    parser.add_argument('-l',           action="store",      dest="login",              default="testadmin")
    #parser.add_argument('-o',           action="store",      dest="output"                 )
    #parser.add_argument('-o',           action="store",      dest="output",             default="metadata.xml")
    parser.add_argument('-o',           action="store",      dest="output",             default="meta_esri_natif.shp.xml")
    #parser.add_argument('-o',           action="store",      dest="output",             default="meta_esri_19139_sans_uid.shp.xml")
    #parser.add_argument('-o',           action="store",      dest="output",             default="meta_esri_19139_avec_uid.shp.xml")
    #parser.add_argument('-o',           action="store",      dest="output",             default="200_metadata_xml_QGIS_gmd.xml")
    #parser.add_argument('-o',           action="store",      dest="output",             default="haies_sans_lien_geoserver.xml")
    #parser.add_argument('-o',           action="store",      dest="output",             default="Haies_Besancon_ouest.shp.xml")
    #parser.add_argument('-o',           action="store",      dest="output",             default="haies_avec_lien_geoserver.xml")
    #parser.add_argument('-o',           action="store",      dest="output",             default="haies_avec_deux_liens_geoserver.xml")
    #parser.add_argument('-o',           action="store",      dest="output",             default="geonetwork-record.xml")
    #parser.add_argument('-p',           action="store",      dest="password",           required=True)
    parser.add_argument('-p',           action="store",      dest="password",           default="testadmin")
    parser.add_argument('-s',           action="store",      dest="datastore"              )
    parser.add_argument('-u',           action="store",      dest="url",                default="https://georchestra-docker.umrthema.univ-fcomte.fr")
    parser.add_argument('-v',           action="store_true", dest="verbose",            default=True)
    parser.add_argument('-w',           action="store",      dest="workspace",          default="geosync-ouvert")
    parser.add_argument('--db_hostname',action="store",      dest="database_hostname",  default="localhost")

    args = parser.parse_args()
    print parser.parse_args()

    if args.xml_filename:
        publish_2_gn(args.xml_filename, args.url, args.login, args.password, args.workspace, args.database_hostname, args.verbose)

