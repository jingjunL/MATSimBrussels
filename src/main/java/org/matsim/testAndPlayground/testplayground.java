package org.matsim.testAndPlayground;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class testplayground {

    public static void main(String[] args) {

        ArrayList<String> first = new ArrayList<>();
        first.add("a"); first.add("c");
        first.add("e"); first.add("f");
        first.add("g");

        ArrayList<String> second = new ArrayList<>();
        second.add("b");second.add("d");

        System.out.println("========" + first.get(0));

        for(int i = 0; i < second.size(); i++){
            System.out.println(first.get(i).toUpperCase());
            System.out.println(second.get(i).toLowerCase());
        }

        //iterate the rest elements remained in first
        for(int i = second.size(); i < first.size(); i++){
            System.out.println(first.get(i).toUpperCase());
        }

    }

}
