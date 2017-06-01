@echo off
java -Xmx32M -Dlogging.config="./config/logback.xml" -jar "@projectName@-@projectVersion@.jar"