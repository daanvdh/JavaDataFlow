/*
 * Copyright (c) 2019 by Eyefreight BV (www.eyefreight.com). All rights reserved.
 *
 * This software is provided by the copyright holder and contributors "as is" and any express or implied warranties, including, but
 * not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no event shall
 * Eyefreight BV or contributors be liable for any direct, indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services; * loss of use, data, or profits; or business
 * interruption) however caused and on any theory of liability, whether in contract, strict liability, or tort (including
 * negligence or otherwise) arising in any way out of the use of this software, even if advised of the possibility of such damage.
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
