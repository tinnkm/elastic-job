package com.tinnkm.elasticjob.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @Auther: tinnkm
 * @Date: 2018/10/31 09:19
 * @Description: list工具集合
 * @since: 1.0
 */
@Slf4j
public class ListUtils<R> {
    private ListUtils(){}

    public static Map<Long, List<Long>> getDivideList(Map<Long,Long> source,int maxDivide){
        HashMap<Long, List<Long>> result = new HashMap<>();
        List<List<Long>> resultList = new ArrayList<>();
        if (source.size() < maxDivide) {
            buildResultList(resultList, source);
        } else {
            int[] pointArr = findSplitPoint(source, maxDivide);
            buildResultList(resultList, source, pointArr);
        }
        for (int i = 0; i < resultList.size(); i++) {
            result.put((long) i,resultList.get(i));
        }
        return result;
    }

    private static int[] findSplitPoint(Map<Long,Long> source, int maxDivide) {
        List<Long> lengthList = new ArrayList<>();
        source.forEach((k,v)->{
            lengthList.add(v);
        });
        int totalNum  = lengthList.stream().mapToInt(Long::intValue).sum();
        //平均值
        double average = (double) totalNum / (double) maxDivide;
        long groupNum = 0;
        int[] pointArr = new int[maxDivide - 1];
        int pointArrIndex = 0;
        double minDiffSum = -1;
        int i = 0;
        for (; i < lengthList.size(); i++) {
            long num = lengthList.get(i);
            //按顺序累加
            groupNum += num;
            double curDiffSum = Math.abs(groupNum - average);

            int surplusGroupNum = maxDivide - 1 - pointArrIndex;
            int surplusDataNum = lengthList.size() - i;
            int differNum = surplusDataNum - surplusGroupNum;

            if (differNum > 0) {
                //当前累加 与平均值的距离 < 上次累加与平均值的距离
                if (curDiffSum < minDiffSum || minDiffSum == -1) {
                    minDiffSum = curDiffSum;
                } else {
                    //保存切分点
                    pointArr[pointArrIndex] = i;
                    if (pointArrIndex == maxDivide - 2) {
                        break;
                    }
                    pointArrIndex++;
                    groupNum = num;
                    minDiffSum = Math.abs(num - average);
                }
            } else {
                //简单解决大数分布到头尾，不够分组的情况。把末尾的每个值单独分为一组。
                pointArr[pointArrIndex++] = i;
                int point = i + 1;
                for (int j = 0; j < surplusGroupNum - 1; j++) {
                    pointArr[pointArrIndex++] = point++;
                }
                break;
            }

        }
        return pointArr;
    }

    private static void buildResultList(List<List<Long>> resultList, Map<Long,Long> dataList) {
        for (Long aDataList : dataList.keySet()) {
            List<Long> list = new ArrayList<>();
            list.add(aDataList);
            resultList.add(list);
        }
    }

    private static void buildResultList(List<List<Long>> resultList,  Map<Long,Long> dataList, int[] resultArr) {
        ArrayList<Long> key = new ArrayList<>();
        dataList.forEach((k,v) -> key.add(k));
        if (resultList == null || dataList == null || resultArr == null) {
            return;
        }
        int j = 0;
        int maxJ = 0;
        for (int i = 0; i <= resultArr.length; i++) {
            if (i == resultArr.length) {
                maxJ = dataList.size();
            } else {
                maxJ = resultArr[i];
            }
            List<Long> list = new ArrayList<>();
            for (; j < maxJ; j++) {
                list.add(key.get(j));
            }
            resultList.add(list);
        }
    }
}
