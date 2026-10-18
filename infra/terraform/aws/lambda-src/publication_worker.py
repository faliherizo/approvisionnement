"""Worker de publication (squelette).

Déclenché par la file SQS de publication : dépose le document rendu vers Microsoft 365 / SAP
en appelant Microsoft Graph, puis émet un événement de résultat sur EventBridge.

Le code métier réel réutilise l'adaptateur Graph du backend (HttpMicrosoft365Client). Ce module
est un point d'entrée minimal remplacé par l'artefact construit en CI (voir pipeline)."""
import json
import logging
import os

logger = logging.getLogger()
logger.setLevel(logging.INFO)


def handler(event, context):
    for record in event.get("Records", []):
        message = json.loads(record.get("body", "{}"))
        logger.info(
            "Publication demandée: document=%s cible=%s",
            message.get("documentId"),
            message.get("cible"),
        )
        # TODO (CI): appeler Microsoft Graph via le secret %s et émettre le résultat sur EventBridge.
        # secret = os.environ["APP_SECRET_ARN"]
    return {"status": "ok", "traites": len(event.get("Records", []))}
