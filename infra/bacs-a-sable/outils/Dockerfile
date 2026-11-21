# Image commune de l'ICP (OpenSSL : autorité, horodatage RFC 3161, OCSP, listes de révocation) et du service pyHanko.
# LibreOffice et veraPDF tournent dans leurs propres images officielles (Gotenberg, veraPDF REST).
FROM python:3.12-slim-bookworm
RUN apt-get update \
    && apt-get install -y --no-install-recommends openssl curl \
    && rm -rf /var/lib/apt/lists/*
RUN pip install --no-cache-dir "pyhanko[opentype]==0.25.1" "pyhanko-certvalidator==0.26.3"
WORKDIR /travail
# Compte sans privilège : l'ICP d'essai et pyHanko n'ont besoin que de leur état (/etat) et de leurs scripts en lecture.
RUN useradd --system --uid 10001 --no-create-home bac && mkdir -p /travail && chown bac /travail
USER 10001
