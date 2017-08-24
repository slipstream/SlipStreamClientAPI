package sixsq.slipstream.client;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

import sixsq.slipstream.client.api.cimi.cimi;

public class CIMI {

    static {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("sixsq.slipstream.client.api.cimi"));
        require.invoke(Clojure.read("sixsq.slipstream.client.api.cimi.sync"));
        require.invoke(Clojure.read("sixsq.slipstream.client.api.cimi.java-utils"));
        require.invoke(Clojure.read("taoensso.timbre"));
    }

    private static final IFn cimiClientConstructor = Clojure.var("sixsq.slipstream.client.api.cimi.sync", "instance");

    private static final IFn toClojure = Clojure.var("sixsq.slipstream.client.api.cimi.java-utils", "to-clojure");
    private static final IFn toJava = Clojure.var("sixsq.slipstream.client.api.cimi.java-utils", "to-java");

    private static final IFn keyword = Clojure.var("clojure.core", "keyword");
    private static final IFn setLoggingLevel = Clojure.var("taoensso.timbre", "set-level!");

    static {
        setLoggingLevel.invoke(keyword.invoke("info"));
    }

    private final cimi syncClient;

    public CIMI() {
        syncClient = (cimi) cimiClientConstructor.invoke();
    }

    public CIMI(String endpoint) {
        syncClient = (cimi) cimiClientConstructor.invoke(endpoint);
    }

    public Object login(Object loginParams) {
        return toJava.invoke(syncClient.login(toClojure.invoke(loginParams)));
    }

    public Object logout() {
        return toJava.invoke(syncClient.logout());
    }

    public Boolean isAuthenticated() {
        return (Boolean) syncClient.authenticated_QMARK_();
    }

    public Object getCloudEntryPoint() {
        return toJava.invoke(syncClient.cloud_entry_point());
    }

    public Object add(String resourceType, Object data) {
        return toJava.invoke(syncClient.add(resourceType, toClojure.invoke(data)));
    }

    public Object add(String resourceType, Object data, Object options) {
        return toJava.invoke(syncClient.add(resourceType, toClojure.invoke(data), toClojure.invoke(options)));
    }

    public Object edit(String urlOrId, Object data) {
        return toJava.invoke(syncClient.edit(urlOrId, toClojure.invoke(data)));
    }

    public Object edit(String urlOrId, Object data, Object options) {
        return toJava.invoke(syncClient.edit(urlOrId, toClojure.invoke(data), toClojure.invoke(options)));
    }

    public Object delete(String urlOrId) {
        return toJava.invoke(syncClient.delete(urlOrId));
    }

    public Object delete(String urlOrId, Object options) {
        return toJava.invoke(syncClient.edit(urlOrId, toClojure.invoke(options)));
    }

    public Object get(String urlOrId) {
        return toJava.invoke(syncClient.get(urlOrId));
    }

    public Object get(String urlOrId, Object options) {
        return toJava.invoke(syncClient.get(urlOrId, toClojure.invoke(options)));
    }

    public Object search(String resourceType) {
        return toJava.invoke(syncClient.search(resourceType));
    }

    public Object search(String resourceType, Object options) {
        return toJava.invoke(syncClient.search(resourceType, toClojure.invoke(options)));
    }

}
