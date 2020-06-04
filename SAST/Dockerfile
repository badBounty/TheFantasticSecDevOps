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

RUN apk --no-cache add openjdk11
ENV JAVA_HOME /usr/lib/jvm/java-11-openjdk
RUN export JAVA_HOME

RUN apk add python3 py-pip
RUN pip3 install nodejsscan

RUN apk add --no-cache \
        bash \
        wget \
        curl \
        icu-libs \
        openssl \
        libintl \
        zlib \
        libcurl

# Install .NET Core SDK
RUN mkdir /usr/share/dotnet \
    && curl -sSL -o dotnet-install.sh https://dot.net/v1/dotnet-install.sh \
    && chmod +x dotnet-install.sh \
    && ./dotnet-install.sh --channel LTS --install-dir /usr/share/dotnet \
    && ./dotnet-install.sh --channel 2.1 --install-dir /usr/share/dotnet


ADD parseLog.py /home/parseLog.py

RUN cd /opt/sonarqube/
ADD configure.sh /opt/sonarqube/configure.sh



EXPOSE 22
ENTRYPOINT ["/opt/sonarqube/configure.sh"]

