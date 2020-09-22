package com.codingapi.deeplearning.demo10.learn.layer;

import com.codingapi.deeplearning.demo10.learn.activation.Activation;
import com.codingapi.deeplearning.demo10.learn.core.InputType;
import lombok.extern.slf4j.Slf4j;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;

/**
 * @author lorne
 * @date 2020/1/29
 * @description
 */
@Slf4j
public class ConvolutionLayer extends BaseLayer {

    //管道数量
    private int channels;

    //内核大小
    private int[] kernelSizes;
    //卷积步长
    private int[] strides;
    //输出管道数量 对应filter的数量
    private int outChannels;
    //激活函数
    private Activation activation;
    //padding 大小
    private int[] padding;


    private INDArray filters;

    private INDArray b;

    private INDArray a;

    private double lambda;
    private double alpha;

    private int outSize;
    private InputType input;

    /**
     * 卷积计算
     * @param data   [patch,(n,n,channel)]
     * @param filter [channel,(kernelSizes)]
     * @return
     */
    private INDArray convolution(INDArray data, INDArray filter) {
        int channel = input.getChannel();
        int patch = data.rows();

        INDArray batchOutArray = Nd4j.create(patch,(outSize*outSize));

        for(int i=0;i<patch;i++) {

            INDArray outDepthArray = Nd4j.zeros(outSize,outSize);

            INDArray channelData = data.getRow(i).reshape(input.getChannel(),input.getHeight()*input.getWidth());
            for(int c=0;c<channel;c++) {
                INDArray rowData = channelData.getRow(c).reshape(input.getHeight(),input.getWidth());
                INDArray outArray = Nd4j.create(outSize,outSize);
                INDArray channelFilter = filter.getRow(c).reshape(kernelSizes);
                for (int x = 0; x < outSize; x++) {
                    for (int y = 0; y < outSize; y++) {

                        INDArray item = rowData.get(NDArrayIndex.interval(x, x + kernelSizes[0]),
                                NDArrayIndex.interval(y, y + kernelSizes[1]));

                        Number sum = item.mul(channelFilter).sumNumber();

                        outArray.put(x, y, sum);
                    }
                }
                outDepthArray.add(outArray);
            }

            batchOutArray.putRow(i,outDepthArray.reshape(1,outSize*outSize));
        }

        return batchOutArray;
    }

    @Override
    public INDArray forward(INDArray data) {
        int patch = data.rows();

        //todo data padding ...

        INDArray a = Nd4j.create(patch,outChannels*outSize*outSize);

        for(int i=0;i<outChannels;i++){

            INDArray filter = filters.getRow(i).reshape(input.getChannel(),kernelSizes[0]*kernelSizes[1]);

            INDArray convolution =  convolution(data,filter);

            INDArray z = convolution.addi(b.getNumber(0,i));

            INDArray res = activation.activation(z);

            INDArrayIndex[] index = new INDArrayIndex[]{
                    NDArrayIndex.interval(0,patch),
                    NDArrayIndex.interval(
                             outSize*outSize*i,
                             outSize*outSize*(i+1))};

            a.put(index,res);
        }
        this.a = a;
        return a;
    }

    @Override
    public INDArray backprop(INDArray delta) {

        return delta;
    }



    @Override
    public LayerInitor initLayer(LayerInitor layerInitor) {
        this.lambda = layerInitor.getLamdba();
        this.alpha = layerInitor.getAlpha();
        this.input = layerInitor.getInputType();

        InputType inputType = layerInitor.getInputType();

        int seed = layerInitor.getSeed();

        filters = Nd4j.rand(outChannels,inputType.getChannel()*kernelSizes[0]*kernelSizes[1],seed);

        //{(n +2 x padding-filter) \over strides + 1}
        outSize = ((inputType.getHeight() + 2 * padding[0] - kernelSizes[0]) / strides[0] + 1 );

        b = Nd4j.rand(1,outChannels,seed);

        return new LayerInitor(alpha,lambda,seed,new InputType(outSize,outSize,outChannels));
    }

    @Override
    public INDArray w() {
        return filters;
    }

    @Override
    public INDArray a() {
        return a;
    }

    @Override
    public void updateParam() {
        //todo update param
    }

    @Override
    public boolean isOutLayer() {
        return false;
    }

    protected ConvolutionLayer(int channels, int[] kernelSizes, int[] strides, int outChannels, Activation activation, int[] padding) {
        this.channels = channels;
        this.kernelSizes = kernelSizes;
        this.strides = strides;
        this.outChannels = outChannels;
        this.activation = activation;
        this.padding = padding;
    }

    private static ConvolutionLayerBuilder builder = new ConvolutionLayerBuilder();

    public static ConvolutionLayerBuilder builder(){
        return builder;
    }
}
