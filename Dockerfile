FROM quay.io/ukhomeofficedigital/hocs-libreoffice

ENV USER user_hocs_docs_converter
ENV USER_ID 1000
ENV GROUP group_hocs_docs_converter
ENV NAME hocs-docs-converter
ENV JAR_PATH build/libs

USER root

RUN apk add --no-cache openjdk11-jre

WORKDIR /app

RUN addgroup -S ${GROUP} && \
    adduser -S -u ${USER_ID} ${USER} -G ${GROUP} -h /app && \
    mkdir -p /app && \
    chown -R ${USER}:${GROUP} /app

USER ${USER_ID}

CMD ["sh", "/app/scripts/run.sh"]