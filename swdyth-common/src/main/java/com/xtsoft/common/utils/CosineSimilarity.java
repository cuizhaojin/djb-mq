package com.xtsoft.common.utils;

import cn.hutool.core.util.StrUtil;
import jdk.nashorn.internal.runtime.logging.Logger;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description: 余玄相似度计算工具类
 * @author: cll
 * @date: 2023/1/12 9:48 
 * @param: null
 * @return: 
 */
@Data
public class CosineSimilarity {
    /**
     * 1、计算两个字符串的相似度
     */
    public static BigDecimal getSimilarity(String text1, String text2) {

        //如果wei空，或者字符长度为0，则代表完全相同
        if (StrUtil.isBlank(text1) && StrUtil.isBlank(text2)) {
            return new BigDecimal("1.0");
        }
        //如果一个为0或者空，一个不为，那说明完全不相似
        if (StrUtil.isBlank(text1) || StrUtil.isBlank(text2)) {
            return new BigDecimal("0.0");
        }
        //这个代表如果两个字符串相等那当然返回1了（这个我为了让它也分词计算一下，所以注释掉了）
//        if (text1.equalsIgnoreCase(text2)) {
//            return 1.0;
//        }
        //第一步：进行分词
        List<Word> words1 = Tokenizer.segment(text1);
        List<Word> words2 = Tokenizer.segment(text2);

        return getSimilarity(words1, words2);
    }

    /**
     * 2、对于计算出的相似度保留小数点后六位
     */
    public static BigDecimal getSimilarity(List<Word> words1, List<Word> words2) {

        BigDecimal score = getSimilarityImpl(words1, words2);

        return score.setScale(6,BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 文本相似度计算 判定方式：余弦相似度，通过计算两个向量的夹角余弦值来评估他们的相似度 余弦夹角原理： 向量a=(x1,y1),向量b=(x2,y2) similarity=a.b/|a|*|b| a.b=x1x2+y1y2
     * |a|=根号[(x1)^2+(y1)^2],|b|=根号[(x2)^2+(y2)^2]
     */
    public static BigDecimal getSimilarityImpl(List<Word> words1, List<Word> words2) {

        // 向每一个Word对象的属性都注入weight（权重）属性值
        taggingWeightByFrequency(words1, words2);

        //第二步：计算词频
        //通过上一步让每个Word对象都有权重值，那么在封装到map中（key是词，value是该词出现的次数（即权重））
        Map<String, Float> weightMap1 = getFastSearchMap(words1);
        Map<String, Float> weightMap2 = getFastSearchMap(words2);

        //将所有词都装入set容器中
        Set<Word> words = new HashSet<>();
        words.addAll(words1);
        words.addAll(words2);

        AtomicFloat ab = new AtomicFloat();// a.b
        AtomicFloat aa = new AtomicFloat();// |a|的平方
        AtomicFloat bb = new AtomicFloat();// |b|的平方

        // 第三步：写出词频向量，后进行计算
        words.parallelStream().forEach(word -> {
            //看同一词在a、b两个集合出现的此次
            Float x1 = weightMap1.get(word.getName());
            Float x2 = weightMap2.get(word.getName());
            if (x1 != null && x2 != null) {
                //x1x2
                float oneOfTheDimension = x1 * x2;
                //+
                ab.addAndGet(oneOfTheDimension);
            }
            if (x1 != null) {
                //(x1)^2
                float oneOfTheDimension =x1 * x1;
                //+
                aa.addAndGet(oneOfTheDimension);
            }
            if (x2 != null) {
                //(x2)^2
                float oneOfTheDimension = x2 * x2;
                //+
                bb.addAndGet(oneOfTheDimension);
            }
        });
        //|a| 对aa开方
        BigDecimal aaa = sqrt(BigDecimal.valueOf(aa.get()), 6);
        //|b| 对bb开方
        BigDecimal bbb = sqrt(BigDecimal.valueOf(bb.get()), 6);

        //使用BigDecimal保证精确计算浮点数
        //double aabb = aaa * bbb;
        BigDecimal aabb = aaa.multiply(bbb);

        //similarity=a.b/|a|*|b|
        //divide参数说明：aabb被除数,9表示小数点后保留9位，最后一个表示用标准的四舍五入法
        BigDecimal cos = BigDecimal.valueOf(ab.get()).divide(aabb, 9, BigDecimal.ROUND_HALF_UP);
        return cos;
    }


    /**
     * 向每一个Word对象的属性都注入weight（权重）属性值
     */
    protected static void taggingWeightByFrequency(List<Word> words1, List<Word> words2) {
        if (words1.get(0).getWeight() != null && words2.get(0).getWeight() != null) {
            return;
        }
        //词频统计（key是词，value是该词在这段句子中出现的次数）
        Map<String, AtomicInteger> frequency1 = getFrequency(words1);
        Map<String, AtomicInteger> frequency2 = getFrequency(words2);

        //如果是DEBUG模式输出词频统计信息
//        if (LOGGER.isDebugEnabled()) {
//            LOGGER.debug("词频统计1：\n{}", getWordsFrequencyString(frequency1));
//            LOGGER.debug("词频统计2：\n{}", getWordsFrequencyString(frequency2));
//        }
        // 标注权重（该词出现的次数）
        words1.parallelStream().forEach(word -> word.setWeight(frequency1.get(word.getName()).floatValue()));
        words2.parallelStream().forEach(word -> word.setWeight(frequency2.get(word.getName()).floatValue()));
    }

    /**
     * 统计词频
     * @return 词频统计图
     */
    private static Map<String, AtomicInteger> getFrequency(List<Word> words) {

        Map<String, AtomicInteger> freq = new HashMap<>();
        //这步很帅哦
        words.forEach(i -> freq.computeIfAbsent(i.getName(), k -> new AtomicInteger()).incrementAndGet());
        return freq;
    }

    /**
     * 输出：词频统计信息
     */
    private static String getWordsFrequencyString(Map<String, AtomicInteger> frequency) {
        StringBuilder str = new StringBuilder();
        if (frequency != null && !frequency.isEmpty()) {
            AtomicInteger integer = new AtomicInteger();
            frequency.entrySet().stream().sorted((a, b) -> b.getValue().get() - a.getValue().get()).forEach(
                    i -> str.append("\t").append(integer.incrementAndGet()).append("、").append(i.getKey()).append("=")
                            .append(i.getValue()).append("\n"));
        }
        str.setLength(str.length() - 1);
        return str.toString();
    }

    /**
     * 构造权重快速搜索容器
     */
    protected static Map<String, Float> getFastSearchMap(List<Word> words) {
        if (CollectionUtils.isEmpty(words)) {
            return Collections.emptyMap();
        }
        Map<String, Float> weightMap = new ConcurrentHashMap<>(words.size());

        words.parallelStream().forEach(i -> {
            if (i.getWeight() != null) {
                weightMap.put(i.getName(), i.getWeight());
            } else {
                System.out.println("no word weight info:" + i.getName());
            }
        });
        return weightMap;
    }

    /**
     * 使用牛顿迭代法开根号
     */
    public static BigDecimal sqrt(BigDecimal value, int scale){
        BigDecimal num2 = BigDecimal.valueOf(2);
        int precision = 100;
        MathContext mc = new MathContext(precision, RoundingMode.HALF_UP);
        BigDecimal deviation = value;
        int cnt = 0;
        while (cnt < precision) {
            deviation = (deviation.add(value.divide(deviation, mc))).divide(num2, mc);
            cnt++;
        }
        deviation = deviation.setScale(scale, BigDecimal.ROUND_HALF_UP);
        return deviation;
    }

}