# Script to build and start integration tests

# Export path for using in IDEA
export PATH=/usr/local/bin/:$PATH

../gradlew compileKotlinJs -PenableKsp=true
../gradlew jsBrowserProductionLibraryDistribution

./_run.sh