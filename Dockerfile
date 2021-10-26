FROM quay.io/ukhomeofficedigital/alpine:v3.14 #TODO: Remove
#FROM quay.io/ukhomeofficedigital/hocs-libreoffice:branch-nm_alpine_libreoffice

ENV USER user_hocs_docs_converter
ENV USER_ID 1000
ENV GROUP group_hocs_docs_converter
ENV NAME hocs-docs-converter
ENV JAR_PATH build/libs

USER root

RUN apk add libreoffice #TODO: Remove
RUN apk add --no-cache msttcorefonts-installer fontconfig #TODO: Remove
RUN update-ms-fonts #TODO: Remove
RUN apk add openjdk11-jre

WORKDIR /app

RUN addgroup -S ${GROUP} && \
    adduser -S -u ${USER_ID} ${USER} -G ${GROUP} -h /app && \
    mkdir -p /app && \
    chown -R ${USER}:${GROUP} /app

COPY ${JAR_PATH}/${NAME}*.jar /app

ADD scripts /app/scripts

RUN chmod a+x /app/scripts/*

EXPOSE 8080

USER ${USER_ID}

CMD ["sh", "/app/scripts/run.sh"]
