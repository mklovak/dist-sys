FROM node:lts-alpine

ENV APP_HOME=/app
WORKDIR $APP_HOME

# It is expected to run Docker build with the whole repository context.
# Folder paths are set according to the repository root folder.

COPY /proto/replicated-log.proto $APP_HOME/proto/replicated-log.proto
COPY /services/replicated_log_secondary_test/package.json $APP_HOME/package.json
COPY /services/replicated_log_secondary_test/package-lock.json $APP_HOME/package-lock.json
COPY /services/replicated_log_secondary_test/index.js $APP_HOME/index.js

RUN npm i

CMD node $APP_HOME/index.js
