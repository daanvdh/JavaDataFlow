/*
 * Copyright 2018 by Daan van den Heuvel.
 *
 * This file is part of JavaDataFlow.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package util;

import java.util.function.Function;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matchers;

/**
 * Match method return values.
 *
 * @author Daan
 */
public class MethodMatcher<A, B> extends FeatureMatcher<A, B> {

  private Function<A, B> function;

  // hide constructor
  private MethodMatcher(Function<A, B> function, B expected) {
    super(Matchers.equalTo(expected), null, null);
    this.function = function;
  }

  public static <A, B> MethodMatcher<A, B> of(Function<A, B> f, B expected) {
    return new MethodMatcher<>(f, expected);
  }

  @Override
  protected B featureValueOf(A actual) {
    return function.apply(actual);
  }
}
