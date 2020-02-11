package com.codingapi.deeplearning.demo10.learn.layer;

import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * @author lorne
 * @date 2020/1/29
 * @description
 */
public class SubsamplingLayer implements NeuralNetworkLayer {

    private int[] kernelSizes;
    private int[] strides;

    //当前层数索引
    protected int index;
    //所有的网络层
    protected NeuralNetworkLayerBuilder layerBuilder;


    private static  SubsamplingLayerBuilder builder = new SubsamplingLayerBuilder();

    public static SubsamplingLayerBuilder builder() {
        return builder;
    }

    protected SubsamplingLayer(int[] kernelSizes, int[] strides) {
        this.kernelSizes = kernelSizes;
        this.strides = strides;
    }

    @Override
    public INDArray forward(INDArray data) {
        return data;
    }


    @Override
    public LayerInitor initLayer(LayerInitor layerInitor) {
        return layerInitor;
    }

    @Override
    public void build(NeuralNetworkLayerBuilder layer, int index) {
        this.index = index;
        this.layerBuilder = layer;
    }

}
