#!/bin/bash

# Vérifier si le script est exécuté sous Windows
if [ -n "$WINDIR" ] || [ -n "$MSYSTEM" ]; then
    source venv_mkdocs/Scripts/activate
else
    source venv_mkdocs/bin/activate
fi

# on lance mkdocs
mkdocs serve

# Désactiver l'environnement virtuel
deactivate
