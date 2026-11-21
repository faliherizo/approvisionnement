"""Service de validation : PAdES par pyHanko (POST /valider?profil=pades-lta, corps application/pdf) et CAdES par
OpenSSL (POST /cms, JSON {"signature": base64 DER, "contenu": base64} → openssl cms -verify) et jetons d'horodatage
RFC 3161 par OpenSSL (POST /horodatage, JSON {"reponse": base64 TSR, "donnees": base64} → openssl ts -verify).

pyHanko n'a pas de serveur officiel ; ce service l'expose sans y ajouter de logique : il lance la validation pyHanko
avec la racine de l'ICP comme seule ancre de confiance et rend son verdict et son rapport tels quels, en JSON.
"""
import base64
import json
import os
import subprocess
import tempfile
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import parse_qs, urlparse

ANCRE = os.environ.get("ANCRE_CONFIANCE", "/etat/ac/ac.pem")
PROFILS = {"pades": "pades", "pades-lta": "pades-lta"}


class Validation(BaseHTTPRequestHandler):
    protocol_version = "HTTP/1.1"

    def repondre(self, code, donnees):
        corps = json.dumps(donnees, ensure_ascii=False).encode()
        self.send_response(code)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(corps)))
        self.end_headers()
        self.wfile.write(corps)

    def do_GET(self):
        self.repondre(200 if self.path == "/sante" else 404, {"etat": "ok"} if self.path == "/sante" else {})

    def cms(self):
        d = json.loads(self._corps)
        with tempfile.TemporaryDirectory() as rep:
            sig, contenu = os.path.join(rep, "s.p7s"), os.path.join(rep, "c.bin")
            open(sig, "wb").write(base64.b64decode(d["signature"]))
            open(contenu, "wb").write(base64.b64decode(d["contenu"]))
            r = subprocess.run(["openssl", "cms", "-verify", "-binary", "-inform", "DER", "-in", sig, "-content", contenu,
                                "-CAfile", ANCRE, "-purpose", "any", "-out", os.devnull], capture_output=True, text=True)
        rapport = r.stdout + r.stderr
        self.repondre(200, {"valide": r.returncode == 0 and "Verification successful" in rapport, "rapport": rapport})

    def horodatage(self):
        d = json.loads(self._corps)
        with tempfile.TemporaryDirectory() as rep:
            tsr, donnees = os.path.join(rep, "r.tsr"), os.path.join(rep, "d.bin")
            open(tsr, "wb").write(base64.b64decode(d["reponse"]))
            open(donnees, "wb").write(base64.b64decode(d["donnees"]))
            texte = subprocess.run(["openssl", "ts", "-reply", "-in", tsr, "-text"], capture_output=True, text=True).stdout
            r = subprocess.run(["openssl", "ts", "-verify", "-data", donnees, "-in", tsr, "-CAfile", ANCRE],
                               capture_output=True, text=True)
        rapport = r.stdout + r.stderr
        self.repondre(200, {"valide": r.returncode == 0 and "Verification: OK" in rapport, "accorde": "Status: Granted." in texte,
                            "rapport": texte + rapport})

    def do_POST(self):
        # Corps toujours lu, même pour un refus : la connexion persistante reste utilisable.
        self._corps = self.rfile.read(int(self.headers.get("Content-Length", "0")))
        url = urlparse(self.path)
        if url.path == "/horodatage" and self.headers.get("Content-Type", "").startswith("application/json"):
            return self.horodatage()
        if url.path == "/cms" and self.headers.get("Content-Type", "").startswith("application/json"):
            return self.cms()
        profil = PROFILS.get(parse_qs(url.query).get("profil", ["pades-lta"])[0])
        if url.path != "/valider" or profil is None or self.headers.get("Content-Type") != "application/pdf":
            return self.repondre(400, {"erreur": "POST /valider?profil=pades|pades-lta, corps application/pdf"})
        with tempfile.NamedTemporaryFile(suffix=".pdf") as f:
            f.write(self._corps)
            f.flush()
            r = subprocess.run(["pyhanko", "sign", "validate", "--pretty-print", "--trust", ANCRE, "--ltv-profile", profil,
                                "--force-revinfo", f.name], capture_output=True, text=True)
        rapport = r.stdout + r.stderr
        self.repondre(200, {"valide": r.returncode == 0 and "judged VALID" in rapport, "profil": profil,
                            "codeRetour": r.returncode, "rapport": rapport})


if __name__ == "__main__":
    ThreadingHTTPServer(("0.0.0.0", 18093), Validation).serve_forever()
