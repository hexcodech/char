import org.deeplearning4j.datasets.iterator.BaseDatasetIterator;

import java.io.IOException;

/**
 * Created by mac on 09.05.17.
 */
public class Fer13DataIterator extends BaseDatasetIterator {

    public Fer13DataIterator(int batch, int numExamples) throws IOException {
        this(batch, numExamples, false);
    }

    /**Get the specified number of examples for the MNIST training data set.
     * @param batch the batch size of the examples
     * @param numExamples the overall number of examples
     * @param binarize whether to binarize mnist or not
     * @throws IOException
     */
    public Fer13DataIterator(int batch, int numExamples, boolean binarize) throws IOException {
        this(batch, numExamples, binarize, true, false, 0);
    }

    /** Constructor to get the full MNIST data set (either test or train sets) without binarization (i.e., just normalization
     * into range of 0 to 1), with shuffling based on a random seed.
     * @param batchSize
     * @param train
     * @throws IOException
     */
    public Fer13DataIterator(int batchSize, boolean train, int seed) throws IOException {
        this(batchSize, (train ? Fer13DataFetcher.NUM_EXAMPLES : Fer13DataFetcher.NUM_EXAMPLES_TEST), false, train,
                true, seed);
    }

    /**Get the specified number of MNIST examples (test or train set), with optional shuffling and binarization.
     * @param batch Size of each patch
     * @param numExamples total number of examples to load
     * @param binarize whether to binarize the data or not (if false: normalize in range 0 to 1)
     * @param train Train vs. test set
     * @param shuffle whether to shuffle the examples
     * @param rngSeed random number generator seed to use when shuffling examples
     */
    public Fer13DataIterator(int batch, int numExamples, boolean binarize, boolean train, boolean shuffle,
                                long rngSeed) throws IOException {
        super(batch, numExamples, new Fer13DataFetcher(binarize, train, shuffle, rngSeed));
    }
}
