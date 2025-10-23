FROM rabbitmq:3.13-management-alpine

LABEL maintainer="OOTD Team5" \
      org.opencontainers.image.source="https://example.com/ootd-team5" \
      org.opencontainers.image.description="RabbitMQ broker image configured for distributed deployments."

ENV RABBITMQ_CONFIG_FILE=/etc/rabbitmq/rabbitmq.conf \
    RABBITMQ_ENABLED_PLUGINS_FILE=/etc/rabbitmq/enabled_plugins \
    RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS="+sbwt none +sbwtdcpu none +sbwtdio none"

COPY rabbitmq.conf /etc/rabbitmq/rabbitmq.conf
COPY enabled_plugins /etc/rabbitmq/enabled_plugins
COPY definitions.json /etc/rabbitmq/definitions.json

RUN rabbitmq-plugins list >/dev/null \
 && chown -R rabbitmq:rabbitmq /etc/rabbitmq

EXPOSE 5672 15672 15692 61613

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=5 \
  CMD rabbitmq-diagnostics -q ping || exit 1
