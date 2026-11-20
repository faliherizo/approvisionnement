#!/bin/sh
# Point d'entrée du service ICP : initialisation idempotente, puis publication HTTP.
set -eu
sh /icp/initialiser.sh
exec python3 /icp/serveur.py
