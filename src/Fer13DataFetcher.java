import org.deeplearning4j.datasets.fetchers.BaseDataFetcher;
import org.deeplearning4j.util.MathUtils;
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

    protected short[] emotions;
    protected float[][] images;

    public Fer13DataFetcher() throws IOException {
        this(true);
    }

    public Fer13DataFetcher(boolean binarize) throws IOException {
        this(binarize, true, true, System.currentTimeMillis());
    }

    public Fer13DataFetcher(boolean binarize, boolean train, boolean shuffle, long rngSeed) throws FileNotFoundException{
        File set = new File("dataset/fer13.csv");
        String line = "";
        int i = 0, j;

        if(!set.exists()){
            throw new FileNotFoundException("dataset/fer13.csv doesn't exist!");
        }

        if(train){
            totalExamples = NUM_EXAMPLES;
            emotions = new short[NUM_EXAMPLES];
            images = new float[NUM_EXAMPLES][48*48];
        }else{
            totalExamples = NUM_EXAMPLES_TEST;
            emotions = new short[NUM_EXAMPLES_TEST];
            images = new float[NUM_EXAMPLES_TEST][48*48];
        }

        System.out.println("Loading fer13 " + (train ? "train" : "test") + " set... ");

        try (BufferedReader br = new BufferedReader(new FileReader(set))) {

            line = br.readLine(); //skip header
            //System.out.println("Header: " + line);

            while ((line = br.readLine()) != null) {

                String[] columns = line.split(",");
                String[] pixels = columns[1].split(" ");
                //System.out.println(columns[0] + " " + columns[1].length() + " " + columns[2]);
                short emotion = Short.parseShort(columns[0]);

                if(emotion >= 1){ //merge 0 and 1 (anger and disgust)
                    emotion--;
                }

                if(
                        train && columns[2].equals("Training") ||
                        (!train) && columns[2].toLowerCase().contains("test")
                ){
                    emotions[i] = emotion;
                    for(j=0; j<pixels.length; j++){
                        images[i][j] = Float.parseFloat(pixels[j]) / 255.0f;
                    }

                    i++;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Loaded " + i + "/" + images.length + " images!");

        numOutcomes = 6;
        cursor = 0;
        inputColumns = images.length;

        this.binarize = binarize;
        this.train = train;
        this.shuffle = shuffle;

        if (train) {
            order = new int[NUM_EXAMPLES];
        } else {
            order = new int[NUM_EXAMPLES_TEST];
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

            featureData[actualExamples] = images[order[cursor]];
            labelData[actualExamples] = new float[numOutcomes];
            labelData[actualExamples][emotions[order[cursor]]] = 1.0f;

            //System.out.println("Solution for " + order[cursor] + " is " + emotions[order[cursor]]);

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

    @Override
    public void reset() {
        cursor = 0;
        curr = null;
        if (shuffle)
            MathUtils.shuffleArray(order, rng);
    }

    @Override
    public DataSet next() {
        DataSet next = super.next();
        return next;
    }

}
