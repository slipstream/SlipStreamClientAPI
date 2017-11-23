(ns sixsq.slipstream.client.impl.utils.run-params
  (:require
    [sixsq.slipstream.client.impl.utils.error :as e]
    [clojure.string :as str]))

(def param-refqname "refqname")
(def param-scalable "mutable")
(def param-keep-running "keep-running")
(def param-tags "tags")
(def param-type "type")

(def params-reserved #{param-tags
                       param-keep-running
                       param-type})


(defn assoc-comp-param
  [m k v]
  (let [k (str/trim (name k))]
    (cond
      (str/blank? k) m
      (str/includes? k ":") (let [[comp param] (str/split k #":")]
                              (assoc m (str "parameter--node--" comp "--" param) (str v)))
      :else (assoc m (str "parameter--" k) (str v)))))


(defn parse-params-fn
  "reduction function that accumulates map entries with the processed keys and
   values"
  [m k v]
  (let [k (str/trim (name k))]
    (cond
      (str/blank? k) m
      (= "scalable" k) (assoc m param-scalable (str v))
      (params-reserved k) (assoc m k (name v))
      :else (assoc-comp-param m k v))))


(defn parse-params
  [params]
  (reduce-kv parse-params-fn {} params))


(defn assoc-module-uri
  [params uri]
  (assoc params param-refqname (str "module/" uri)))


(defn extract-location
  "transducer that extracts the value of the location header from the response"
  []
  (comp
    (map e/throw-if-error)
    (map :headers)
    (map :location)))
