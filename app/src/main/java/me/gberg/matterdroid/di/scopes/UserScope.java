package me.gberg.matterdroid.di.scopes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * Created by gberg on 31/07/16.
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface UserScope {
}
