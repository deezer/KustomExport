# Script to handle the npm stuff and be able to run typescript

# Export path for using in IDEA
export PATH=/usr/local/bin/:$PATH

# 2 gradle steps due to a possible Kotlin JsIr issue
../gradlew clean compileKotlinJs -PenableKsp=true
../gradlew jsBrowserProductionLibraryDistribution

echo Installing typescript
npm install typescript

echo Linking @kustom/Samples
cd ../build/js/packages/@kustom/Samples
npm link
cd -
npm link @kustom/Samples

echo npm i
npm i ts-node
