FROM ubuntu
RUN apt-get update -y && \
    apt-get install -y --no-install-recommends default-jre-headless && \
    rm -rf /var/lib/apt/lists/
COPY colorbar.py \
     colorbars.R \
     distmat.R \
     mcq-global \
     mcq-lcs \
     mcq-local \
     mcq-print \
     /usr/local/bin/
COPY target/mcq-cli-*-jar-with-dependencies.jar \
     /usr/local/bin/target/mcq-cli-latest-jar-with-dependencies.jar
