# Semver

[![CircleCI](https://circleci.com/gh/owainlewis/java-http-clj.svg?style=svg)](https://circleci.com/gh/owainlewis/semver)

A pure Clojure/Script implementation of the Semantic Versioning spec. This library allows you to easily parse, validate, sort and modify semantic version strings.

http://semver.org

## Getting started

[![Clojars Project](https://img.shields.io/clojars/v/io.forward/semver.svg)](https://clojars.org/com.owainlewis/semver)

## Usage

All the examples below assume you have included the semver library like this:

```clojure
(ns 'foo
  (:require [semver.core :as s]))
```

### Parse a version string

```clojure
(s/parse "1.2.3-SNAPSHOT")

;; => semver.core.Version{:major 1, :minor 2, :patch 3, :pre-release "SNAPSHOT", :metadata nil}
```

### Sorting a list of version strings

If you want to sort a list of semantic version strings you can use the `sorted` function to do this.

```clojure
(s/sorted ["1.2.3", "1.2.3-SNAPSHOT", "2.0.0", "0.1.0-beta3"])

;; => ("2.0.0" "1.2.3" "1.2.3-SNAPSHOT" "0.1.0-beta3")
```

### Validation

You can use the `valid?` function to check if an input string is a valid semantic version

```clojure
(s/valid? "1.2.3-beta1") ;; => true

(s/valid "1.2.3.4") ;; => false
```

### Transforming a version string

A selection of modifiers are available to make it easy to modify version strings in a consistent manner. Simply pass a modifier function to the `transform` function.

```clojure
(s/transform s/increment-minor "1.0.0" ) ;; => "1.1.0"
(s/transform s/increment-major "1.0.0" ) ;; => "2.0.0"
```

## License

Copyright © 2020 Owain Lewis

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
