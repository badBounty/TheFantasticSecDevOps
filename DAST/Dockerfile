FROM owasp/zap2docker-stable

USER root

RUN apt-get update

RUN apt-get install -y openssh-server
RUN mkdir /var/run/sshd

RUN echo 'root:root' |chpasswd

RUN sed -ri 's/^#?PermitRootLogin\s+.*/PermitRootLogin yes/' /etc/ssh/sshd_config
RUN sed -ri 's/UsePAM yes/#UsePAM yes/g' /etc/ssh/sshd_config
RUN systemctl enable ssh
RUN mkdir /root/.ssh
RUN apt-get clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*
RUN /usr/bin/ssh-keygen -A
RUN ssh-keygen -t rsa -b 4096 -f  /etc/ssh/ssh_host_key
RUN cat /etc/ssh/ssh_host_key.pub >> /root/.ssh/authorized_keys
RUN cat /etc/ssh/ssh_host_key

USER zap

ADD ZAPScanner.py /home/zap/ZAPScanner.py

USER root

EXPOSE 22

RUN pip install python-owasp-zap-v2.4
RUN chown zap:zap /home/zap/ZAPScanner.py

ADD init.sh /zap/init.sh
RUN chown zap:zap /zap/init.sh
RUN chmod +x /zap/init.sh

