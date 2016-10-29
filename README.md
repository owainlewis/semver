# semver

A Clojure implementation of the Semantic Versioning spec. This library allows you to parse, sort and modify semantic versions.

## Usage

```
(ns 'foo'
  (:require [semver.core :as s]))

;; Sorting

(s/sort-by-semver ["1.2.3", "1.2.3-SNAPSHOT", "2.0.0", "0.1.0-beta3"])

;; ("2.0.0" "1.2.3" "1.2.3-SNAPSHOT" "0.1.0-beta3")


```

## License

Copyright © 2016 Owain Lewis

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
