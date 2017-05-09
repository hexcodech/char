import org.deeplearning4j.datasets.fetchers.BaseDataFetcher;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import java.io.*;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by mac on 05.05.17.
 */
public class Fer13DataFetcher extends BaseDataFetcher{

    public static int NUM_EXAMPLES = 28709;
    public static int NUM_EXAMPLES_TEST = 3589 + 3589;

    protected boolean binarize = true;
    protected boolean train;
    protected int[] order;
    protected Random rng;
    protected boolean shuffle;

    protected short[] trainEmotions = new short[NUM_EXAMPLES];
    protected float[][] trainImages = new float[NUM_EXAMPLES][48*48];
    protected short[] testEmotions = new short[NUM_EXAMPLES_TEST];
    protected float[][] testImages = new float[NUM_EXAMPLES_TEST][48*48];

    public Fer13DataFetcher(boolean binarize, boolean train, boolean shuffle, long rngSeed) throws FileNotFoundException{
        File set = new File("dataset/fer13.csv");
        String line = "";
        int i = 0, j = 0;

        if(!set.exists()){
            throw new FileNotFoundException("dataset/fer13.csv doesn't exist!");
        }

        try (BufferedReader br = new BufferedReader(new FileReader(set))) {

            while ((line = br.readLine()) != null) {

                String[] columns = line.split(",");
                String[] pixels = columns [1].split(" ");
                short emotion = Short.parseShort(columns[0]);

                if(emotion >= 1){ //merge 0 and 1 (anger and disgust)
                    emotion--;
                }

                if(columns [2].toLowerCase().contains("test")){
                    testEmotions[i] = emotion;

                    for(j=0; j<pixels.length; j++){
                        testImages[i][j] = Float.parseFloat(pixels[j]) / 255.0f;
                    }
                }else{
                    trainEmotions[i] = emotion;

                    for(j=0; j<pixels.length; j++){
                        trainImages[i][j] = Float.parseFloat(pixels[j]) / 255.0f;
                    }
                }

                i++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        numOutcomes = 6;
        cursor = 0;
        inputColumns = train ? trainImages.length : testImages.length;

        this.binarize = binarize;
        this.train = train;
        this.shuffle = shuffle;

        if (train) {
            order = new int[inputColumns];
        } else {
            order = new int[inputColumns];
        }
        for (i = 0; i < order.length; i++){
            order[i] = i;
        }
        rng = new Random(rngSeed);
        reset(); //Shuffle order

    }

    @Override
    public void fetch(int numExamples) {
        if (!hasMore()) {
            throw new IllegalStateException("Unable to getFromOrigin more; there are no more images");
        }

        float[][] featureData = new float[numExamples][0];
        float[][] labelData = new float[numExamples][0];

        int actualExamples = 0;
        for (int i = 0; i < numExamples; i++, cursor++) {
            if (!hasMore()){
                break;
            }

            featureData[actualExamples] = train ? trainImages[order[cursor]] : testImages[order[cursor]];
            labelData[actualExamples] = new float[5];
            labelData[actualExamples][
                    train ? trainEmotions[order[cursor]] : testEmotions[order[cursor]]
            ] = 1.0f;

            actualExamples++;
        }

        if (actualExamples < numExamples) {
            featureData = Arrays.copyOfRange(featureData, 0, actualExamples);
            labelData = Arrays.copyOfRange(labelData, 0, actualExamples);
        }

        INDArray features = Nd4j.create(featureData);
        INDArray labels = Nd4j.create(labelData);
        curr = new DataSet(features, labels);
    }

}
