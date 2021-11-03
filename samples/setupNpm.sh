# Script to handle the npm stuff and be able to run typescript

# Export path for using in IDEA
export PATH=/usr/local/bin/:$PATH

# 2 gradle steps due to a possible Kotlin JsIr issue
../gradlew clear compileKotlinJs -PenableKsp=true
../gradlew jsBrowserProductionLibraryDistribution

#cd build/productionLibrary
#cd ../build/compileSync/main/productionLibrary/kotlin/@kustom
cd ../build/js/packages/@kustom/Samples
npm link
cd -
npm link @kustom/Samples

npm i ts-node