package telecom.projectpiim;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_features2d.BOWImgDescriptorExtractor;
import org.bytedeco.javacpp.opencv_features2d.FlannBasedMatcher;
import org.bytedeco.javacpp.opencv_features2d.KeyPoint;
import org.bytedeco.javacpp.opencv_ml.CvSVM;
import org.bytedeco.javacpp.opencv_nonfree.SIFT;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;

import static org.bytedeco.javacpp.opencv_highgui.imread;




public class TestClassifier {


    public static void main(String[] args) throws FileNotFoundException{

        //prepare BOW descriptor extractor from the vocabulary already computed

        //final String pathToVocabulary = "vocabulary.yml" ; // to be define
        final Mat vocabulary;

        System.out.println("read vocabulary from file... ");
        Loader.load(opencv_core.class);
        opencv_core.CvFileStorage storage = opencv_core.cvOpenFileStorage("app/assets/Data_BOW/vocabulary.yml", null, opencv_core.CV_STORAGE_READ);
        Pointer p = opencv_core.cvReadByName(storage, null, "vocabulary", opencv_core.cvAttrList());
        opencv_core.CvMat cvMat = new opencv_core.CvMat(p);
        vocabulary = new opencv_core.Mat(cvMat);
        System.out.println("vocabulary loaded " + vocabulary.rows() + " x " + vocabulary.cols());
        opencv_core.cvReleaseFileStorage(storage);


        //create SIFT feature point extracter
        final SIFT detector;
        // default parameters ""opencv2/features2d/features2d.hpp""
        detector = new SIFT(0, 3, 0.04, 10, 1.6);

        //create a matcher with FlannBased Euclidien distance (possible also with BruteForce-Hamming)
        final FlannBasedMatcher matcher;
        matcher = new FlannBasedMatcher();

        //create BoF (or BoW) descriptor extractor
        final BOWImgDescriptorExtractor bowide;
        bowide = new BOWImgDescriptorExtractor(detector.asDescriptorExtractor(), matcher);

        //Set the dictionary with the vocabulary we created in the first step
        bowide.setVocabulary(vocabulary);
        System.out.println("Vocab is set");


        int classNumber = 3;
        String[] class_names;
        class_names = new String[classNumber];

        class_names[0] = "Coca";
        class_names[1] = "Pepsi";
        class_names[2] = "Sprite";


        final CvSVM [] classifiers;
        classifiers = new CvSVM [classNumber];
        for (int i = 0 ; i < classNumber ; i++) {
            //System.out.println("Ok. Creating class name from " + className);
            //open the file to write the resultant descriptor
            classifiers[i] = new CvSVM();
            classifiers[i].load("app/assets/Data_BOW/classifiers/" + class_names[i] + ".xml");
        }

        Mat response_hist = new Mat();
        KeyPoint keypoints = new KeyPoint();
        Mat inputDescriptors = new Mat();


        MatVector imagesVec;

        File root = new File("app/assets/Data_BOW/TestImage");
        FilenameFilter imgFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
            }
        };

        File[] imageFiles = root.listFiles(imgFilter);

        imagesVec = new MatVector(imageFiles.length);

        //  Mat labels = new Mat(imageFiles.length, 1, CV_32SC1);
        //  IntBuffer labelsBuf = labels.createBuffer();


        for (File im : imageFiles) {

            //System.out.println("path:" + im.getName());

            Mat imageTest = imread(im.getAbsolutePath(), 1);
            detector.detectAndCompute(imageTest, Mat.EMPTY, keypoints, inputDescriptors);
            bowide.compute(imageTest, keypoints, response_hist);

            // Finding best match
            float minf = Float.MAX_VALUE;
            String bestMatch = null;

            long timePrediction = System.currentTimeMillis();
            // loop for all classes
            for (int i = 0; i < classNumber; i++) {
                // classifier prediction based on reconstructed histogram
                float res = classifiers[i].predict(response_hist, true);
                //System.out.println(class_names[i] + " is " + res);
                if (res < minf) {
                    minf = res;
                    bestMatch = class_names[i];
                }
            }
            timePrediction = System.currentTimeMillis() - timePrediction;
            System.out.println(im.getName() + "  predicted as " + bestMatch + " in " + timePrediction + " ms");

        }

        return;
    }

}