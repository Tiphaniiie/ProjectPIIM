package telecom.projectpiim;

import android.util.Log;

import org.bytedeco.javacpp.opencv_core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.bytedeco.javacpp.opencv_features2d.BFMatcher;
import static org.bytedeco.javacpp.opencv_features2d.DMatchVectorVector;
import static org.bytedeco.javacpp.opencv_features2d.KeyPoint;
import static org.bytedeco.javacpp.opencv_highgui.imread;
import static org.bytedeco.javacpp.opencv_nonfree.SIFT;


/**
 * Created by ASUS on 11/22/2016.
 */

public class ComparePics {
    private static SpeMat image;
    private Mat[] images;

    ComparePics(SpeMat image){
        this.image = image;
        Mat[] images = new Mat[]{
                this.image.getImage(),
                imread("app/assets/data/church02.jpg", 1)
        };
    }


    private static Mat load(File file, int flags) throws IOException {
        if(!file.exists()) {
            throw new FileNotFoundException("Image file does not exist: " + file.getAbsolutePath());
        }
        Mat image = imread(file.getAbsolutePath(), flags);
        if(image == null || image.empty()) {
            throw new IOException("Couldn't load image: " + file.getAbsolutePath());
        }
        return image;
    }
    private static DMatchVectorVector refineMatches(DMatchVectorVector oldMatches) {
        // Ratio of Distances
        double RoD = 0.6;
        DMatchVectorVector newMatches = new DMatchVectorVector();

        // Refine results 1: Accept only those matches, where best dist is < RoD
        // of 2nd best match.
        int sz = 0;
        newMatches.resize(oldMatches.size());

        double maxDist = 0.0, minDist = 1e100; // infinity

        for (int i = 0; i < oldMatches.size(); i++) {
            newMatches.resize(i, 1);
            if (oldMatches.get(i, 0).distance() < RoD
                    * oldMatches.get(i, 1).distance()) {
                newMatches.put(sz, 0, oldMatches.get(i, 0));
                sz++;
                double distance = oldMatches.get(i, 0).distance();
                if (distance < minDist)
                    minDist = distance;
                if (distance > maxDist)
                    maxDist = distance;
            image.setDistance(distance);
            Log.i("DISTANNNNNNNNCE", String.valueOf(image.getDistance()));
            }
        }
        newMatches.resize(sz);

        // Refine results 2: accept only those matches which distance is no more
        // than 3x greater than best match
        sz = 0;
        DMatchVectorVector brandNewMatches = new DMatchVectorVector();
        brandNewMatches.resize(newMatches.size());
        for (int i = 0; i < newMatches.size(); i++) {
            // TODO: Move this weights into params
            // Since minDist may be equal to 0.0, add some non-zero value
            if (newMatches.get(i, 0).distance() <= 3 * minDist) {
                brandNewMatches.resize(sz, 1);
                brandNewMatches.put(sz, 0, newMatches.get(i, 0));
                sz++;
            }
        }
        brandNewMatches.resize(sz);
        return brandNewMatches;
    }
    public static float bestDistance(float[] scores){
        float finalScore = scores[0];
        for (int i = 0; i < scores.length; i++){
            if (scores[i]< finalScore){
                finalScore = scores[i];
            }
        }
        return finalScore;
    }


    public void calcDist(){

        KeyPoint[]  keypoints = {new KeyPoint(), new KeyPoint()};
        Mat[] descriptors = new Mat[2];
        int nFeatures = 0;
        int nOctaveLayers = 3;
        double contrastThreshold = 0.03;
        int edgeThreshold = 10;
        double sigma = 1.6;
        SIFT sift = new SIFT(nFeatures, nOctaveLayers, contrastThreshold, edgeThreshold, sigma);

        // Detect SIFT features and compute descriptors for both images
        for (int i = 0; i <= 1; i++) {
            // Create Surf Keypoint Detector
            sift.detect(images[i], keypoints[i]);
            // Create Surf Extractor
            descriptors[i] = new Mat();
            sift.compute(images[i], keypoints[i], descriptors[i]);
        }

        BFMatcher matcher = new BFMatcher();
        DMatchVectorVector matches = new DMatchVectorVector();

        long t = System.currentTimeMillis();

        matcher.knnMatch(descriptors[0], descriptors[1], matches, 2);

        DMatchVectorVector bestMatches = refineMatches(matches);
        //Log.i("VECTOOOOOOOR", bestMatches.);
    }

}
