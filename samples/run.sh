# Script to build and start integration tests

# Export path for using in IDEA
export PATH=/usr/local/bin/:$PATH

../gradlew compileKotlinJs -PenableKsp=true --rerun-tasks --no-build-cache && \
  ../gradlew jsBrowserProductionLibraryDistribution && \
  npm i && \
  ./_run.sh


