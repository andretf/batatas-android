package com.andretrindade.batatas.persistence;

import dagger.ObjectGraph;

public class DaggerInjector {

    private static ObjectGraph objectGraph;

    public static void bootstrap(Object target) {
        if (objectGraph == null) {
            objectGraph = ObjectGraph.create(new UserMemoryProvider(), new BatatasClientProvider());
        }
        objectGraph.inject(target);
    }
}
