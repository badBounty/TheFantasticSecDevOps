RUN curl -fsSL https://artifacts.elastic.co/GPG-KEY-elasticsearch | sudo apt-key add -
&& echo "deb https://artifacts.elastic.co/packages/7.x/apt stable main" | sudo tee -a /etc/apt/sources.list.d/elastic-7.x.list
&& sudo apt update
&& sudo apt install elasticsearch

RUN sudo systemctl start elasticsearch