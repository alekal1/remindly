package ee.aleksale.remindly.core.annotation;

import ee.aleksale.remindly.core.model.type.ReminderType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ReminderEnabled {

  ReminderType type();
}
