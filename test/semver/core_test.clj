(ns semver.core-test
  (:require [clojure.test :refer [are deftest is testing]]
            [semver.core :as s])
  (:import [semver.core Version]))

(def valid-versions ["1.0.0"
                     "1.0.2-SNAPSHOT"
                     "0.3.4-alpha.rc.1"
                     "1.0.1-SNAPSHOT+meta"
                     "1.0.0-alpha"
                     "1.0.0-alpha.1"
                     "1.0.0-alpha.beta"
                     "1.0.0-beta"
                     "1.0.0-beta.2"
                     "1.0.0-beta.11"
                     "1.0.0-rc.1"
                     "1.0.0+20130313144700"
                     "1.0.0-beta+exp.sha.5114f85"])

(def invalid-versions [""
                       "1.3"
                       "3-alpha"
                       "01.0.0"
                       "1.01.0"
                       "1.0.01"
                       "1.0.0-"
                       "1.0.0+"
                       "1.0.0-alpha..1"
                       "1.0.0-alpha.01"
                       "1.0.0-01"
                       "v1.2.3"
                       "1.2.3.4"])

(def precedence-order ["1.0.0-alpha"
                       "1.0.0-alpha.1"
                       "1.0.0-alpha.beta"
                       "1.0.0-beta"
                       "1.0.0-beta.2"
                       "1.0.0-beta.11"
                       "1.0.0-rc.1"
                       "1.0.0"])

(deftest valid?-test
  (testing "should return true for valid semvers"
    (is (every? true? (map s/valid? valid-versions))))
  (testing "should return false for invalid semvers"
    (is (every? false? (map s/valid? invalid-versions))))
  (testing "should return false for nil"
    (is (false? (s/valid? nil)))))

(deftest parse-test
  (testing "should parse valid semver"
    (is (instance? Version (s/parse "1.2.3-foo+bar")))
    (is (instance? Version (s/parse "1.2.3-SNAPSHOT")))
    (is (instance? Version (s/parse "1.2.3-alpha.1+foo.bar")))
    (is (instance? Version (s/parse "1.2.3"))))
  (testing "should return nil for invalid semver"
    (is (nil? (s/parse "1.2")))
    (is (nil? (s/parse "1.0.0-alpha.01")))))

(deftest snapshot?-test
  (testing "should return true if version is snapshot"
    (is (s/snapshot? "1.0.0-SNAPSHOT")))
  (testing "should return false if version is not snapshot"
    (is (not (s/snapshot? "1.0.0-alpha.1")))))

(deftest newer?-test
  (testing "should return true if the first version is newer than the second version"
    (is (s/newer? "1.0.1" "1.0.0"))
    (is (s/newer? "2.0.1" "1.0.1"))
    (is (s/newer? "2.0.0" "1.0.0-alpha.1"))
    (is (s/newer? "1.1.0" "1.0.5"))
    (is (s/newer? "1.0.0-alpha.10" "1.0.0-alpha.2"))
    (is (s/newer? "1.0.0-alpha" "1.0.0-1"))))

(deftest semver-precedence-test
  (testing "should follow the official SemVer 2.0.0 precedence example"
    (doseq [[older newer] (partition 2 1 precedence-order)]
      (is (s/older? older newer)
          (str older " should be older than " newer))))
  (testing "build metadata should not affect precedence"
    (is (s/equal? "1.0.0+build.1" "1.0.0+build.2"))
    (is (s/equal? "1.0.0-alpha+build.1" "1.0.0-alpha+build.2"))))

(deftest invalid-comparison-test
  (testing "should reject invalid versions when comparing"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo
         #"Invalid semantic version"
         (s/compare-strings "1.0.0" "01.0.0")))))

(deftest sorted-test
  (testing "should sort a list of versions start with newest first"
    (is (= (s/sorted ["1.0.2" "1.0.1-SNAPSHOT" "1.0.1" "1.3.0"])
           ["1.3.0" "1.0.2" "1.0.1" "1.0.1-SNAPSHOT"])))
  (testing "should sort the official precedence examples newest first"
    (is (= (s/sorted precedence-order)
           (reverse precedence-order)))))

(deftest render-test
  (testing "should convert from version to string"
    (is (= (s/render (s/parse "1.0.0-beta+foo")) "1.0.0-beta+foo"))
    (is (= (s/render (s/parse "1.0.0-SNAPSHOT")) "1.0.0-SNAPSHOT"))
    (is (= (s/render (s/parse "1.0.0")) "1.0.0"))))

(deftest transform-test
  (testing "Should increment versions correctly"
    (are [version incr-fn desired-version] (= desired-version (s/transform incr-fn version))

      "0.0.1" s/increment-patch "0.0.2"
      "0.1.0" s/increment-minor "0.2.0"
      "0.1.1" s/increment-minor "0.2.0"
      "0.1.10" s/increment-major "1.0.0"
      "1.0.0-alpha+build.1" s/increment-patch "1.0.1")))
