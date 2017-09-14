(ns sixsq.slipstream.client.api.utils.http-utils)

(defn set-or-clear-insecure-flag
  [{:keys [insecure?] :as req}]
  (-> req
      (assoc :kvlt.platform/insecure? insecure?)
      (dissoc :insecure?)))
