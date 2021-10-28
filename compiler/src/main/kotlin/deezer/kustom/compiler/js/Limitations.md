# Limitations

- define all patterns / details on patterns (ex: add a method on an enum -> compilo doesn't export it by default today)

# Hacks

- KSP task defined on gradle (tricks on gradle + 1 trick on compiler)

# TODO

- fix gradle ksp to make it run properly
- exceptions
- sealed class (could be a future addition)
- lambdas in signature
- apply on all other modules (and find more cases to implement)
- (opti/bonus) flag useless import/export to strip JS code

# Pros & cons

- more complex than manual (requires a bit of language theory, looks reasonable to me)
- avoid manual typing errors
- can validate strict format (like ktlint, we can block a build if something is weird)
