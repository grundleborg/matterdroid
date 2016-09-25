package me.gberg.matterdroid.utils.immutables;

import org.immutables.value.Value;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PACKAGE, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
@Value.Style(
        get = {"is*", "get*"},
        init = "set*",
        typeAbstract = {"Immutable*"},
        typeImmutable = "*",
        visibility = Value.Style.ImplementationVisibility.PUBLIC)
public @interface ImmutablesStyle {}
