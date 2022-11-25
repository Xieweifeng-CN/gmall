package com.jack.gmall.common.cache;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Java0509Cache {
    String prefix() default "cache";
}
