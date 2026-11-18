 worked through the addenda 3 answers: all 121 obligatory functional requirements are now covered. The exception is capacity: 500 concurrent users (Q38) is still not demonstrated. Backend tests pass: 761/761, both coverage gates met. Nothing is committed.

Written for: équipe de soumission (documents .md du dossier IQ)

What addenda 3 made obligatory, and what I implemented

Story	Answer	What was missing	Done
RST-26	Q51	No SCIM provisioning, now obligatory	/scim/v2 endpoint for Saviynt and Entra ID: creation, groups, deactivation. One token per organisation, shown only once.
RST-27	Q43, Q44	SSO only assigned a default role	Directory groups and attributes give roles and business domains. Least privilege. Every role change goes to the audit log.
RST-28	Q26	PDF only	Generation as editable Word. A change after launch restarts the approvals.
RST-29	Q48	Destruction only traced in the journal	Sealed destruction certificate: items, date, authority, method, result.
RST-30	Q39	No metering	Usage report per sent envelope (IQ's billing unit), computed from the sealed journal.
Documentation delivered

questions_sans_reponse_IQ.md: the questions still unanswered after the three addenda, each with the assumption we'll use. It adds 6 new questions raised by addenda 3, for example whether an invitation resent after expiry counts as a new envelope.
questions_a_clarifier_IQ.md: the addenda 3 answers are worked in.
solutions_proposees_reponses_IQ.md: our solution for each answer where IQ asks us to describe something (Salesforce method, statuses, privacy data, hosting locations, RTO/RPO, billing, signature, Workday, SCIM). Ready to reuse in the submission.
guide_annuaire_scim.md: operational guide for Entra ID, Saviynt and SCIM, which Q51 requires.
impact_addenda_3.md: the full analysis.
ADRs:
ADR-014: directory and SCIM;
ADR-015: envelope metering;
ADR-016: Salesforce changes from addenda 2;
ADR-012 revised for the destruction certificate.
The conformity matrix, the inventory, the stories, the architecture doc and the sandbox guide are updated. Addenda 3 moves the submission deadline to 4 December 2026, 11 a.m.
Q38, 500 concurrent users: not demonstrated yet

The load test found real defects. Three are fixed:

contract packages created at the same moment received the same reference;
server errors were shown to users as empty "access denied" responses;
the database connection pool was too small.
The remaining blocker is the central follow-up screen (suivi). Each of its rows costs four database queries, so with 1 500 contract packages it takes 40 seconds and slows everything else. I capped it at 500 rows, which isn't enough. The real fix is to paginate the API and the screen (50 rows per page) with grouped queries. That changes the API contract, the screen and the CSV export, so I'd rather you approve it first. Until a run passes, the docs say Q38 is not met and must not be claimed in the submission.

Tests and checks

e2e: the full IQ run gave 51 passed and 3 failed. I fixed the causes: a wrong button name in my own test, a slow query in the usage report, and a database left full by the load test. The 4 affected tests then pass on a clean database, but I haven't rerun the whole IQ suite since.
Global backend coverage: 89.97 %, still under the 96 % target in the standards.
GitNexus: the index for this repo is broken, so detect_changes hasn't been run. It must be run before any commit.