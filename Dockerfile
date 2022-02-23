FROM quay.io/ukhomeofficedigital/hocs-base-image as builder

COPY build/libs/hocs-docs-converter.jar .

RUN java -Djarmode=layertools -jar hocs-docs-converter.jar extract

FROM quay.io/ukhomeofficedigital/hocs-libreoffice

COPY scripts/run.sh /app/scripts/run.sh

WORKDIR /app

COPY --from=builder dependencies/ ./
COPY --from=builder spring-boot-loader/ ./
COPY --from=builder application/ ./

USER 1001

CMD ["sh", "/app/scripts/run.sh"]
