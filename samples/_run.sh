export PATH=/usr/local/bin/:$PATH

for file in $(find src/commonMain -type f -name "*.ts")
do
  if [[ ! $file =~ "shared_ts" ]]; then
    node_modules/ts-node/dist/bin.js $file
    #npx ts-node $file
  fi
done