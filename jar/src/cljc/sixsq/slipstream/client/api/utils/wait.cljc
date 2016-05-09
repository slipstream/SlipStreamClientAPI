(ns sixsq.slipstream.client.api.utils.wait
  (:require
    #?(:clj [clojure.core.async :refer [timeout go-loop <! <!!]]
       :cljs [cljs.core.async :refer [<!]]))
  #?(:cljs (:require-macros
             [cljs.core.async.macros :refer [timeout go-loop]])))

(defn iterations [timeout interval]
  {:pre [(> interval 0)]}
  (-> (quot timeout interval)
      (max 1)))

(defn wait-for-async
  "Asynchronous wait for 'predicate' for 'time-out' seconds with 'interval' seconds.

  Retruns channel that will contain the retuned value."
  [predicate time-out interval]
  {:pre [(> interval 0)]}
  (let [max-n       (iterations time-out interval)
        interval-ms (* 1000 interval)]
    (go-loop [n 1]
      (if-let [result (predicate)]
        result
        (do
          (<! (timeout interval-ms))
          (if (< n max-n)
            (do
              (recur (inc n)))))))))

#?(:clj
   (defn wait-for
     "Wait for 'predicate' for 'time-out' seconds with 'interval' seconds."
     [predicate time-out interval]
     (<!! (wait-for-async predicate time-out interval))))
