FROM quay.io/ukhomeofficedigital/hocs-base-image as builder

WORKDIR /builder

COPY ./build/libs/hocs-*.jar .

RUN java -Djarmode=layertools -jar hocs-*.jar extract

FROM quay.io/ukhomeofficedigital/hocs-base-image

RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y software-properties-common && \
    add-apt-repository ppa:libreoffice/ppa && \
    apt-get install libreoffice --no-install-recommends  --no-install-suggests -y && \
    apt-get clean

WORKDIR /app

COPY --from=builder --chown=user_hocs:group_hocs ./builder/spring-boot-loader/ ./
COPY --from=builder --chown=user_hocs:group_hocs ./builder/dependencies/ ./
COPY --from=builder --chown=user_hocs:group_hocs ./builder/application/ ./

USER 10000

ENTRYPOINT exec java $JAVA_OPTS org.springframework.boot.loader.JarLauncher
