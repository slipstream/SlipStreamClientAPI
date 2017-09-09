(ns sixsq.slipstream.client.api.utils.http-utils)

(def ^:const http-lib-insecure-key :kvlt.platform/insecure?)

(defn process-req
  [req]
  (-> req
      (assoc http-lib-insecure-key true #_(:insecure? req)) ;; FIXME: Need better solution!
      (dissoc :insecure?)))

