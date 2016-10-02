(ns semver.core)
;; A Clojure implementation of the semver specification
;;
;; A library for parsing and sorting semantic version
;;
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

(defn pre-release?
  "Returns true if the input version is a pre-release i.e 1.2.3-alpha.1"
  [version]
  (some? (:pre-release version)))

(defn snapshot?
  "Returns true if the input version is a snapshot else false"
  [version]
  (= "SNAPSHOT" (:pre-release version)))

(defn compare-part
  "identifiers consisting of only digits are compared numerically and
   identifiers with letters or hyphens are compared lexically in ASCII sort order.
   Numeric identifiers always have lower precedence than non-numeric identifiers."
  [x y]
  ;; TODO
  0)

(defn compare-split-parts
  "Precedence for two pre-release versions with the same major, minor, and patch version MUST
   be determined by comparing each dot separated identifier from left to right until a difference is
   found as follows: identifiers consisting of only digits are compared numerically and
   identifiers with letters or hyphens are compared lexically in ASCII sort order.
   Numeric identifiers always have lower precedence than non-numeric identifiers.
   A larger set of pre-release fields has a higher precedence than a smaller set,
   if all of the preceding identifiers are equal"
  [x y]
  (let [[x-parts y-parts] (map (fn [s]
                                 (->> (clojure.string/split s #"[.]")
                                      (remove clojure.string/blank?)))
                                      [x y])]
    (loop [xs x-parts ys y-parts]
      (cond
        (and (empty? xs) (seq? ys)) 1
        (and (empty? xs) (empty? ys)) 0
        (and (seq xs) (empty? ys)) -1
        :else
          (let [fx (first xs) fy (first ys)]
            (if (= fx fy)
              ;; If they are equal move on to the next part
              (recur (rest xs) (rest ys))
              ;; If they are not equal compare them
              (compare-part fx fy)))))))

(defn compare-pre-release [x y]
  ;; When major, minor, and patch are equal, a pre-release version has lower precedence than a normal version
  (cond
    (and (nil? x) (some? y)) 1
    (and (nil? x) (nil? y)) 0
    (and (some? x) (y nil?)) -1
    ;; Comparing each dot separated identifier from left to right until a difference is found
    :else (compare-split-parts x y)))

(defn compare-semver
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

;; Sorting
(defn sort-by-semver [versions])
