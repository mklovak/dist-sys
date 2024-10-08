FROM gradle:7.2.0-jdk11 as builder

ENV APP_HOME=/app
WORKDIR $APP_HOME

# It is expected to run Docker build with the whole repository context.
# Folder paths are set according to the repository root folder.

COPY services/replicated_log_primary/src $APP_HOME/src
COPY proto/replicated-log.proto $APP_HOME/src/main/proto/
COPY services/replicated_log_primary/*.gradle.kts $APP_HOME
COPY services/replicated_log_primary/gradle.properties $APP_HOME

RUN gradle --no-daemon build

FROM registry.access.redhat.com/ubi8/ubi-minimal:8.4

ARG JAVA_PACKAGE=java-11-openjdk-headless
ARG RUN_JAVA_VERSION=1.3.8
ARG HTTP_PORT=9090
ARG GRPC_PORT=9095

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en'
ENV HTTP_PORT=$HTTP_PORT
ENV GRPC_PORT=$GRPC_PORT

# Install java and the run-java script
# Also set up permissions for user `1001`
RUN microdnf install curl ca-certificates ${JAVA_PACKAGE} \
    && microdnf update \
    && microdnf clean all \
    && mkdir /deployments \
    && chown 1001 /deployments \
    && chmod "g+rwX" /deployments \
    && chown 1001:root /deployments \
    && curl https://repo1.maven.org/maven2/io/fabric8/run-java-sh/${RUN_JAVA_VERSION}/run-java-sh-${RUN_JAVA_VERSION}-sh.sh -o /deployments/run-java.sh \
    && chown 1001 /deployments/run-java.sh \
    && chmod 540 /deployments/run-java.sh \
    && echo "securerandom.source=file:/dev/urandom" >> /etc/alternatives/jre/conf/security/java.security

# Configure the JAVA_OPTIONS, you can add -XshowSettings:vm to also display the heap size.
ENV JAVA_OPTIONS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
# We make four distinct layers so if there are application changes the library layers can be re-used
COPY --from=builder --chown=1001 /app/build/quarkus-app/lib/ /deployments/lib/
COPY --from=builder --chown=1001 /app/build/quarkus-app/*.jar /deployments/
COPY --from=builder --chown=1001 /app/build/quarkus-app/app/ /deployments/app/
COPY --from=builder --chown=1001 /app/build/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE $HTTP_PORT
EXPOSE $GRPC_PORT
USER 1001

ENTRYPOINT [ "/deployments/run-java.sh" ]
