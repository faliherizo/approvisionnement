"""Points de publication de l'ICP du bac à sable, aux protocoles d'une autorité réelle.

- POST /tsa      : horodatage RFC 3161 (application/timestamp-query → application/timestamp-reply), par « openssl ts » ;
- POST /ocsp     : statut de certificat RFC 6960 (application/ocsp-request → application/ocsp-response), par « openssl ocsp » ;
- GET  /crl/ac.crl : liste de révocation RFC 5280 en DER, régénérée à chaque demande depuis le registre ;
- GET  /ac.cer   : certificat de l'autorité (caIssuers) ;
- POST /admin/cachet/suspendre : suspension du certificat de cachet (motif certificateHold, RFC 5280 § 5.3.1) ;
- POST /admin/cachet/retablir  : levée de la suspension (removeFromCRL) — liste et OCSP le reflètent aussitôt.
Aucune réponse n'est fabriquée à la main : chaque octet sort d'OpenSSL, comme chez une autorité qui l'emploie.
"""
import os
import subprocess
import tempfile
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

CNF = "/icp/openssl.cnf"
AC = "/etat/ac"


def openssl(*args, entree=None):
    return subprocess.run(["openssl", *args], input=entree, capture_output=True, check=True).stdout


def serie_cachet():
    return openssl("x509", "-in", "/etat/cachet.pem", "-noout", "-serial").decode().strip().split("=")[1]


def retablir(serie):
    """Levée de suspension : l'entrée du registre redevient valide (statut V, sans date de révocation)."""
    registre = f"{AC}/index.txt"
    with open(registre) as f:
        lignes = f.read().splitlines()
    sortie = []
    for ligne in lignes:
        champs = ligne.split("\t")
        if champs[0] == "R" and champs[3].upper() == serie.upper() and champs[2].endswith(",certificateHold"):
            champs[0], champs[2] = "V", ""
        sortie.append("\t".join(champs))
    with open(registre, "w") as f:
        f.write("\n".join(sortie) + "\n")


class Icp(BaseHTTPRequestHandler):
    def repondre(self, code, type_, corps=b""):
        self.send_response(code)
        self.send_header("Content-Type", type_)
        self.send_header("Content-Length", str(len(corps)))
        self.end_headers()
        self.wfile.write(corps)

    def corps(self):
        return self.rfile.read(int(self.headers.get("Content-Length", "0")))

    def do_POST(self):
        try:
            if self.path == "/tsa" and self.headers.get("Content-Type") == "application/timestamp-query":
                with tempfile.TemporaryDirectory() as d:
                    requete = os.path.join(d, "q.tsq")
                    with open(requete, "wb") as f:
                        f.write(self.corps())
                    reponse = os.path.join(d, "r.tsr")
                    openssl("ts", "-reply", "-config", CNF, "-queryfile", requete, "-out", reponse)
                    with open(reponse, "rb") as f:
                        return self.repondre(200, "application/timestamp-reply", f.read())
            if self.path == "/ocsp" and self.headers.get("Content-Type") == "application/ocsp-request":
                with tempfile.TemporaryDirectory() as d:
                    requete = os.path.join(d, "q.der")
                    with open(requete, "wb") as f:
                        f.write(self.corps())
                    reponse = os.path.join(d, "r.der")
                    openssl("ocsp", "-index", f"{AC}/index.txt", "-CA", f"{AC}/ac.pem", "-rsigner", "/etat/ocsp/ocsp.pem",
                            "-rkey", "/etat/ocsp/ocsp.key", "-reqin", requete, "-respout", reponse, "-ndays", "1")
                    with open(reponse, "rb") as f:
                        return self.repondre(200, "application/ocsp-response", f.read())
            if self.path == "/admin/cachet/suspendre":
                openssl("ca", "-config", CNF, "-revoke", "/etat/cachet.pem", "-crl_reason", "certificateHold")
                return self.repondre(200, "text/plain", b"suspendu")
            if self.path == "/admin/cachet/retablir":
                retablir(serie_cachet())
                return self.repondre(200, "text/plain", b"retabli")
            self.repondre(415 if self.path in ("/tsa", "/ocsp") else 404, "text/plain", b"refus")
        except subprocess.CalledProcessError as e:
            self.repondre(500, "text/plain", e.stderr)

    def do_GET(self):
        if self.path == "/crl/ac.crl":
            pem = openssl("ca", "-gencrl", "-config", CNF)
            return self.repondre(200, "application/pkix-crl", openssl("crl", "-outform", "DER", entree=pem))
        if self.path == "/ac.cer":
            with open(f"{AC}/ac.cer", "rb") as f:
                return self.repondre(200, "application/pkix-cert", f.read())
        if self.path == "/sante":
            return self.repondre(200, "text/plain", b"ok")
        self.repondre(404, "text/plain", b"introuvable")


if __name__ == "__main__":
    ThreadingHTTPServer(("0.0.0.0", 18090), Icp).serve_forever()
