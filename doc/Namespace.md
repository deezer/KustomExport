# Namespace

## Problem

Typescript generated code defines the namespace from the package name.

```kotlin
export namespace com.deezer.sample.my_project.features.share_feat.dto {
    class ShareDto {
```
From a Typescript usage, it makes it difficult to operate with such long names.

## Solution

```kotlin
ksp {
    arg("erasePackage", "true")
}
```
This will generate all your code in the empty package, removing entirely the namespace for Typescript.

As a result, it can produce naming collision.
