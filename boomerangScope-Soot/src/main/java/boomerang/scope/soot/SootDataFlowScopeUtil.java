package boomerang.scope.soot;

import boomerang.scope.DataFlowScope;
import boomerang.scope.DeclaredMethod;
import boomerang.scope.Method;
import boomerang.scope.soot.jimple.JimpleDeclaredMethod;
import boomerang.scope.soot.jimple.JimpleMethod;
import boomerang.scope.soot.jimple.JimpleWrappedClass;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.util.queue.QueueReader;

public class SootDataFlowScopeUtil {
  private static final Logger LOGGER = LoggerFactory.getLogger(SootDataFlowScopeUtil.class);
  private static final String HASH_CODE_SUB_SIG = "int hashCode()";
  private static final String TO_STRING_SUB_SIG = "java.lang.String toString()";
  private static final String EQUALS_SUB_SIG = "boolean equals(java.lang.Object)";
  private static final String CLONE_SIG = "java.lang.Object clone()";
  public static Predicate<SootClass>[] classFilters;
  public static Predicate<SootMethod>[] methodFilters;

  /**
   * Default data-flow scope that only excludes phantom and native methods.
   *
   * @param scene
   * @return
   */
  public static DataFlowScope make(Scene scene) {
    reset();
    return new DataFlowScope() {
      @Override
      public boolean isExcluded(DeclaredMethod method) {
        return method.getDeclaringClass().isPhantom();
      }

      public boolean isExcluded(Method method) {
        return method.getDeclaringClass().isPhantom();
      }
    };
  }

  /**
   * Excludes hashCode, toString, equals methods and the implementors of java.util.Collection,
   * java.util.Maps and com.google.common.collect.Multimap
   */
  public static DataFlowScope excludeComplex() {
    reset();
    return new DataFlowScope() {
      @Override
      public boolean isExcluded(DeclaredMethod method) {
        JimpleDeclaredMethod m = (JimpleDeclaredMethod) method;
        for (Predicate<SootClass> f : classFilters) {
          SootClass sootClass = ((JimpleWrappedClass) m.getDeclaringClass()).getDelegate();
          if (f.apply(sootClass)) {
            return true;
          }
        }
        for (Predicate<SootMethod> f : methodFilters) {
          // TODO
          // if (f.apply((SootMethod) m.getDelegate())) {
          //  return true;
          // }
        }
        return m.getDeclaringClass().isPhantom();
      }

      public boolean isExcluded(Method method) {
        JimpleMethod m = (JimpleMethod) method;
        for (Predicate<SootClass> f : classFilters) {
          SootClass sootClass = ((JimpleWrappedClass) m.getDeclaringClass()).getDelegate();
          if (f.apply(sootClass)) {
            return true;
          }
        }
        for (Predicate<SootMethod> f : methodFilters) {
          if (f.apply(m.getDelegate())) {
            return true;
          }
        }
        return m.getDeclaringClass().isPhantom() || m.isPhantom();
      }
    };
  }

  private static class MapFilter implements Predicate<SootClass> {
    private static final String MAP = "java.util.Map";
    private static final String GUAVA_MAP = "com.google.common.collect.Multimap";
    private final Set<SootClass> excludes = Sets.newHashSet();

    public MapFilter() {
      List<SootClass> mapSubClasses =
          Scene.v().getActiveHierarchy().getImplementersOf(Scene.v().getSootClass(MAP));
      excludes.add(Scene.v().getSootClass(MAP));
      excludes.addAll(mapSubClasses);
      if (Scene.v().containsClass(GUAVA_MAP)) {
        SootClass c = Scene.v().getSootClass(GUAVA_MAP);
        if (c.isInterface()) {
          excludes.addAll(Scene.v().getActiveHierarchy().getImplementersOf(c));
        }
      }
      for (SootClass c : Scene.v().getClasses()) {
        if (c.hasOuterClass() && excludes.contains(c.getOuterClass())) excludes.add(c);
      }
      if (excludes.isEmpty()) {
        LOGGER.debug("Excludes empty for {}", MAP);
      }
    }

    @Override
    public boolean apply(SootClass c) {
      return excludes.contains(c);
    }
  }

  private static class IterableFilter implements Predicate<SootClass> {
    private static final String ITERABLE = "java.lang.Iterable";
    private final Set<SootClass> excludes = Sets.newHashSet();

    public IterableFilter() {
      List<SootClass> iterableSubClasses =
          Scene.v().getActiveHierarchy().getImplementersOf(Scene.v().getSootClass(ITERABLE));
      excludes.addAll(iterableSubClasses);
      for (SootClass c : Scene.v().getClasses()) {
        if (c.hasOuterClass() && excludes.contains(c.getOuterClass())) excludes.add(c);
      }
      if (excludes.isEmpty()) {
        LOGGER.debug("Excludes empty for {}", ITERABLE);
      }
    }

    @Override
    public boolean apply(SootClass c) {
      return excludes.contains(c);
    }
  }

  private static class SubSignatureFilter implements Predicate<SootMethod> {
    private final Set<SootMethod> excludes = Sets.newHashSet();

    public SubSignatureFilter(String subSig) {
      QueueReader<MethodOrMethodContext> l = Scene.v().getReachableMethods().listener();
      while (l.hasNext()) {
        SootMethod m = l.next().method();
        if (m.getSubSignature().equals(subSig)) {
          excludes.add(m);
        }
      }
      if (excludes.isEmpty()) {
        LOGGER.debug("Excludes empty for {}", subSig);
      }
    }

    @Override
    public boolean apply(SootMethod m) {
      return excludes.contains(m);
    }
  }

  private static void reset() {
    classFilters = new Predicate[] {new MapFilter(), new IterableFilter()};
    methodFilters =
        new Predicate[] {
          new SubSignatureFilter(HASH_CODE_SUB_SIG),
          new SubSignatureFilter(TO_STRING_SUB_SIG),
          new SubSignatureFilter(EQUALS_SUB_SIG),
          new SubSignatureFilter(CLONE_SIG)
        };
  }
}
