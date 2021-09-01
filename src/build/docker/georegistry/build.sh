[ -f georegistry.war ] && rm -f georegistry.war
cp ../../../../georegistry-web/target/georegistry.war georegistry.war

docker build -t georegistry .
