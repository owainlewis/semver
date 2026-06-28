(ns semver.core
  (:require [clojure.string :as str]))

(defrecord Version
           [major minor patch pre-release metadata])

(def ^:private numeric-identifier
  #"0|[1-9][0-9]*")

(def ^:private non-numeric-identifier
  #"[0-9A-Za-z-]*[A-Za-z-][0-9A-Za-z-]*")

(def ^:private pre-release-identifier
  (str "(?:" numeric-identifier "|" non-numeric-identifier ")"))

(def ^{:private true} semver
  (re-pattern
   (str "^(0|[1-9][0-9]*)"
        "\\.(0|[1-9][0-9]*)"
        "\\.(0|[1-9][0-9]*)"
        "(?:-(" pre-release-identifier
        "(?:\\." pre-release-identifier ")*))?"
        "(?:\\+([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?$")))

(defn- numeric-identifier?
  [identifier]
  (boolean (re-matches numeric-identifier identifier)))

(defn- parse-number
  [s]
  (bigint s))

(defn valid?
  "Return true if version is a valid Semantic Versioning 2.0.0 string."
  [version]
  (boolean
   (and (string? version)
        (re-matches semver version))))

(defn parse
  "Parse version into a Version record.

  Return nil when version is not a valid Semantic Versioning 2.0.0 string."
  [version]
  (when-let [[_ major minor patch pre-release metadata] (and (string? version)
                                                             (re-matches semver version))]
    (->Version (parse-number major)
               (parse-number minor)
               (parse-number patch)
               pre-release
               metadata)))

(defn render
  "Render a Version record as a semantic version string."
  [^Version version]
  (let [{:keys [major minor patch pre-release metadata]} version]
    (when (and (some? major) (some? minor) (some? patch))
      (str major "." minor "." patch
           (when pre-release
             (str "-" pre-release))
           (when metadata
             (str "+" metadata))))))

(defn- compare-part
  "Compare one pre-release identifier by SemVer 2.0.0 precedence rules."
  [x y]
  (let [x-numeric? (numeric-identifier? x)
        y-numeric? (numeric-identifier? y)]
    (cond
      (and x-numeric? y-numeric?) (compare (parse-number x) (parse-number y))
      x-numeric? -1
      y-numeric? 1
      :else (compare x y))))

(defn- compare-split-parts
  "Compare dot-separated pre-release identifiers."
  [x y]
  (let [[x-parts y-parts] (map (fn [s] (str/split s #"\."))
                               [x y])]
    (loop [xs x-parts ys y-parts]
      (cond
        (and (empty? xs) (empty? ys)) 0
        (empty? xs) -1
        (empty? ys) 1
        :else
        (let [fx (first xs) fy (first ys)]
          (let [comparison (compare-part fx fy)]
            (if (zero? comparison)
              (recur (rest xs) (rest ys))
              comparison)))))))

(defn- is-snapshot?
  "Returns true if the input version is a snapshot else false"
  [pre-release]
  (= "SNAPSHOT" pre-release))

(defn- compare-pre-release
  "Compare two optional pre-release strings."
  [x y]
  (cond
    (and (nil? x) (some? y)) 1
    (and (nil? x) (nil? y)) 0
    (and (some? x) (nil? y)) -1
    :else (compare-split-parts x y)))

(defn- compare-semver
  "Compare two semantic versions
   Build metadata does not figure into precedence
   Precedence is determined by the first difference when comparing each of these identifiers from left to right
   as follows: Major, minor, and patch versions are always compared numerically.
   Example: 1.0.0 < 2.0.0 < 2.1.0 < 2.1.1"
  [v1 v2]
  (let [core-comparison (compare [(:major v1) (:minor v1) (:patch v1)]
                                 [(:major v2) (:minor v2) (:patch v2)])]
    (if (zero? core-comparison)
      (compare-pre-release (:pre-release v1) (:pre-release v2))
      core-comparison)))

(defn- parse!
  [version]
  (or (parse version)
      (throw (ex-info "Invalid semantic version" {:version version}))))

(defn compare-strings
  "Compare two semantic version strings.

  Return a negative number when v1 is older, zero when v1 has the same
  precedence, and a positive number when v1 is newer. Build metadata is ignored
  for precedence."
  [^String v1 ^String v2]
  (compare-semver (parse! v1) (parse! v2)))

(defn newer?
  "Returns true if v1 is newer than v2 else false"
  [^String v1 ^String v2]
  (pos? (compare-strings v1 v2)))

(defn older?
  "Returns true if v1 is older than v2 else false"
  [^String v1 ^String v2]
  (neg? (compare-strings v1 v2)))

(defn equal?
  "Returns true if v1 is equal to v2 else false"
  [^String v1 ^String v2]
  (zero? (compare-strings v1 v2)))

(defn snapshot? [^String version]
  (boolean
   (when-let [pr (:pre-release (parse version))]
     (is-snapshot? pr))))

(defn sorted
  "Given a list of semantic version strings, compare them and return them in sorted order
   with newest versions first"
  [versions]
  (sort #(compare-strings %2 %1) versions))

(defn increment-major
  "Returns a copy of a given version with the major version incremented"
  [^Version version]
  (-> version
      (update :major inc)
      (assoc :minor 0)
      (assoc :patch 0)
      (assoc :pre-release nil)
      (assoc :metadata nil)))

(defn increment-minor
  "Returns a copy of the given version with the minor version incremented"
  [^Version version]
  (-> version
      (update :minor inc)
      (assoc :patch 0)
      (assoc :pre-release nil)
      (assoc :metadata nil)))

(defn increment-patch
  "Returns a copy of the given version with the patch version incremented"
  [^Version version]
  (-> version
      (update :patch inc)
      (assoc :pre-release nil)
      (assoc :metadata nil)))

(defn transform
  "Transform a version string by applying a modifier function

   This might typically be used to alter the version in some way like incrementing a version part
   or adding something to the metadata or pre-release parts

   Example:
     (transform \"1.0.0\" increment-major)
  "
  [modifier version]
  (when-let [parsed-version (parse version)]
    (render (apply modifier [parsed-version]))))
