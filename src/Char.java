import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**A Simple Multi Layered Perceptron (MLP) applied to digit classification for
 * the MNIST Dataset (http://yann.lecun.com/exdb/mnist/).
 *
 * This file builds one input layer and one hidden layer.
 *
 * The input layer has input dimension of numRows*numColumns where these variables indicate the
 * number of vertical and horizontal pixels in the image. This layer uses a rectified linear unit
 * (relu) activation function. The weights for this layer are initialized by using Xavier initialization
 * (https://prateekvjoshi.com/2016/03/29/understanding-xavier-initialization-in-deep-neural-networks/)
 * to avoid having a steep learning curve. This layer will have 1000 output signals to the hidden layer.
 *
 * The hidden layer has input dimensions of 1000. These are fed from the input layer. The weights
 * for this layer is also initialized using Xavier initialization. The activation function for this
 * layer is a softmax, which normalizes all the 10 outputs such that the normalized sums
 * add up to 1. The highest of these normalized values is picked as the predicted class.
 *
 */
public class Char {

    private static Logger log = LoggerFactory.getLogger(Char.class);
    private static final int numRows    = 28;
    private static final int numColumns = 28;
    private static final int outputNum  = 10;
    private static final int batchSize  = 128;
    private static final int rngSeed    = 123;
    private static final int numEpochs  = 15;

    private static final String hostname = "localhost";
    private static final int port        = 6969;

    private DataSetIterator mnistTrain, mnistTest;
    private MultiLayerNetwork model;
    private MultiLayerConfiguration conf;

    private SocketIOServer server;

    public static void main(String[] args) throws Exception {
        new Char();
    }

    Char() throws Exception{

        //Setup some variables
        mnistTrain = new MnistDataSetIterator(batchSize, true, rngSeed);
        mnistTest  = new MnistDataSetIterator(batchSize, false, rngSeed);

        log.info("Load model....");
        loadModel("mnist-data.ai"); addShutdownHook();

        setupSocketIO();
        server.addEventListener("read", String.class, new DataListener<String>() {
            public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
                //client.sendEvent("msg", data);
            }
        });
        server.addEventListener("work", String.class, new DataListener<String>() {
            public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
                doWork();
                client.sendEvent("evaluate", evaluate());
            }
        });
        server.addEventListener("stop hammertime", String.class, new DataListener<String>() {
            public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
                server.stop();
            }
        });

        server.start();


    }

    void setupSocketIO(){
        Configuration config = new Configuration();
        config.setHostname(hostname);
        config.setPort(port);

        server = new SocketIOServer(config);
    }

    void doWork(){
        model.init();
        //print the score with every 1 iteration
        model.setListeners(new ScoreIterationListener(1));

        for(int i=0; i<numEpochs; i++){
            model.fit(mnistTrain);
        }
    }

    String evaluate(){
        Evaluation eval = new Evaluation(outputNum); //create an evaluation object with 10 possible classes
        while(mnistTest.hasNext()){
            DataSet next = mnistTest.next();
            INDArray output = model.output(next.getFeatureMatrix()); //get the networks prediction
            eval.eval(next.getLabels(), output); //check the prediction against the true class
        }

        return eval.stats();
    }

    void loadModel(String path) throws Exception{
        File save = new File(path);

        if(save.exists()){
            conf  = null;
            model = ModelSerializer.restoreMultiLayerNetwork(new File(path));
        }else{
            conf = new NeuralNetConfiguration.Builder()
                    .seed(rngSeed) //include a random seed for reproducibility
                    // use stochastic gradient descent as an optimization algorithm
                    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                    .iterations(1)
                    .learningRate(0.006) //specify the learning rate
                    .updater(Updater.NESTEROVS).momentum(0.9) //specify the rate of change of the learning rate.
                    .regularization(true).l2(1e-4)
                    .list()
                    .layer(0, new DenseLayer.Builder() //create the first, input layer with xavier initialization
                            .nIn(numRows * numColumns)
                            .nOut(1000)
                            .activation(Activation.RELU)
                            .weightInit(WeightInit.XAVIER)
                            .build())
                    .layer(1, new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD) //create hidden layer
                            .nIn(1000)
                            .nOut(outputNum)
                            .activation(Activation.SOFTMAX)
                            .weightInit(WeightInit.XAVIER)
                            .build())
                    .pretrain(false).backprop(true) //use backpropagation to adjust weights
                    .build();

            model = new MultiLayerNetwork(conf);
        }
    }

    void addShutdownHook(){
        final Thread mainThread = Thread.currentThread();
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        final Date date = new Date();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try{
                    File locationToSave = new File("mnist-data.ai");
                    ModelSerializer.writeModel(model, locationToSave, true);

                    mainThread.join();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

}