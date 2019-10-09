package com.sap.shared;

/**
 * K-Nearest Neighbor (KNN) method - Data training and testing
 *
 * Authors:         Jinpei Chen
 *                  Yuzhao Li
 *
 * Created data:    10/09/2019
 * Last modified:   30/09/2019
 */


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class FallDetection_KNN {

    String filename;
    String testfilename;
    private LinkedList<Float> test;
    private List<Float[]> train;
    private List<Float> trainlabel;

    public FallDetection_KNN(LinkedList<Float> test, List<Float[]> train, List<Float> trainlabel){
//        this.filename = filename;
//        this.testfilename = testfilename;
        this.test = test;
        this.train = train;
        this.trainlabel = trainlabel;
    }

    // lists for storing training and testing datasets label and features.
//    private List<Float[]> trainfeatures = new ArrayList<>();
//    private List<String> trainlabel = new ArrayList<>();

//    private List<Float[]> testfeatures = new ArrayList<>();
//    private List<String> testlabel = new ArrayList<>();

    // Variables
    int determin_K_by_size = 0;        // 0 for manual assign, 1 for auto determine by size
    int knn_value = 7;
    int number_Of_Label = 0;
    int Distance_Metrics_Selction = 1; // 1 for Euclidean, 0 for Manhattan

    // loading training data and extracting features and label for training dataset
//    private void loadtrainData(String filename) throws IOException {
//
////        File file = new File("./app/src/main/java/KNN/fall_data_101.csv");
//        InputStream input = myCtx.getAssets().open("fall_data_101.csv");
//        InputStreamReader inputreader = new InputStreamReader(input);
//
//        try {
//            BufferedReader readFile = new BufferedReader(inputreader);
//            String line;
//            while ((line = readFile.readLine()) != null) {
//                String[] split = line.split(",");
//                Float[] values = new Float[split.length - 1];
//                for (int i = 0; i < split.length-1; i++)
//                    values[i] = Float.parseFloat(split[i+1]);
//                trainfeatures.add(values);
//                trainlabel.add(split[0]);
//                number_Of_Label++;
//            }
//            readFile.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        Float[] f = new Float[test.size()];
//        for (int i=0; i<f.length; i++){
//            f[i] = test.get(i);
//        }
//        testfeatures.add(f);
//    }


    private Float getDistance(Float[] a1, Float[] a2) {

        if (Distance_Metrics_Selction == 1) {
            return euclideanDistance(a1, a2);
            // calling accuracy method to show accuracy of model.
//                accuracy();
        }

        else {
            return manhattanDistance(a1, a2);
//                accuracy();
        }

    }


    private Float euclideanDistance(Float[] a1, Float[] a2) {
        Float sum = 0.0f;
        for (int i=0; i<a1.length; i++) {
            //applied Euclidean distance formula
            sum += (float) Math.pow(a1[i] - a2[i], 2);
        }
        return (float) Math.sqrt(sum);
    }

    private Float manhattanDistance(Float[] a1, Float[] a2) {
        Float sum = 0.0f;
        for (int i = 0; i < a1.length; i++)
            //Applied Manhattan distance formula
            sum += Math.abs(a1[i] - a2[i]);
        return sum;
    }

    private int determineK () {
        Float root = (float) Math.sqrt(number_Of_Label);
        Float rawK = root / 2 ;
        int num = (int) Math.round(rawK) ;
        if ( num%2 != 0 ) {
            return num ;
        }
        else {
            return num - 1 ;
        }
    }

    public int wrappingUp(){
//            try{
////            loadtrainData(filename);
////            loadtestData(testfilename);
//            }
//            catch(IOException e){e.printStackTrace();}

        List<Float> distances = new ArrayList<>();
        List<Float> distancesClone = new ArrayList<>();
        int CLASS_1=0, CLASS_2=0;

//            System.out.println(trainfeatures.size()+", "+testfeatures.size());

        for (int i=0; i<train.size(); i++){
            Float[] tmp = new Float[test.size()];
            for (int j=0; j<test.size(); j++){
                tmp[j] = test.get(j);
            }
            Float distance = getDistance(train.get(i),tmp);
            distances.add(distance);
            distancesClone.add(distance);
        }
        Collections.sort(distances);

        if (determin_K_by_size==1)
            knn_value = determineK();

        System.out.println(distances.size());
        List<Float> k_shortest = distances.subList(0,knn_value);

        for (Float element : k_shortest) {
            System.out.println(element);
            Integer indexOnClone = distancesClone.indexOf(element);
            Float label = trainlabel.get(indexOnClone);
            System.out.println(label);
            if (label == 1.0f) {
                CLASS_1++;
            }
            else if (label == 0.0f) {
                CLASS_2++;
            }
        }
        System.out.println(CLASS_1+" "+CLASS_2);
        if (CLASS_1 > CLASS_2){
            return 1;
        }
        else {
            return 0;
        }

    }
}
