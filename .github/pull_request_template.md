## What and why


## How I verified


## Checklist
- [ ] No credentials (tokens, API keys, identity-verification values) and no patient data anywhere in the diff.
- [ ] The changed samples build: `./gradlew :app:assembleDebug :app:assembleRelease :app:testDebugUnitTest :app:lintDebug`.
- [ ] READMEs updated if behavior, setup or versions changed.
