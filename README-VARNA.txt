In order to fulfill Varna dependency:
1.  Download http://varna.lri.fr/bin/VARNAv3-93.jar
2.  Execute:
    mvn install:install-file -Dfile=/tmp/download/VARNAv3-93.jar -DgroupId=fr.lri.varna -DartifactId=varna -Dversion=3.93 -Dpackaging=jar
    (assuming the JAR path is: /tmp/download/VARNAv3-93.jar)
