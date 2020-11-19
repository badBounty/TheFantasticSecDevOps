FROM jenkins/jenkins:lts

ARG node=no
ARG Net=no
ARG JAVA=no
ARG MAVEN_VERSION=3.5.4

USER root

## Common Packages
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl libunwind8 gettext apt-transport-https ca-certificates \
    gnupg2 \
    software-properties-common \
    && curl -fsSL https://download.docker.com/linux/$(. /etc/os-release; echo "$ID")/gpg > /tmp/dkey; apt-key add /tmp/dkey \
    &&  add-apt-repository \
    "deb [arch=amd64] https://download.docker.com/linux/$(. /etc/os-release; echo "$ID") \
    $(lsb_release -cs) \
    stable" \
    && apt-get update \
    && apt-get -y install docker-ce \
    && usermod -a -G docker jenkins

## JAVA installation
RUN if [ "$JAVA" = "yes" ] ; then \
    apt-get update \
    && wget https://download.java.net/java/GA/jdk11/9/GPL/openjdk-11.0.2_linux-x64_bin.tar.gz \
    && tar xfvz openjdk-11.0.2_linux-x64_bin.tar.gz \
    && chmod 777 -R /jdk-11.0.2 \
    && java -version \
    && curl -fsSL https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar xzf - -C /usr/share \
    && mv /usr/share/apache-maven-$MAVEN_VERSION /usr/share/maven \
    && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn; \
    fi

## Node installation
RUN if [ "$node"  = "yes" ] ; then \
    curl -sL https://deb.nodesource.com/setup_14.x | bash - \
    && apt-get install --yes nodejs \
    && apt-get install --yes build-essential \
    && node --version \
    && npm --version; \
    fi

## .Net Core Installation
RUN if [ "$Net"  = "yes" ] ; then \
    curl https://packages.microsoft.com/keys/microsoft.asc | gpg --dearmor > microsoft.gpg \
    && mv microsoft.gpg /etc/apt/trusted.gpg.d/microsoft.gpg \
    && sh -c 'echo "deb [arch=amd64] https://packages.microsoft.com/repos/microsoft-debian-stretch-prod stretch main" > /etc/apt/sources.list.d/dotnetdev.list' \
    && apt-get update \
    && apt-get install -y dotnet-sdk-3.1 \
    && dotnet tool install --global dotnet-sonarscanner --version 4.8.0 \
    && chmod 777 -R /root \
    && ls -l /root/.dotnet/tools; \
    fi

RUN chmod 777 -R /etc/profile.d

ADD PipelineScripts /var/jenkins_home/PipelineScripts

RUN ls -l /etc/profile.d/

USER jenkins

ENV PATH "$PATH:/jdk-11.0.2/bin:/root/.dotnet/tools"
ENV JAVA_HOME "/jdk-11.0.2"
ENV MAVEN_VERSION=${MAVEN_VERSION}
ENV M2_HOME /usr/share/maven
ENV maven.home $M2_HOME
ENV M2 $M2_HOME/bin
ENV PATH $M2:$PATH