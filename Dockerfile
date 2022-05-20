FROM openjdk:11

EXPOSE 1985

COPY target/vmext-demo-1.0.jar mathpipeline.jar
COPY application.yaml application.yaml
# COPY lacast.config.yaml lacast.config.yaml


# COPY /LaCASt/ /LaCASt/

RUN apt update && apt install -y curl avahi-daemon wget sshpass sudo locales locales-all ssh vim expect libfontconfig1 libgl1-mesa-glx libasound2

RUN echo "en_US.UTF-8 UTF-8" > /etc/locale.gen && locale-gen
# RUN wget https://account.wolfram.com/download/public/wolfram-engine/desktop/LINUX && sudo bash LINUX -- -auto -verbose && rm LINUX
RUN mkdir my_harvests
COPY my_harvests my_harvests

# Creating a logging directory for basex specific logs.
RUN mkdir /opt/basex/ && chmod ugo+rwx /opt/basex/

ENTRYPOINT ["java", "-jar", "mathpipeline.jar"]
