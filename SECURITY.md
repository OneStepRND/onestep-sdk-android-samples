# Security

## Reporting a vulnerability

Email `shahar@onestep.co`. Do not open a public issue for a security problem, and never include
credentials or patient data (PHI) in a report.

## Credentials never go in this repository

This repository is public. Client tokens, API keys, identity-verification secrets or digests, and
patient identifiers must never be committed, in code, configuration, docs, issues or pull requests.

- Each sample reads its credentials from its own `local.properties`, which git ignores. Start from
  `local.properties.example` in the sample's folder.
- The identity-verification secret stays on your server. The app receives only the digest.
- Use synthetic test users only. Never a real patient's name, record number or health data.

## Guardrails

- **CI:** `.github/workflows/secret-scan.yml` runs [gitleaks](https://github.com/gitleaks/gitleaks)
  on the new commits of every pull request and push to `main`, with the rules in `.gitleaks.toml`.
  A finding fails the check.
- **Local hook (recommended):** install gitleaks, then run once per clone:

  ```bash
  git config core.hooksPath .githooks
  ```

  The pre-commit hook then blocks a commit that adds a credential.

## If a credential was committed

1. Revoke or rotate it on the OneStep side first. Deleting the commit does not make a pushed secret
   safe: it can already be in forks, clones and caches.
2. Then remove it from the code and contact `shahar@onestep.co`.
