[ -d target ] && rm -rf target
mkdir target
cp ../../../../georegistry-web/target/georegistry.war target/georegistry.war

docker build -t georegistry .
