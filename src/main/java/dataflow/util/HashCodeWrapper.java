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

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;

/**
 * A wrapper class to override the equals and hashCode methods to only be equal if 2 objects are the exact same instance. This class is typically used so that
 * {@link JavaParser} {@link Node} classes that have equals and hashCode methods that are not strict enough, can be used as key of a hashMap.
 *
 * @author Daan
 */
public class HashCodeWrapper<T> {

  private final T value;

  public HashCodeWrapper(T value) {
    this.value = value;
  }

  public T getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "HashCodeWrapper[" + value.toString() + "]";
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(value);
  }

  @Override
  public boolean equals(Object obj) {
    boolean equal = false;
    if (this.getClass().isInstance(obj)) {
      equal = value == ((HashCodeWrapper<?>) obj).getValue();
    }
    return equal;
  }

}
