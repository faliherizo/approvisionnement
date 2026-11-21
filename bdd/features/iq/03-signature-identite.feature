# language: fr
@AO-IQ-2026-6370-0005
Fonctionnalité: Signature électronique et vérification d'identité (Cahier § 1.4.10, § 1.4.11 ; scénario 4 ; addenda 4)
  Les invitations et les codes partent par courriel SMTP (bac à sable Mailpit) ; l'attestation est signée en CAdES.

  Contexte:
    Étant donné que je suis connecté en tant que "initiateur IQ"

  @IQ-S-04 @IQ-C-61 @IQ-C-62 @IQ-C-63 @IQ-C-64 @IQ-C-03 @IQ-C-57 @IQ-S-07 @IQ-A-04 @IQ-C-54
  Scénario: Un signataire externe reçoit l'invitation par courriel, prouve son identité, consent et signe
    Quand j'exécute le cas "iq.signature.externe"
    Alors la plateforme se comporte comme attendu
    Quand j'ouvre l'écran "IQ — preuve de la trousse"
    Alors l'écran est accessible, sans débordement ni superposition

  @IQ-C-26 @IQ-C-27 @IQ-C-28 @IQ-C-54
  Scénario: Signature séquentielle puis signature conjointe d'un groupe de signataires
    Quand j'exécute le cas "iq.signature.sequentielle-parallele"
    Alors la plateforme se comporte comme attendu

  @IQ-A-24
  Scénario: Un représentant signe pour une entreprise après avoir attesté son autorité
    Quand j'exécute le cas "iq.signature.representant"
    Alors la plateforme se comporte comme attendu

  @IQ-A-23 @IQ-C-61
  Scénario: Le niveau d'identité exigé se configure par l'organisation
    Quand j'exécute le cas "iq.identite.par-risque"
    Alors la plateforme se comporte comme attendu
