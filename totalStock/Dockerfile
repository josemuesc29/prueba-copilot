FROM maven:3.8.1-openjdk-17-slim
COPY /target/*.jar /usr/share/app.jar
COPY --from=datadog/serverless-init:1 /datadog-init /app/datadog-init
ADD 'https://dtdg.co/latest-java-tracer' /dd_tracer/java/dd-java-agent.jar
ENV DD_SITE=datadoghq.com
ENV DD_SERVICE=shopping-cart
ENV DD_TRACE_ENABLED=true
ENV DD_LOGS_ENABLED=false
ENV DD_LOGS_INJECTION=false
ENV DD_ENV=production_co
ENTRYPOINT ["/app/datadog-init"]
CMD ["java", "-jar", "/usr/share/app.jar"]
#ENV SPRING_PROFILES_ACTIVE="local"