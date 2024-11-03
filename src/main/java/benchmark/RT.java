package benchmark;

import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

public final class RT {
  private RT() {
    throw new AssertionError();
  }

  private static final MethodHandle NEW_NULL_RESTRICTED_ARRAY;

  static {
    Class<?> valueClass;
    try {
      valueClass = Class.forName("jdk.internal.value.ValueClass");
    } catch (ClassNotFoundException e) {
      throw new AssertionError(e);
    }
    var lookup = lookup();
    try {
      NEW_NULL_RESTRICTED_ARRAY = lookup.findStatic(valueClass,"newNullRestrictedArray",
          methodType(Object[].class, Class.class, int.class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw (LinkageError) new LinkageError().initCause(e);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T[] newNullRestrictedArray(Class<T> component, int length) {
    try {
      return (T[]) NEW_NULL_RESTRICTED_ARRAY.invokeExact(component, length);
    } catch (RuntimeException | Error e) {
      throw e;
    } catch (Throwable e) {
      throw new AssertionError(e);
    }
  }

  private static final ClassValue<Object> CLASS_VALUE = new ClassValue<Object>() {
    @Override
    protected Object computeValue(@NotNull Class<?> type) {
      return ((Object[]) java.lang.reflect.Array.newInstance(type, 1))[0];
    }
  };

  @SuppressWarnings("unchecked")
  public static <T> T defaultValue(Class<T> type) {
    if (type.isPrimitive()) {
      throw new IllegalArgumentException(type.getName() + " is primitive");
    }
    return (T) CLASS_VALUE.get(type);
  }
}
