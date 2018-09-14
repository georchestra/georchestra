#!/usr/bin/python
# -*-coding:Utf-8 -*

"""module contenant deleteMetadata"""
import os
import re
import sys


def deleteMetadata(name, login, password, url, workspace, verbose=False):
    #takes a keyword and delete metadata associated

    url_csw = url + "/geonetwork/srv/fre/csw-publication"    
    keyword = workspace + ":" + name              # keyword = geosync-restreint:baies_metadata__baies_metadata

    # Connect to a CSW, and inspect its properties:
    from owslib.csw import CatalogueServiceWeb
    csw = CatalogueServiceWeb(url_csw, skip_caps=True, username=login, password=password)
    # suppression des métadonnées relatives à "keyword" / couche geoserver

    # TODO faire sans owslib via API rest
    #      ex: https://georchestra-mshe.univ-fcomte.fr/geonetwork/srv/fre/xml.search?any=geosync-ouvert:fouilles_chailluz__mobilier_pros
    #      cette recherche est stricte au sens où cela trouve ce qui correspond exactement (exact match) par exemple la recherche de 'myworkspace:foo' ne trouve pas 'myworkspace:foobar'

    from owslib.fes import PropertyIsEqualTo, PropertyIsLike
    myquery = PropertyIsEqualTo('csw:AnyText',keyword)  # TODO vérifier que la recherche est stricte sinon risque de retourner des résultats non désirés (ex: 'myworkspace:foobar' en cherchant 'myworkspace:foo')
    if verbose:
      print "INFO CatalogueServiceWeb(" + url_csw + ",...) ... PropertyIsEqualTo('csw:AnyText'," + keyword + ")"
    csw.getrecords2(constraints=[myquery], maxrecords=10)
    resultat = csw.results
    if verbose:
        print "INFO " + str(csw.results['matches']) + " fiche(s) de metadata trouvée(s)"
    
    if csw.results['matches'] > 1:
        print "WARNING plus d'1 fiche de metadata trouvée pour " + keyword
    
    result = True # s'il y a au moins 1 erreur, retourne False
    for rec in csw.records:
        try:
            csw.transaction(ttype='delete', typename='MD_Metadata', identifier=csw.records[rec].identifier) #marche apparement pour les metadonnees étant de type gmd:MD_Metadata
            result = True and result
        except Exception as e:
            sys.stderr.write("ERROR suppression de metadata pour " + keyword + " échouée : " + e.message + "\n")
            print "ERROR suppression de metadata pour " + keyword + " échouée"
            result = False
        try:
            identifier = csw.records[rec].identifier #genere une erreur si pas d'identifiant
            # titre=csw.records[rec].title
            print "OK suppression de metadata " + keyword + " (" + identifier + ") réussie"
        except Exception as e:
            print "OK suppression de metadata réussie"
            print "WARNING " + e.message

    return result


# test de la fonction deleteMetadata 
if __name__ == "__main__":

    import argparse

    parser = argparse.ArgumentParser(add_help=True)

    parser.add_argument("-i", "--name",         action="store",         dest="name",        required=True)
    parser.add_argument('-l',                   action="store",         dest="login",       required=True)
    parser.add_argument('-p',                   action="store",         dest="password",    required=True)
    parser.add_argument("-u", "--url",          action="store",         dest="url" ,        required=True)
    parser.add_argument("-w", "--workspace",    action="store",         dest="workspace",   required=True)
    parser.add_argument('-v', "--verbose",      action="store_true",    dest="verbose",                     default=False)
    args = parser.parse_args()

    #print "INFO ",parser.parse_args()

    if args.url:
        deleteMetadata(args.name, args.login, args.password, args.url, args.workspace, args.verbose)

