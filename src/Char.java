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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class Char {

    private static Logger log = LoggerFactory.getLogger(Char.class);
    private static final int OUTPUT_NUM          = 10;

    private static final int BATCH_SIZE          = 128;
    private static final int NUM_EPOCHS          = 15;
    private static final int N_CHANNELS          = 1;
    private static final int ITERATIONS          = 1;

    private static final String HOSTNAME         = "localhost";
    private static final int PORT                = 6969;
    private static final String ENCODING_PREFIX  = "base64,";

    private final int                            SEED;


    private DataSetIterator                      mnistTrain, mnistTest;
    private MultiLayerNetwork                    model;
    private MultiLayerConfiguration              conf;

    private SocketIOServer                       server;

    private BASE64Decoder decoder                = new BASE64Decoder();
    private ImageLoader loader                   = new ImageLoader();
    private Random random                        = new Random();


    private boolean working                      = false;

    Char() throws Exception{
        SEED = random.nextInt(1000);//set seed to a constant to be able to find better hyperparams

        loadModel("mnist-data.ki"); addSaveHook();
        setupSocketIO();

        server.addEventListener("read", String.class, new DataListener<String>() {
            public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
                try{

                    int contentStartIndex        = data.indexOf(ENCODING_PREFIX) + ENCODING_PREFIX.length();
                    byte[] imageBytes            = decoder.decodeBuffer(data.substring(contentStartIndex));
                    ByteArrayInputStream bis     = new ByteArrayInputStream(imageBytes);
                    BufferedImage originalImage  = ImageIO.read(bis); bis.close();

                    BufferedImage scaledImage    = scaleImage(originalImage, 28, 28);
                    INDArray values              = loader.asRowVector(scaledImage); //not gray yet

                    for(int x=0;x<scaledImage.getWidth();x++){
                        for(int y=0;y<scaledImage.getHeight();y++){

                            //invert because the input is black on white and mnist white on black
                            //and divide by 255 as the mnist values are between 0 and 1

                            values.putScalar(
                                    x + y * 28,
                                    1.0 - (toGray(new Color(scaledImage.getRGB(x, y))) / 255.0)
                            );
                        }
                    }

                    client.sendEvent(
                            "read",
                            Arrays.toString(
                                    model.output(values).data().asDouble()
                            )
                    );

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });

        server.addEventListener("work", String.class, new DataListener<String>() {
            public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
                try{
                    trainModel();
                    client.sendEvent("evaluate", evaluateModel());

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
    }

    void setupSocketIO(){
        Configuration config = new Configuration();
        config.setHostname(HOSTNAME);
        config.setPort(PORT);

        server = new SocketIOServer(config);
    }

    void trainModel() throws Exception{

        mnistTrain = new MnistDataSetIterator(BATCH_SIZE, true, SEED);

        working = true;

        for(int i=0; i<NUM_EPOCHS; i++){
            model.fit(mnistTrain);
        }

        working = false;
    }

    String evaluateModel() throws Exception{
        mnistTest = new MnistDataSetIterator(BATCH_SIZE, false, SEED);

        Evaluation eval = new Evaluation(OUTPUT_NUM); //create an evaluation object with 10 possible classes
        while(mnistTest.hasNext()){
            DataSet next = mnistTest.next();
            INDArray output = model.output(next.getFeatureMatrix()); //get the networks prediction
            eval.eval(next.getLabels(), output); //check the prediction against the true class
        }

        return eval.stats();
    }

    BufferedImage scaleImage(BufferedImage image, int width, int height){
        BufferedImage scaledImage = new BufferedImage(width, height, image.getType());
        Graphics2D graphics2d = scaledImage.createGraphics();

        graphics2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics2d.drawImage(image, 0, 0, width, height, null);
        graphics2d.dispose();

        return scaledImage;
    }

    double toGray(Color color){
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();

        return (0.299 * red + 0.587 * green + 0.114 * blue);
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
                .seed(SEED)
                .iterations(ITERATIONS)
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
                        .nIn(N_CHANNELS)
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
                        .nOut(OUTPUT_NUM)
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

    void addSaveHook(){
        final Thread mainThread = Thread.currentThread();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try{
                    File locationToSave = new File("mnist-data.ki");
                    ModelSerializer.writeModel(model, locationToSave, true);

                    mainThread.join();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) throws Exception {
        new Char();
    }

}