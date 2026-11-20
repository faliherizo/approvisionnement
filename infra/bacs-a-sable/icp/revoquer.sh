#!/bin/sh
# Révoque le certificat de cachet, comme le ferait l'autorité sur demande : la liste et l'OCSP le reflètent aussitôt.
set -eu
openssl ca -config /icp/openssl.cnf -revoke /etat/cachet.pem -crl_reason keyCompromise
