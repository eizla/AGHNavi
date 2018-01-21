package com.aghnavi.agh_navi.dmsl.utils;


import com.aghnavi.agh_navi.dmsl.nav.IPoisClass;
import com.aghnavi.agh_navi.dmsl.nav.PoisModel;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PopularRecentContainer implements Serializable{

    private Multiset<IPoisClass> popular = HashMultiset.create();
    private Multiset<IPoisClass> frequentImmutablePopular;
    private EvictingQueue<IPoisClass> recent = EvictingQueue.create(2);


    public Multiset<IPoisClass> getPopular() {
        return popular;
    }

    public void setPopular(Multiset<IPoisClass> popular) {
        this.popular = popular;
    }

    public EvictingQueue<IPoisClass> getRecent() {
        return recent;
    }

    public void setRecent(EvictingQueue<IPoisClass> recent) {
        this.recent = recent;
    }

    public Multiset<IPoisClass> getFrequentImmutablePopular() {
        return frequentImmutablePopular;
    }

    public void setFrequentImmutablePopular(Multiset<IPoisClass> frequentImmutablePopular) {
        this.frequentImmutablePopular = frequentImmutablePopular;
    }

    public boolean add(IPoisClass iPoisClass) {
        while(recent.contains(iPoisClass)) {
            recent.remove(iPoisClass);
        }
        recent.add(iPoisClass);
        popular.add(iPoisClass);
        sortBag();
        return true;
    }

    private Multiset<IPoisClass> sortBag() {
        return frequentImmutablePopular = Multisets.copyHighestCountFirst(popular);
    }

    public List<String> mapToRecentStringArray() {
        List<String> stringList = new ArrayList<>();
        if(recent.isEmpty()) return Collections.emptyList();
        for(IPoisClass poi : recent) {
            stringList.add(poi.name());
        }
        return stringList;
    }

    public List<String> mapToPopularStringArray() {
        int counter = 0;
        List<String> stringList = new ArrayList<>();
        if(popular.isEmpty()) return Collections.emptyList();
        for(IPoisClass poi : getFrequentImmutablePopular()) {
            stringList.add(poi.name());
            if(++counter == 2) break;
        }
        return stringList;
    }
}
