package com.cektrend.trashget.utils;

import com.cektrend.trashget.data.DataTrash;

import java.util.Comparator;

public class SortByOverallCapacity implements Comparator {
    @Override
    public int compare(Object t1, Object t2) {
        DataTrash trash1 = (DataTrash) t1;
        DataTrash trash2 = (DataTrash) t2;
        int overallCapacityTrash1 = (trash1.getOrganicCapacity() + trash1.getAnorganicCapacity()) / 2;
        int overallCapacityTrash2 = (trash2.getOrganicCapacity() + trash2.getAnorganicCapacity()) / 2;
        // return -1, 0, 1 to determine less than, equal to or greater than
        // return (Integer.compare(overallCapacityTrash1, overallCapacityTrash2));
        // **or** the previous return statement can be simplified to:
        return overallCapacityTrash2 - overallCapacityTrash1;
    }
}