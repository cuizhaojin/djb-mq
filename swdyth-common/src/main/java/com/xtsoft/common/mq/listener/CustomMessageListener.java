package com.xtsoft.common.mq.listener;

/**
 * @author: cuizhaojin
 * @date: 2024/8/14 16:43
 * @description:
 */
import com.xtsoft.common.mq.vo.ConsumeMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CustomMessageListener {
    String topic();
    String selectorExpression() default "*";
    String consumerGroup();
    ConsumeMode consumeMode() default ConsumeMode.ORDERLY;
}

