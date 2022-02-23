FROM quay.io/ukhomeofficedigital/hocs-base-image as builder

COPY build/libs/*.jar .

RUN java -Djarmode=layertools -jar *.jar extract

FROM quay.io/ukhomeofficedigital/hocs-libreoffice

COPY scripts/run.sh /app/scripts/run.sh

COPY --from=builder dependencies/ /app/
COPY --from=builder spring-boot-loader/ /app/
COPY --from=builder application/ /app/

USER 1001

CMD ["sh", "/app/scripts/run.sh"]
