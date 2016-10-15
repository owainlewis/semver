(ns semver.core-test
  (:require [clojure.test :refer :all]
            [semver.core :refer :all])
  (:import [semver.core Version]))

(def valid-versions [
                     "1.0.0"
                     "1.0.2-SNAPSHOT"
                     "0.3.4-alpha.rc.1"
                     ])

(def invalid-versions ["1.3", "3-alpha"])

(deftest valid?-test
  (testing "should return true for valid semvers"
    (is (every? true? (map valid? valid-versions))))
  (testing "should return false for invalid semvers"
    (is (every? false? (map valid? invalid-versions)))))

(deftest parse-test
  (testing "should parse valid semver"
    (is (instance? Version (parse "1.2.3"))))
  (testing "should return nil for invalid semver"
    (is (nil? (parse "1.2")))))

(deftest snapshot?-test
  (testing "should return true if version is snapshot"
    (is (snapshot? "1.0.0-SNAPSHOT")))
  (testing "should return false if version is not snapshot"
    (is (not (snapshot? "1.0.0-alpha.1")))))

(deftest newer?-test
  (testing "should return true if the first version is newer than the second version"
    (is (newer? "1.0.1" "1.0.0"))
    (is (newer? "2.0.1" "1.0.1"))
    (is (newer? "2.0.0" "1.0.0-alpha.1"))
    (is (newer? "1.1.0" "1.0.5"))))
