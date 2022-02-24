
# HOCS Document Converter Service

[![CodeQL](https://github.com/UKHomeOffice/hocs-docs-converter/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/UKHomeOffice/hocs-docs-converter/actions/workflows/codeql-analysis.yml)

Simple document converter service which accepts any document as a parameter and returns a PDF

---

## Running hocs-docs-converter locally

### Dependencies

- LibreOffice - You should download it from https://www.libreoffice.org/download/download/


### Run from your terminal

```
SPRING_PROFILES_ACTIVE=development ./gradlew bootRun
```

---

## Testing hocs-docs-converter locally

### Run from your terminal

```sh
SPRING_PROFILES_ACTIVE=development ./gradlew check
```

---

Published to https://quay.io/repository/ukhomeofficedigital/hocs-docs-converter
