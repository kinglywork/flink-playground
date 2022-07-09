FROM openjdk:11-jdk@sha256:3432e5c27a4c5b56b717b06669414458ea5142cda2fb80e5fe8049e1cbe8b582

ARG sbt_version

RUN apt-get update && \
    apt-get install -y jq netcat && \
    rm -rf /var/lib/apt/lists/* /var/tmp/*

RUN curl -f -L -o /usr/local/bin/sbt https://github.com/paulp/sbt-extras/raw/master/sbt && \
    chmod 755 /usr/local/bin/sbt && \
    sbt -sbt-version $sbt_version -script /dev/null

WORKDIR /work
COPY build.sbt ./
COPY project/build.properties project/plugins.sbt ./project/
RUN sbt update
