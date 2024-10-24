(ns semver.core
  (:require [clojure.string :as str]))

(defrecord Version
           [major minor patch pre-release metadata])

(def ^{:private true} semver
  #"^(\d+)\.(\d+)\.(\d+)(?:-([0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?(?:\+([0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?$")

(defn valid?
  "Returns true if an input string is a valid semantic version string"
  [^String version]
  (boolean (re-matches semver version)))

(defn- parse-int
  [s]
  #?(:clj (Integer/parseInt s 10)
     :cljs (js/parseInt s)))

(defn parse
  "Parse a semantic version string returning nil if the input is invalid
   or a Version if the input is valid"
  [^String version]
  (when (valid? version)
    (let [[[_ major minor patch pre-release metadata]] (re-seq semver version)
          major-version (parse-int major)
          minor-version (parse-int minor)
          patch-version (parse-int patch)]
      (Version. major-version minor-version patch-version pre-release metadata))))

(defn render
  "Takes a semantic version type and renders it back as a string"
  [^Version version]
  (let [{:keys [major minor patch pre-release metadata]} version]
    (cond
      (boolean (and (some? major) (some? minor) (some? patch) (some? pre-release) (some? metadata)))
      (str major "." minor "." patch "-" pre-release "+" metadata)
      (boolean (and (some? major) (some? minor) (some? patch) (some? pre-release) (nil? metadata)))
      (str major "." minor "." patch "-" pre-release)
      (boolean (and (some? major) (some? minor) (some? patch) (nil? pre-release) (nil? metadata)))
      (str major "." minor "." patch)
      :default nil)))

(defn- compare-part
  "Identifiers consisting of only digits are compared numerically and
   identifiers with letters or hyphens are compared lexically in ASCII sort order.
   Numeric identifiers always have lower precedence than non-numeric identifiers."
  [x y]
  (compare x y))

(defn- compare-split-parts
  "Precedence for two pre-release versions with the same major, minor, and patch version MUST
   be determined by comparing each dot separated identifier from left to right until a difference is
   found as follows: identifiers consisting of only digits are compared numerically and
   identifiers with letters or hyphens are compared lexically in ASCII sort order.
   Numeric identifiers always have lower precedence than non-numeric identifiers.
   A larger set of pre-release fields has a higher precedence than a smaller set,
   if all of the preceding identifiers are equal"
  [x y]
  (let [[x-parts y-parts] (map (fn [s]
                                 (->> (str/split s #"[.]")
                                      (remove str/blank?)))
                               [x y])]
    (loop [xs x-parts ys y-parts]
      (cond
        (and (empty? xs) (seq? ys)) 1
        (and (empty? xs) (empty? ys)) 0
        (and (seq xs) (empty? ys)) -1
        :else
        (let [fx (first xs) fy (first ys)]
          (if (= fx fy)
            (recur (rest xs) (rest ys))
            (compare-part fx fy)))))))

(defn- is-snapshot?
  "Returns true if the input version is a snapshot else false"
  [pre-release]
  (= "SNAPSHOT" pre-release))

(defn- compare-pre-release
  "When major, minor, and patch are equal, a pre-release version has lower precedence than a normal version"
  [x y]
  (cond
    (and (is-snapshot? x) (not (is-snapshot? y))) -1
    (and (not (is-snapshot? x)) (is-snapshot? y)) 1
    (and (nil? x) (some? y)) 1
    (and (nil? x) (nil? y)) 0
    (and (some? x) (nil? y)) -1
    ;; Comparing each dot separated identifier from left to right until a difference is found
    :else (compare-split-parts x y)))

(defn- compare-semver
  "Compare two semantic versions
   Build metadata does not figure into precedence
   Precedence is determined by the first difference when comparing each of these identifiers from left to right
   as follows: Major, minor, and patch versions are always compared numerically.
   Example: 1.0.0 < 2.0.0 < 2.1.0 < 2.1.1"
  [v1 v2]
  (if (= (:major v1) (:major v2))
    (if (= (:minor v1) (:minor v2))
      (if (= (:patch v1) (:patch v2))
        (compare-pre-release (:pre-release v1) (:pre-release v2))
        (compare (:patch v1) (:patch v2)))
      (compare (:minor v1) (:minor v2)))
    (compare (:major v1) (:major v2))))

(defn compare-strings
  "Compare two semantic version strings"
  [^String v1 ^String v2]
  (compare-semver (parse v1) (parse v2)))

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
  (sort newer? versions))

(defn increment-major
  "Returns a copy of a given version with the major version incremented"
  [^Version version]
  (-> version
      (update :major inc)
      (assoc :minor 0)
      (assoc :patch 0)))

(defn increment-minor
  "Returns a copy of the given version with the minor version incremented"
  [^Version version]
  (-> version
      (update :minor inc)
      (assoc :patch 0)))

(defn increment-patch
  "Returns a copy of the given version with the patch version incremented"
  [^Version version]
  (update version :patch inc))

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
