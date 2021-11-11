# Script to handle the npm stuff and be able to run typescript

# Export path for using in IDEA
export PATH=/usr/local/bin/:$PATH

# 2 gradle steps due to a possible Kotlin JsIr issue
# If run locally, you can use those commands
#../gradlew clean compileKotlinJs -PenableKsp=true
#../gradlew jsBrowserProductionLibraryDistribution

echo Linking @kustom/Samples
cd ../build/js/packages/@kustom/Samples
npm link
cd -
npm link @kustom/Samples

echo Samples are linked
ls node_modules/@kustom
ls -al node_modules/@kustom/Samples
ls node_modules/@kustom/Samples

echo Installing typescript
npm install typescript --save-dev

echo Install ts-node
npm install @types/node --save-dev
npm install ts-node --save-dev
npm install
