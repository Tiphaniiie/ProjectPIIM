package telecom.piim2;

import android.util.Log;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;

import java.io.File;
import java.util.ArrayList;

import static org.bytedeco.javacpp.opencv_core.CV_STORAGE_READ;
import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.cvAttrList;
import static org.bytedeco.javacpp.opencv_core.cvOpenFileStorage;
import static org.bytedeco.javacpp.opencv_core.cvReadByName;
import static org.bytedeco.javacpp.opencv_core.cvReleaseFileStorage;
import static org.bytedeco.javacpp.opencv_features2d.BOWImgDescriptorExtractor;
import static org.bytedeco.javacpp.opencv_features2d.FlannBasedMatcher;
import static org.bytedeco.javacpp.opencv_features2d.KeyPoint;
import static org.bytedeco.javacpp.opencv_highgui.imread;
import static org.bytedeco.javacpp.opencv_ml.CvSVM;
import static org.bytedeco.javacpp.opencv_nonfree.SIFT;

/**
 * Created by ASUS on 2/21/2017.
 */

public class PicAnalysis {

    private File vocab;
    private ArrayList<Brand> brandsList;
    private String[] class_names;
    private CvSVM[] classifiers;
    private Mat vocabulary;
    private String mCurrentPhotoPath;

    public PicAnalysis(String mCurrentPhotoPath, File vocab, ArrayList<Brand> brandsList) {
        this.mCurrentPhotoPath = mCurrentPhotoPath;
        this.vocab = vocab;
        this.brandsList = brandsList;
    }

    public Mat getVoc(){
        Loader.load(opencv_core.class);
        opencv_core.CvFileStorage storage = cvOpenFileStorage(vocab.getAbsolutePath(), null, CV_STORAGE_READ);
        Pointer p = cvReadByName(storage, null, "vocabulary", cvAttrList());
        opencv_core.CvMat cvMat = new opencv_core.CvMat(p);
        vocabulary = new Mat(cvMat);
        Log.d("Vocabulary", "vocabulary loaded " + vocabulary.rows() + " x " + vocabulary.cols());
        cvReleaseFileStorage(storage);
        return vocabulary;
    }

    public void getClfier(){
        //Create classifiers from server files
        class_names = new String[brandsList.size()];
        for (int i = 0; i< brandsList.size(); i++){
            class_names[i] = brandsList.get(i).getName();
        }
        classifiers = new CvSVM[brandsList.size()];

        for (int i = 0; i < brandsList.size(); i++) {
            Log.d("Classifier", "Ok. Creating class name from " + class_names[i]);
            //open the file to write the resultant descriptor
            classifiers[i] = new CvSVM();
            classifiers[i].load(brandsList.get(i).getClassifier().getAbsolutePath());
        }
    }

    public String analyse(){
        SIFT detector;
        FlannBasedMatcher matcher;
        BOWImgDescriptorExtractor bowide;
        Mat response_hist = new Mat();
        KeyPoint keypoints = new KeyPoint();
        Mat inputDescriptors = new Mat();

        //create SIFT feature point extracter
        // default parameters ""opencv2/features2d/features2d.hpp""
        detector = new SIFT(0, 3, 0.04, 10, 1.6);

        //create a matcher with FlannBased Euclidien distance (possible also with BruteForce-Hamming)
        matcher = new FlannBasedMatcher();

        //create BoF (or BoW) descriptor extractor
        bowide = new BOWImgDescriptorExtractor(detector.asDescriptorExtractor(), matcher);

        //Set the dictionary with the vocabulary
        vocabulary = getVoc();
        bowide.setVocabulary(vocabulary);
        Log.d("Vocabulary", "vocab is set");

        //Set the classifiers with the brands
        getClfier();
        Log.d("Classifier", "ok");

        //Transform chosen picture to Mat
        Mat imageTest = imread(mCurrentPhotoPath, 1);

        //Analyse it
        detector.detectAndCompute(imageTest, Mat.EMPTY, keypoints, inputDescriptors);
        bowide.compute(imageTest, keypoints, response_hist);

        // Finding best match
        float minf = Float.MAX_VALUE;
        String bestMatch = null;
        long timePrediction = System.currentTimeMillis();

        // loop for all classes
        for (int j = 0; j < brandsList.size(); j++) {
            // classifier prediction based on reconstructed histogram
            float res = classifiers[j].predict(response_hist, true);
            if (res < minf) {
                minf = res;
                bestMatch = class_names[j];
            }
        }

        timePrediction = System.currentTimeMillis() - timePrediction;
        Log.d("Analysis", mCurrentPhotoPath + "  predicted as " + bestMatch + " in " + timePrediction + " ms");
        return bestMatch;
    }


}
