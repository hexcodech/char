import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.ack.AckManager;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.protocol.JacksonJsonSupport;
import com.corundumstudio.socketio.protocol.PacketDecoder;
import org.datavec.image.loader.ImageLoader;
import org.deeplearning4j.nn.conf.LearningRatePolicy;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
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
import org.nd4j.linalg.factory.Nd4j;
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
    private static final int OUTPUT_NUM          = 6;
    private static final int SQUARE_SIZE         = 48;

    private static final int BATCH_SIZE          = 128;
    private static final int NUM_EPOCHS          = 15;
    private static final int N_CHANNELS          = 1;
    private static final int ITERATIONS          = 1;

    private static final String ENCODING_PREFIX  = "base64,";

    private final int                            SEED;


    private DataSetIterator                      train, test;
    private MultiLayerNetwork                    model;
    private MultiLayerConfiguration              conf;

    private SocketIOServer                       server;

    private BASE64Decoder base64Decoder          = new BASE64Decoder();
    private ImageLoader loader                   = new ImageLoader();
    private Random random                        = new Random();
    private AckManager                           ackManager;
    private PacketDecoder packetDecoder          = new PacketDecoder( new JacksonJsonSupport(), ackManager);

    private String HOSTNAME;
    private int PORT;

    private boolean working                      = false;

    Char(boolean train) throws Exception{
        SEED = random.nextInt(1000);
        new Char(true, "localhost", 6969);
    }

    Char(String hostname, int port) throws Exception{
        SEED = random.nextInt(1000);
        new Char(false, hostname, port);
    }

    Char(boolean train, String hostname, int port) throws Exception{

        HOSTNAME = hostname;
        PORT = port;
        SEED = random.nextInt(1000);

        loadModel("fer13.ki"); addSaveHook();

        if(train){
            trainModel();
            log.info(evaluateModel());
        }else{
            startServer();
        }

    }

    void startServer(){
        setupSocketIO();

        server.addEventListener("read", FaceObject.class, new DataListener<FaceObject>() {
            public void onData(SocketIOClient client, FaceObject face, AckRequest ackRequest) {

                int minLen= Math.min(face.getWidth(), face.getHeight());
                int maxLen = Math.max(face.getWidth(), face.getHeight());

                if(minLen < SQUARE_SIZE){
                    client.sendEvent(
                            "read", "[0,0,0,0,0,0]"
                    );

                    return;
                }

                try{
                    int contentStartIndex        = face.getImage().indexOf(ENCODING_PREFIX) + ENCODING_PREFIX.length();
                    byte[] imageBytes            = base64Decoder.decodeBuffer(face.getImage().substring(contentStartIndex));
                    ByteArrayInputStream bis     = new ByteArrayInputStream(imageBytes);
                    BufferedImage originalImage  = ImageIO.read(bis);
                                                   bis.close();

                    BufferedImage croppedImage   = cropImage(originalImage, new Rectangle(face.getX(), face.getY(), maxLen, maxLen));


                    //ImageIO.write(croppedImage, "png", new File("orig.png"));

                    INDArray values              = grayScaleImage(croppedImage, SQUARE_SIZE, SQUARE_SIZE);

                   /* BufferedImage bi = new BufferedImage(SQUARE_SIZE,SQUARE_SIZE,BufferedImage.TYPE_BYTE_GRAY);
                    for( int i=0; i<SQUARE_SIZE*SQUARE_SIZE; i++ ){
                        bi.getRaster().setSample(i % SQUARE_SIZE, i / SQUARE_SIZE, 0, (int)(255*values.getDouble(i)));
                    }*/

                    //ImageIO.write(bi, "png", new File("read-gray-scaled.png"));

                    double[] output = model.output(values).data().asDouble();
                    int max = 0;

                    for(int i=1;i<output.length;i++){
                        if(output[i] > output[max]){
                            max = i;
                        }
                    }

                    log.info("Read " + max + " (" + output[max] + ")");

                    client.sendEvent(
                            "read",
                            Arrays.toString(
                                    output
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

        train = new Fer13DataIterator(BATCH_SIZE, true, SEED);

        working = true;

        for(int i=0; i<NUM_EPOCHS; i++){
            model.fit(train);
        }

        working = false;
    }

    String evaluateModel() throws Exception{
        test = new Fer13DataIterator(BATCH_SIZE, false, SEED);

        Evaluation eval = new Evaluation(OUTPUT_NUM); //create an evaluation object with 10 possible classes
        while(test.hasNext()){
            DataSet next = test.next();
            INDArray output = model.output(next.getFeatureMatrix()); //get the networks prediction
            eval.eval(next.getLabels(), output); //check the prediction against the true class
        }

        return eval.stats();
    }

    BufferedImage cropImage(BufferedImage src, Rectangle rect) {
        BufferedImage dest = src.getSubimage(0, 0, rect.width, rect.height);
        return dest;
    }

    INDArray grayScaleImage(BufferedImage image, int width, int height){
        INDArray values = Nd4j.zeros(width*height);

        double boxWidth  = (((double) image.getWidth())  / (double) width);
        double boxHeight = (((double) image.getHeight()) / (double) height);

        for(int x=0; x<width; x++){
            for(int y=0; y<height; y++){

                double sum = 0, weight = 0;

                for(int x2 = (int) Math.floor(x*boxWidth); x2 < (x+1) * boxWidth; x2++){
                    for(int y2 = (int) Math.floor(y*boxHeight); y2 < (y+1) * boxHeight; y2++){

                        Color color;

                        if((x2 < image.getWidth() && y2 < image.getHeight())){
                            color = new Color(image.getRGB(x2, y2));
                        }else{
                            color = Color.white;
                        }

                        double red   = (double) color.getRed();
                        double green = (double) color.getGreen();
                        double blue  = (double) color.getBlue();

                        sum += (0.299f * red + 0.587f * green + 0.114f * blue);
                        weight++;
                    }
                }

                //invert because the input is black on white and mnist white on black
                //and divide by 255 as the mnist values are between 0 and 1

                values.putScalar(x + y * width, 1.0f - (float) ((sum/weight/255) * (sum/weight/255)));
            }
        }

        return values;
    }

    double toGray(Color color){
        double red   = (double) color.getRed();
        double green = (double) color.getGreen();
        double blue  = (double) color.getBlue();

        return (0.299f * red + 0.587f * green + 0.114f * blue);
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
                .setInputType(InputType.convolutionalFlat(SQUARE_SIZE,SQUARE_SIZE,1)) //See note below
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
                    File locationToSave = new File("fer13.ki");
                    ModelSerializer.writeModel(model, locationToSave, true);

                    mainThread.join();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args){
        try{
            if(args.length == 0){
                new Char(true);
            }else if(args.length == 2){
                new Char(args[0], Integer.parseInt(args[1]));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}