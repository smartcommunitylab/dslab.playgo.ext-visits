# syntax=docker/dockerfile:experimental
FROM maven:3-openjdk-11 as mvn
COPY src/ /tmp/src
COPY pom.xml /tmp/pom.xml
WORKDIR /tmp
RUN --mount=type=cache,target=/root/.m2,source=/root/.m2,from=smartcommunitylab/playngo-ext-visits:cache mvn package -DskipTests

FROM eclipse-temurin:11-alpine
ARG VER=1.0
ARG USER=playngo
ARG USER_ID=1008
ARG USER_GROUP=playngo
ARG USER_GROUP_ID=1008
ARG USER_HOME=/home/${USER}
ENV FOLDER=/tmp/target
ENV APP=quarkus-run
RUN  addgroup -g ${USER_GROUP_ID} ${USER_GROUP}; \
     adduser -u ${USER_ID} -D -g '' -h ${USER_HOME} -G ${USER_GROUP} ${USER} ;

COPY --chown=1008:1008 --from=mvn /tmp/target/quarkus-app/lib/ /deployments/lib/
COPY --chown=1008:1008 --from=mvn /tmp/target/quarkus-app/*.jar /deployments/
COPY --chown=1008:1008 --from=mvn /tmp/target/quarkus-app/app/ /deployments/app/
COPY --chown=1008:1008 --from=mvn /tmp/target/quarkus-app/quarkus/ /deployments/quarkus/

WORKDIR ${USER_HOME}
EXPOSE 8080
USER playngo
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /deployments/${APP}.jar"]