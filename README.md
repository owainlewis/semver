# semver

[![test](https://github.com/owainlewis/semver/actions/workflows/test.yml/badge.svg)](https://github.com/owainlewis/semver/actions/workflows/test.yml)

A small Clojure implementation of [Semantic Versioning 2.0.0](https://semver.org/).

It parses, validates, compares, sorts, renders, and increments semantic version
strings.

Opinion [high]: This library should stay boring and strict.
Flip fact: If the official SemVer spec changes, the parser and precedence tests
should change with it.

## Install

`deps.edn`:

```clojure
com.owainlewis/semver {:mvn/version "0.2.0-SNAPSHOT"}
```

Leiningen consumers can still depend on the Maven artifact:

```clojure
[com.owainlewis/semver "0.2.0-SNAPSHOT"]
```

The current source version is `0.2.0-SNAPSHOT`.

## Usage

```clojure
(ns example
  (:require [semver.core :as semver]))
```

Parse a version:

```clojure
(semver/parse "1.2.3-alpha.1+build.5")

;; => #semver.core.Version{:major 1N,
;;                         :minor 2N,
;;                         :patch 3N,
;;                         :pre-release "alpha.1",
;;                         :metadata "build.5"}
```

Validate a version:

```clojure
(semver/valid? "1.2.3")
;; => true

(semver/valid? "01.2.3")
;; => false

(semver/valid? "1.0.0-alpha.01")
;; => false
```

Compare versions:

```clojure
(semver/newer? "1.0.0" "1.0.0-rc.1")
;; => true

(semver/older? "1.0.0-alpha.2" "1.0.0-alpha.10")
;; => true

(semver/equal? "1.0.0+build.1" "1.0.0+build.2")
;; => true
```

Sort versions newest first:

```clojure
(semver/sorted ["1.0.0-alpha"
                "1.0.0-alpha.1"
                "1.0.0-alpha.beta"
                "1.0.0-beta"
                "1.0.0-beta.2"
                "1.0.0-beta.11"
                "1.0.0-rc.1"
                "1.0.0"])

;; => ("1.0.0"
;;     "1.0.0-rc.1"
;;     "1.0.0-beta.11"
;;     "1.0.0-beta.2"
;;     "1.0.0-beta"
;;     "1.0.0-alpha.beta"
;;     "1.0.0-alpha.1"
;;     "1.0.0-alpha")
```

Increment versions:

```clojure
(semver/transform semver/increment-patch "1.0.0-alpha+build.1")
;; => "1.0.1"

(semver/transform semver/increment-minor "1.0.9")
;; => "1.1.0"

(semver/transform semver/increment-major "1.9.9")
;; => "2.0.0"
```

## API

- `valid?`
- `parse`
- `render`
- `compare-strings`
- `newer?`
- `older?`
- `equal?`
- `snapshot?`
- `sorted`
- `increment-major`
- `increment-minor`
- `increment-patch`
- `transform`

## Spec Coverage

The test suite covers the core SemVer 2.0.0 rules:

- Required `MAJOR.MINOR.PATCH`.
- No leading zeroes in major, minor, or patch.
- Valid pre-release and build identifiers.
- No leading zeroes in numeric pre-release identifiers.
- Numeric pre-release identifiers compare numerically.
- Numeric pre-release identifiers have lower precedence than non-numeric identifiers.
- A normal version has higher precedence than a pre-release version.
- Build metadata does not affect precedence.
- Official precedence example order from the SemVer 2.0.0 spec.

## Development

Run tests:

```sh
clojure -M:test
```

Check formatting:

```sh
clojure -M:fmt/check
```

Format code:

```sh
clojure -M:fmt/fix
```

Build a jar:

```sh
clojure -T:build jar
```

Install locally:

```sh
clojure -T:build install
```

Deploy to Clojars:

```sh
CLOJARS_USERNAME=... CLOJARS_PASSWORD=... clojure -T:build deploy
```

Release from GitHub Actions:

1. Set `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` as repository secrets.
2. Create and push a version tag.

```sh
git tag v0.2.0
git push origin v0.2.0
```

The release workflow uses the tag without the leading `v` as the artifact
version.

## CI

GitHub Actions runs:

- `clojure -M:fmt/check`
- `clojure -M:test`
- `clojure -T:build jar`

CircleCI has been removed.

Leiningen has been replaced by the Clojure CLI, `deps.edn`, and
`tools.build`.

## License

Eclipse Public License 1.0.
