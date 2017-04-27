import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import org.datavec.image.loader.ImageLoader;
import org.deeplearning4j.nn.conf.LearningRatePolicy;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.buffer.DataBuffer;
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
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


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
    private static final int seed       = 123;
    private static final int numEpochs  = 15;
    private static final int nChannels  = 1;
    private static final int iterations = 1;

    private static final String hostname = "localhost";
    private static final int port        = 6969;
    private static String encodingPrefix = "base64,";

    private DataSetIterator mnistTrain, mnistTest;
    private MultiLayerNetwork model;
    private MultiLayerConfiguration conf;

    private SocketIOServer server;

    private boolean working = false;

    private BASE64Decoder decoder = new BASE64Decoder();
    private ImageLoader loader = new ImageLoader();

    public static void main(String[] args) throws Exception {
        new Char();
    }

    Char() throws Exception{

        //Setup some variables
        log.info("Load model....");
        loadModel("mnist-data.ai"); addShutdownHook();

        setupSocketIO();
        server.addEventListener("read", String.class, new DataListener<String>() {
            public void onData(SocketIOClient client, String data, AckRequest ackRequest) {

                BufferedImage originalImage = null, scaledImage = null;
                byte[] imageBytes;
                ByteArrayInputStream bis;

                try{

                    int contentStartIndex = data.indexOf(encodingPrefix) + encodingPrefix.length();

                    imageBytes = decoder.decodeBuffer(data.substring(contentStartIndex));

                    bis = new ByteArrayInputStream(imageBytes);
                    originalImage = ImageIO.read(bis); bis.close();

                    scaledImage = new BufferedImage(28, 28, originalImage.getType());
                    Graphics2D graphics2d = scaledImage.createGraphics();

                    graphics2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    graphics2d.drawImage(originalImage, 0, 0, 28, 28, null);
                    graphics2d.dispose();

                    ImageIO.write(scaledImage, "png", new File("scaled.png"));

                    INDArray values = loader.asRowVector(scaledImage); //doesn't work, not gray yet

                    for(int x=0;x<scaledImage.getWidth();x++){
                        for(int y=0;y<scaledImage.getHeight();y++){
                            Color color = new Color(scaledImage.getRGB(x, y));

                            int red = color.getRed();
                            int green = color.getGreen();
                            int blue = color.getBlue();

                            double gray = 1 - ((0.299 * red + 0.587 * green + 0.114 * blue) / 255); //also invert

                            //System.out.println(gray + " " + (x*28 + y));

                            values.putScalar(x + y*28, gray);
                        }
                    }

                    /*BufferedImage bi = new BufferedImage(28,28,BufferedImage.TYPE_BYTE_GRAY);
                    for( int i=0; i<784; i++ ){
                        bi.getRaster().setSample(i % 28, i / 28, 0, (int)(255*values.getDouble(i)));
                    }

                    ImageIO.write(bi, "png", new File("read-gray-scaled.png"));*/

                    INDArray output = model.output(values);
                    DataBuffer buffer = output.data();

                    client.sendEvent("read", Arrays.toString(buffer.asDouble()));

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
        server.addEventListener("work", String.class, new DataListener<String>() {
            public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
                try{
                    doWork();
                    client.sendEvent("evaluate", evaluate());
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
        server.addEventListener("stop hammertime", String.class, new DataListener<String>() {
            public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
                server.stop();
            }
        });

        server.start();


        /*mnistTest = new MnistDataSetIterator(batchSize, false, rngSeed);
        INDArray arr = mnistTest.next().getFeatureMatrix();
        System.out.println(Arrays.toString(arr.data().asDouble()));

        BufferedImage bi = new BufferedImage(28,28,BufferedImage.TYPE_BYTE_GRAY);
        for( int i=0; i<784; i++ ){
            bi.getRaster().setSample(i % 28, i / 28, 0, (int)(255*arr.getDouble(i)));
        }

        ImageIO.write(bi, "png", new File("mnist.png"));*/
    }

    void setupSocketIO(){
        Configuration config = new Configuration();
        config.setHostname(hostname);
        config.setPort(port);

        server = new SocketIOServer(config);
    }

    void doWork() throws Exception{

        mnistTrain = new MnistDataSetIterator(batchSize, true, seed);

        working = true;

        for(int i=0; i<numEpochs; i++){
            model.fit(mnistTrain);
        }

        working = false;
    }

    String evaluate() throws Exception{
        mnistTest = new MnistDataSetIterator(batchSize, false, seed);

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

            // learning rate schedule in the form of <Iteration #, Learning Rate>
            Map<Integer, Double> lrSchedule = new HashMap<>();
            lrSchedule.put(0, 0.01);
            lrSchedule.put(1000, 0.005);
            lrSchedule.put(3000, 0.001);

            conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .iterations(iterations) // Training iterations as above
                .regularization(true).l2(0.0005)
            /*
                Uncomment the following for learning decay and bias
             */
                .learningRate(.01)//.biasLearningRate(0.02)
            /*
                Alternatively, you can use a learning rate schedule.
                NOTE: this LR schedule defined here overrides the rate set in .learningRate(). Also,
                if you're using the Transfer Learning API, this same override will carry over to
                your new model configuration.
            */
                .learningRateDecayPolicy(LearningRatePolicy.Schedule)
                .learningRateSchedule(lrSchedule)
            /*
                Below is an example of using inverse policy rate decay for learning rate
            */
                //.learningRateDecayPolicy(LearningRatePolicy.Inverse)
                //.lrPolicyDecayRate(0.001)
                //.lrPolicyPower(0.75)
                .weightInit(WeightInit.XAVIER)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(Updater.NESTEROVS).momentum(0.9)
                .list()
                .layer(0, new ConvolutionLayer.Builder(5, 5)
                        //nIn and nOut specify depth. nIn here is the nChannels and nOut is the number of filters to be applied
                        .nIn(nChannels)
                        .stride(1, 1)
                        .nOut(20)
                        .activation(Activation.IDENTITY)
                        .build())
                .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2,2)
                        .stride(2,2)
                        .build())
                .layer(2, new ConvolutionLayer.Builder(5, 5)
                        //Note that nIn need not be specified in later layers
                        .stride(1, 1)
                        .nOut(50)
                        .activation(Activation.IDENTITY)
                        .build())
                .layer(3, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2,2)
                        .stride(2,2)
                        .build())
                .layer(4, new DenseLayer.Builder().activation(Activation.RELU)
                        .nOut(500).build())
                .layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(outputNum)
                        .activation(Activation.SOFTMAX)
                        .build())
                .setInputType(InputType.convolutionalFlat(28,28,1)) //See note below
                .backprop(true).pretrain(false).build();

            model = new MultiLayerNetwork(conf);
        }

        model.init();
        //print the score with every 1 iteration
        model.setListeners(new ScoreIterationListener(1));

        return;
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