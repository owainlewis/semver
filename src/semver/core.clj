(ns semver.core)

(defrecord Version [major minor patch pre-release metadata])

(def ^{:private true} semver
  #"^(\d+)\.(\d+)\.(\d+)(?:-([0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?(?:\+([0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?$")

(defn valid?
  "Returns true if an input string is a valid semantic version string"
  [version]
  (boolean (re-matches semver version)))

(defn parse
  "Parse a semantic version string returning nil if the input is invalid
   or a Version if the input is valid"
  [version]
  (when (valid? version)
    (let [[[_ major minor patch pre-release metadata]] (re-seq semver version)
          major-version (Integer/parseInt major 10)
          minor-version (Integer/parseInt minor 10)
          patch-version (Integer/parseInt patch 10)]
      (Version. major-version minor-version patch-version pre-release metadata))))

(defn pre-release? [version]
  (some? (:pre-release version)))

(defn snapshot? [version]
  (= "SNAPSHOT" (:pre-release version)))

;; Rules
;; Build metadata does not figure into precedence
;; Precedence is determined by the first difference when comparing each of these identifiers from left to right
;; as follows: Major, minor, and patch versions are always compared numerically.
;; Example: 1.0.0 < 2.0.0 < 2.1.0 < 2.1.1.
;; When major, minor, and patch are equal, a pre-release version has lower precedence than a normal version.
;; a pre-release version has lower precedence than a normal version.
;; Precedence for two pre-release versions with the same major, minor, and patch version MUST be determined by comparing each dot separated identifier from left to right until a difference is found as follows: identifiers consisting of only digits are compared numerically and identifiers with letters or hyphens are compared lexically in ASCII sort order. Numeric identifiers always have lower precedence than non-numeric identifiers. A larger set of pre-release fields has a higher precedence than a smaller set, if all of the preceding identifiers are equal

;; TODO
(defn compare-pre-release [x y] 0)

(defn compare-semver [v1 v2]
  (if (= (:major v1) (:major v2))
    (if (= (:minor v1) (:minor v2))
      (if (= (:patch v1) (:patch v2))
        (compare-pre-release (:pre-release v1) (:pre-release v2))
        (compare (:patch v1) (:patch v2)))
      (compare (:minor v1) (:minor v2)))
    (compare (:major v1) (:major v2))))

;; Comparision

(defn newer?
  "Returns true if v1 is newer than v2 else false"
  [v1 v2]
  (pos? (compare-semver v1 v2)))

(defn older?
  "Returns true if v1 is older than v2 else false"
  [v1 v2]
  (neg? (compare-semver v1 v2)))

(defn equal?
  "Returns true if v1 is equal to v2 else false"
  [v1 v2]
  (zero? (compare-semver v1 v2)))

;; Misc

(defn increment-major [version])
(defn increment-minor [version])
(defn increment-patch [version])

;; Sorting
(defn sort-by-semver [versions])
