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
package dataflow.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * HashMap where all keys are wrapped in a {@link HashCodeWrapper}.
 *
 * @author Daan
 */
public class HashMapWrapper<T, V> {

  private Map<HashCodeWrapper<T>, V> map = new HashMap<>();

  public HashMapWrapper(Map<HashCodeWrapper<T>, V> map) {
    this.map = map;
  }

  public Set<T> keySet() {
    return map.keySet().stream().map(HashCodeWrapper::getValue).collect(Collectors.toSet());
  }

  public V get(T key) {
    return map.get(new HashCodeWrapper<>(key));
  }

  public Collection<V> values() {
    return map.values();
  }

}
