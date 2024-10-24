(ns semver.core-test
  (:require [clojure.test :as test :refer [deftest testing is are]]
            #?(:cljs [semver.core :as s :refer [Version]]
               :clj [semver.core :as s]))
  #?(:clj
     (:import [semver.core Version])))

(def valid-versions ["1.0.0"
                     "1.0.2-SNAPSHOT"
                     "0.3.4-alpha.rc.1"
                     "1.0.1-SNAPSHOT+meta"])

(def invalid-versions ["1.3", "3-alpha"])

(deftest valid?-test
  (testing "should return true for valid semvers"
    (is (every? true? (map s/valid? valid-versions))))
  (testing "should return false for invalid semvers"
    (is (every? false? (map s/valid? invalid-versions)))))

(deftest parse-test
  (testing "should parse valid semver"
    (is (instance? Version (s/parse "1.2.3-foo+bar")))
    (is (instance? Version (s/parse "1.2.3-SNAPSHOT")))
    (is (instance? Version (s/parse "1.2.3-alpha.1+foo.bar")))
    (is (instance? Version (s/parse "1.2.3"))))
  (testing "should return nil for invalid semver"
    (is (nil? (s/parse "1.2")))))

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
    (is (s/newer? "1.1.0" "1.0.5"))))

(deftest sorted-test
  (testing "should sort a list of versions start with newest first"
    (is (= (s/sorted ["1.0.2" "1.0.1-SNAPSHOT" "1.0.1" "1.3.0"])
           ["1.3.0" "1.0.2" "1.0.1" "1.0.1-SNAPSHOT"]))))

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
      "0.1.10" s/increment-major "1.0.0")))

#?(:cljs (test/run-tests))
