FROM jboss/wildfly:latest

COPY build/libs/starship-0.0.1.war /opt/jboss/wildfly/standalone/deployments/
COPY src/main/resources/truststore/keystore.jks /keystore.jks

