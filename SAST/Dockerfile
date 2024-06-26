FROM newtmitch/sonar-scanner:4.0.0-alpine as sonarscanner

FROM sonarqube:latest

RUN apk --update add --no-cache openssh bash \
  && sed -i s/#PermitRootLogin.*/PermitRootLogin\ yes/ /etc/ssh/sshd_config \
  && echo "root:root" | chpasswd \
  && rm -rf /var/cache/apk/*
RUN sed -ie 's/#Port 22/Port 22/g' /etc/ssh/sshd_config
RUN sed -ri 's/#HostKey \/etc\/ssh\/ssh_host_key/HostKey \/etc\/ssh\/ssh_host_key/g' /etc/ssh/sshd_config
RUN sed -ir 's/#HostKey \/etc\/ssh\/ssh_host_rsa_key/HostKey \/etc\/ssh\/ssh_host_rsa_key/g' /etc/ssh/sshd_config
RUN sed -ir 's/#HostKey \/etc\/ssh\/ssh_host_dsa_key/HostKey \/etc\/ssh\/ssh_host_dsa_key/g' /etc/ssh/sshd_config
RUN sed -ir 's/#HostKey \/etc\/ssh\/ssh_host_ecdsa_key/HostKey \/etc\/ssh\/ssh_host_ecdsa_key/g' /etc/ssh/sshd_config
RUN sed -ir 's/#HostKey \/etc\/ssh\/ssh_host_ed25519_key/HostKey \/etc\/ssh\/ssh_host_ed25519_key/g' /etc/ssh/sshd_config
RUN /usr/bin/ssh-keygen -A
RUN ssh-keygen -t rsa -b 4096 -f  /etc/ssh/ssh_host_key
RUN mkdir /root/.ssh
RUN cat /etc/ssh/ssh_host_key.pub >> /root/.ssh/authorized_keys
RUN cat /etc/ssh/ssh_host_key


RUN apk add maven
RUN apk add git
RUN apk add --update nodejs npm

RUN apk --no-cache add openjdk11
ENV JAVA_HOME /usr/lib/jvm/java-11-openjdk
RUN export JAVA_HOME

RUN apk add python3 py-pip

RUN apk add --no-cache \
        bash \
        wget \
        curl \
        icu-libs \
        openssl \
        libintl \
        zlib \
        libcurl

RUN apk add screen

# Install .NET Core SDK
RUN mkdir /usr/share/dotnet \
    && curl -sSL -o dotnet-install.sh https://dot.net/v1/dotnet-install.sh \
    && chmod +x dotnet-install.sh \
    && ./dotnet-install.sh --channel LTS --install-dir /usr/share/dotnet \
    && ./dotnet-install.sh --channel 2.1 --install-dir /usr/share/dotnet

RUN mkdir /home/dependency-check \
  && curl -sSL -o /home/dependency-check/dependency-check-6.0.2-release.zip https://github.com/jeremylong/DependencyCheck/releases/download/v6.0.2/dependency-check-6.0.2-release.zip \
  && unzip /home/dependency-check/dependency-check-6.0.2-release.zip -d /home/dependency-check

COPY --from=0  /usr/lib/sonar-scanner /home/sonarscanner

ADD parseLog.py /home/parseLog.py
RUN touch /home/titleNormalization.log
ADD titleNormalization.py /home/titleNormalization.py
ADD normalization.json /home/normalization.json
ADD parseNPMAuditResults.py /home/parseNPMAuditResults.py
ADD parseDependencyCheckResults.py /home/parseDependencyCheckResults.py
ADD parseNodejsscan.py /home/parseNodejsscan.py
ADD /SCA/scaNodeJS.py /home/scaNodeJS.py
ADD /SCA/scaJava.py /home/scaJava.py
ADD SonarResults.py /home/SonarResults.py

# Dependency-checks bash script
ADD DependencyCheck.sh /home/DependencyCheck.sh
# Njsscan bash script
ADD NodeScan.sh /home/NodeScan.sh

RUN chmod +x /home/DependencyCheck.sh 
RUN chmod +x /home/NodeScan.sh

RUN cd /opt/sonarqube/
ADD configure.sh /opt/sonarqube/configure.sh

# Semgremp installation
ADD SemgrepResultsParser/SemgrepParser.py /home/SemgrepParser.py

# Nuclei scanner only binary
RUN cd /home
RUN wget https://github.com/projectdiscovery/nuclei/releases/download/v2.5.0/nuclei_2.5.0_linux_amd64.zip \
    && unzip nuclei_2.5.0_linux_amd64.zip \
    && chmod +x nuclei \
    && rm nuclei_2.5.0_linux_amd64.zip

RUN mkdir Nuclei-Custom-Templates
ADD Nuclei-Custom-Templates /home/Nuclei-Custom-Templates

# Nuclei parser
ADD NucleiResultsParser/parseNucleiResults.py /home/parseNucleiResults.py

# Insider 
RUN cd /home
RUN wget https://github.com/insidersec/insider/releases/download/2.1.0/insider_2.1.0_linux_x86_64.tar.gz \
    && tar -xf insider_2.1.0_linux_x86_64.tar.gz \
    && chmod +x insider \
    && rm insider_2.1.0_linux_x86_64.tar.gz

# Flawfinder
RUN pip install flawfinder
ADD FlawfinderResultsParser/parseFlawfinderResults.py /home/parseFlawfinderResults.py

# NodeJsScan y Semgrep
RUN apk add gcc      
RUN apk add build-base
RUN apk add --no-cache gcc musl-dev python3-dev
RUN pip3 install --upgrade pip    
RUN pip3 install ujson     
RUN pip3 install --ignore-installed packaging
RUN pip3 install njsscan
RUN pip3 install semgrep

EXPOSE 22
ENTRYPOINT ["/opt/sonarqube/configure.sh"]
