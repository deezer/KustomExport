# Script to build and start integration tests

# Export path for using in IDEA
export PATH=/usr/local/bin/:$PATH

../gradlew compileKotlinJs -PenableKsp=true --rerun-tasks --no-build-cache && \
  ../gradlew jsBrowserProductionLibraryDistribution && \
  cd ../build/js/packages/@kustom/Samples && \
  npm link && \
  cd - && \
  npm link @kustom/Samples && \
  ./_run.sh


