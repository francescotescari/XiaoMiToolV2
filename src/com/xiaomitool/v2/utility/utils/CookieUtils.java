package com.xiaomitool.v2.utility.utils;

import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class CookieUtils {

    public static interface EventCookieAdd {
        public boolean run(URI url, HttpCookie cookie);
    }
    public static void clear(){
        if (cookieStoreModded != null){
            cookieStoreModded.clear();
        }
    }

    private static class CookieStoreModded  implements CookieStore {
        private List<EventCookieAdd> eventCookieAdds = new ArrayList<>();
        private CookieStore cookieStore = (new CookieManager()).getCookieStore();

        public void clear(){
            cookieStore = (new CookieManager()).getCookieStore();
        }

        public void addListener(EventCookieAdd event){
            eventCookieAdds.add(event);
        }
        public void removeListener(EventCookieAdd event){
            eventCookieAdds.remove(event);
        }

        @Override
        public void add(URI url,  HttpCookie cookie){

            if (!cookie.hasExpired()) {
                List<EventCookieAdd> toRemove = new ArrayList<>();
                for (EventCookieAdd event : eventCookieAdds) {
                    if (!event.run(url, cookie)){
                        toRemove.add(event);
                    }
                }
                for (EventCookieAdd event : toRemove){
                    eventCookieAdds.remove(event);
                }
            }
            cookieStore.add(url,cookie);
        }

        @Override
        public List<HttpCookie> get(URI uri) {
            return cookieStore.get(uri);
        }

        @Override
        public List<HttpCookie> getCookies() {
            return cookieStore.getCookies();
        }

        @Override
        public List<URI> getURIs() {
            return cookieStore.getURIs();
        }

        @Override
        public boolean remove(URI uri, HttpCookie cookie) {
            return cookieStore.remove(uri,cookie);
        }

        @Override
        public boolean removeAll() {
            return cookieStore.removeAll();
        }
    }

    private static CookieStoreModded cookieStoreModded;


    public static void addCookieListener(EventCookieAdd event){
        if (cookieStoreModded == null){
            cookieStoreModded = new CookieStoreModded();
            CookieManager cookieManager = new CookieManager(cookieStoreModded,null);
            CookieHandler.setDefault(cookieManager);
        }
        cookieStoreModded.addListener(event);
    }
    public static void removeCookieListener(EventCookieAdd event){
        if (cookieStoreModded == null){
            return;
        }
        cookieStoreModded.removeListener(event);
    }

}
