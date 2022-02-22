FROM quay.io/ukhomeofficedigital/hocs-base-image as builder

COPY build/libs/*.jar .

RUN java -Djarmode=layertools -jar *.jar extract

FROM quay.io/ukhomeofficedigital/hocs-libreoffice

WORKDIR /app

COPY scripts/run.sh /app/scripts/run.sh

COPY --from=builder dependencies/ ./
COPY --from=builder spring-boot-loader/ ./
COPY --from=builder application/ ./

EXPOSE 8080

USER ${USER_ID}

CMD ["sh", "/app/scripts/run.sh"]
