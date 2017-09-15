(ns sixsq.slipstream.client.api.runs)

(defprotocol runs
  "Methods to get and search for runs.

   Note that the return types will depend on the concrete
   implementation.  For example, an asynchronous implementation will
   return channels from all of the functions."

  (get-run
    [this url-or-id]
    [this url-or-id options]
    "Reads the run identified by the URL or resource id.  Returns
     the resource as EDN data.")

  (search-runs
    [this]
    [this options]
    "Search for runs of the given type, returning a list of the
     matching runs. Supported options are :cloud, :activeOnly,
     :offset, and :limit. The returned document is in EDN format."))
