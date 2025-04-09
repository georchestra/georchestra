#!/bin/bash

# Créer un environnement virtuel
python -m venv venv_mkdocs

# Vérifier si le script est exécuté sous Windows
if [ -n "$WINDIR" ] || [ -n "$MSYSTEM" ]; then
    source venv_mkdocs/Scripts/activate
else
    source venv_mkdocs/bin/activate
fi

# upgrade pip
python -m pip install --upgrade pip

# Installer les dépendances et lister les packages installés
python -m pip install -r mkdocs_requirements.txt
python -m pip list

# on vérifie mkdocs
mkdocs --version

# Désactiver l'environnement virtuel
deactivate
