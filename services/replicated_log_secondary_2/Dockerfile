FROM mozilla/sbt as builder

ENV APP_HOME=/app
WORKDIR $APP_HOME

COPY services/replicated_log_secondary_2/src $APP_HOME/src
COPY services/replicated_log_secondary_2/project/build.properties $APP_HOME/project/
COPY services/replicated_log_secondary_2/project/plugins.sbt $APP_HOME/project/
COPY services/replicated_log_secondary_2/build.sbt $APP_HOME/

COPY proto/replicated-log.proto $APP_HOME/src/main/proto/

RUN sbt stage

ENTRYPOINT ["/app/target/universal/stage/bin/replicated_log_secondary_2"]
