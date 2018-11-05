package com.tinnkm.elasticjob.utils;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ListUtilsTest {

    @Test
    public void getDivideList() {
        HashMap<Long, Long> map = new HashMap<>();
        map.put(1L,61L);
        map.put(2L,71L);
        map.put(13L,19L);
        map.put(10L,17L);
        map.put(18L,91L);
        Map<Long, List<Long>> divideList = ListUtils.getDivideList(map, 7);
        divideList.forEach((k,v)->{
            System.out.println(k);
            v.forEach(System.out::println);
            System.out.println("---------------------------");
        });
    }
}