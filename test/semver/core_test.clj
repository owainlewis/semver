(ns semver.core-test
  (:require [clojure.test :refer :all]
            [semver.core :refer :all]))

(def valid-versions [
  "1.0.0"
])

(deftest valid?-test
  (testing "should return true for valid semvers"
    (is (every? true? (map valid? valid-versions))))
  (testing "should return false for invalid semvers"))

(deftest parse-test
  (testing "should parse valid semver")
  (testing "should return nil for invalid semver"))

(deftest pre-release?-test
  (testing "should return true if version has pre-release tag")
  (testing "should return false if version does not have pre-release tag"))

(deftest snapshot?-test
  (testing "should return true if version is snapshot")
  (testing "should return false if version is not snapshot"))
