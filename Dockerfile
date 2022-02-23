FROM quay.io/ukhomeofficedigital/hocs-base-image as builder

COPY build/libs/*.jar .

RUN java -Djarmode=layertools -jar *.jar extract

FROM quay.io/ukhomeofficedigital/hocs-libreoffice

EXPOSE 8080
USER 10000

WORKDIR /app

COPY scripts/run.sh /app/scripts/run.sh

COPY --from=builder dependencies/ app/
COPY --from=builder spring-boot-loader/ app/
COPY --from=builder application/ app/

CMD ["sh", "/app/scripts/run.sh"]
