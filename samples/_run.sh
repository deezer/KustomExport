export PATH=/usr/local/bin/:$PATH

echo npx ts-node --show-config
npx ts-node --show-config
echo "---"

for file in $(find src/commonMain -type f -name "*.ts")
do
  if [[ $file =~ "shared_ts" ]]; then
    # ignore those files
    echo "@@ Ignore file $file"
  else
    echo "@@ npx ts-node $file"
    node_modules/ts-node/dist/bin.js $file
    #npx ts-node $file
  fi
done