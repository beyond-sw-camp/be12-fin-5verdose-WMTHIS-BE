package com.example.be12fin5verdosewmthisbe.dbtransaction;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ReadOnlyTransactional {
}