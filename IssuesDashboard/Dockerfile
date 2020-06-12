FROM mcr.microsoft.com/dotnet/core/sdk:3.1 as build-env

WORKDIR /app

COPY *.csproj ./
RUN dotnet restore

COPY . ./
RUN dotnet publish -c Release -o out


FROM mcr.microsoft.com/dotnet/core/aspnet:3.1

WORKDIR /app

RUN mkdir -p /usr/share/man/man1
RUN apt update \
    && apt install -y default-jdk

ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64

RUN echo $JAVA_HOME


ENV EK_VERSION=7.7.0

ENV ASPNETCORE_URLS=http://+:5000

RUN apt-get update -qq >/dev/null 2>&1 \
    && apt-get install wget sudo -qqy >/dev/null 2>&1 \
    && useradd -m -s /bin/bash elasticsearch \
    && echo elasticsearch ALL=NOPASSWD: ALL >/etc/sudoers.d/elasticsearch \
    && chmod 440 /etc/sudoers.d/elasticsearch \
    && chmod -R 777 /app

USER elasticsearch

WORKDIR /home/elasticsearch

RUN wget -q -O - https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-oss-${EK_VERSION}-no-jdk-linux-x86_64.tar.gz | tar -zx \
    && mkdir -p elasticsearch-${EK_VERSION}/data \
    && wget -q -O - https://artifacts.elastic.co/downloads/kibana/kibana-oss-${EK_VERSION}-linux-x86_64.tar.gz | tar -zx

WORKDIR /app
EXPOSE 5000
EXPOSE 9200 5601
COPY --from=build-env /app/out .

CMD /home/elasticsearch/elasticsearch-${EK_VERSION}/bin/elasticsearch -E http.host=0.0.0.0 --quiet & /home/elasticsearch/kibana-${EK_VERSION}-linux-x86_64/bin/kibana --allow-root --host 0.0.0.0 -Q & dotnet IssuesDashboard.dll