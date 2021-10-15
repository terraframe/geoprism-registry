([ -d target ] && rm -rf target) || true
mkdir target
cp ../../../../georegistry-web/target/georegistry.war target/georegistry.war
cp -R ../../../../envcfg/prod target/appcfg

docker build -t georegistry .
docker save georegistry:latest | gzip > target/georegistry.dimg.gz
