export PATH=/usr/local/bin/:$PATH

echo
echo
echo via node_modules
node_modules/ts-node/dist/bin.js --show-config

echo
echo
echo via nox
npx ts-node --show-config

for file in $(find src/commonMain -type f -name "*.ts")
do
  if [[ $file =~ "shared_ts" ]]; then
    # ignore those files
    echo "@@ Ignore file $file"
  else
    echo "@@ npx ts-node $file"
    ls -al $file
    node_modules/ts-node/dist/bin.js $file
    #npx ts-node $file
  fi
done