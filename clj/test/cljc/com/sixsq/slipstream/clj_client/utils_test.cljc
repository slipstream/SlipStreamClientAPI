(ns com.sixsq.slipstream.clj-client.utils-test
  (:require
        [com.sixsq.slipstream.clj-client.lib.utils :as t]
        #?(:clj [clojure.test :refer [deftest is are testing run-tests]]
           :cljs [cljs.test :refer-macros [deftest is testing run-tests]])))

(deftest test-in?
  (is (true? (t/in? :a [:a :b])))
  (is (true? (t/in? :a #{:a :b})))
  (is (true? (t/in? :a '(:a :b))))

  (is (false? (t/in? :a [:b])))
  (is (false? (t/in? :a #{:b})))
  (is (false? (t/in? :a '(:b)))))

(deftest test-url-join
  (is (= "" (t/url-join)))
  (is (= "" (t/url-join [])))
  (is (= "a/b" (t/url-join ["a" "b"])))
  (is (= "http://example.com/r1/id1" (t/url-join ["http://example.com" "r1" "id1"]))))

(deftest test-to-body-params
  (is (= "" (t/to-body-params {})))
  (is (= "" (t/to-body-params {"" ""})))
  (is (= "" (t/to-body-params {" " ""})))
  (is (= "" (t/to-body-params {nil "b"})))
  (is (= "" (t/to-body-params {"" "b"})))
  (is (= "a=b" (t/to-body-params {:a "b"})))
  (is (= "a=b" (t/to-body-params {"a" "b"})))
  (is (= "a=b\nc=d" (t/to-body-params {"a" "b" "c" "d"})))
  (is (= "a=b\nc=d" (t/to-body-params {:a "b" :c "d"})))
  (is (= "a=b&c=d" (t/to-body-params {"a" "b" "c" "d"} "&"))))