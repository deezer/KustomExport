for file in $(find src/commonMain -type f -name "*.ts")
do
  npx ts-node $file
done