FROM java:7
COPY . /usr/src/char
WORKDIR /usr/src/char
CMD ["java", "-jar", "char.jar", "0.0.0.0", "9316"]
