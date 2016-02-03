BUILDING eHour
=====================


IntelliJ IDEA Project
----------------
- Open pom.xml in root project directory and let Maven figure out dependencies

General Maven Build
----------------
*Note: Test cases are skipped. Changes are verified by manual testing, instead of original test cases or automatic testing*

- First-Time Build

mvn clean install -DskipTests -Pprod

- Later Build After Updates/Changes

mvn clean install -DskipTests -Pprod -Dmaven.test.skip -Dmaven.test.skip.exec

- If referring private Artifactory repository

May need options -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true if SSL certificate is self-signed


Compatibility with orignal eHour Releases / Databases
----------------
Not compatible, since database table structures have been altered. 


Deploy within Tomcat 7
----------------
0. Deploy database and initialize tables with scripts provided. Also prepare EHOUR_HOME directory as original version
1. If war is built successfully, it could be found in eHour-web/target/, named eHour-web-1.4.3.war.
2. Deploy war to Tomcat's webapps/ directory

